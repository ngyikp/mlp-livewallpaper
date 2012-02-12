package com.overkill.ponymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.overkill.live.pony.LiveWallpaperSettings;
import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.R;
import com.overkill.live.pony.ToolSet;
import com.overkill.ponymanager.AsynFolderDownloader.onDownloadListener;
import com.overkill.ponymanager.AsynImageLoader.onImageListener;
import com.overkill.ponymanager.pony.DownloadPony;
import com.overkill.ponymanager.pony.DownloadPonyAdapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PonyManager extends ListActivity implements onDownloadListener, onImageListener{
	public static final String TAG = "PonyManager";
	public static final String ACTION_REMOVE_PONY = "com.overkill.live.pony.action.removed";
	
	public static final String REMOTE_BASE_URL = "http://mlp-livewallpaper.googlecode.com/svn/assets/";
	public static final String REMOTE_LIST_URL = REMOTE_BASE_URL + "ponies2.lst";
	
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
	          "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
	          "\\@" +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
	          "(" +
	          "\\." +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
	          ")+"
	      );
	
	public static final int ACTION_INSTALL = 1;
	public static final int ACTION_DELETE = 2;
	public static final int ACTION_STOP = 3;
	public static final int ACTION_UPDATE = 4;
	public static final int ACTION_REPORT = 5;
	
	public static final int THEMES[] = {R.style.Theme_Pony_Applejack, R.style.Theme_Pony_Rainbowdash, R.style.Theme_Pony_Fluttershy, R.style.Theme_Pony_PinkiePie, R.style.Theme_Pony_Rarity, R.style.Theme_Pony_TwilightSparkle};
	
	public static final String filterOptions[] = {"SORT A-Z", "SORT Z-A", "Show all", "Only show not installed", "Only show updates", "Filter By Category"};
	private int currentFilter = 0;
	private boolean currentSortASC = true;
	
	File localFolder;
	
	protected DownloadPonyAdapter adapter = null;

	AsynImageLoader asynImageLoader;
	
	DialogInterface.OnClickListener filterListener = new OnClickListener() {				
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(adapter == null) return; // adapter is not ready yet
			currentFilter = which;
			switch (which) {
			case 0: // A-Z
				currentSortASC = true;
				break;
			case 1: // Z-A
				currentSortASC = false;
				break;
			case 2: // show all
				adapter.resetFilter();
				break;
			case 3: // only not installed
				adapter.filterByState(R.string.pony_state_not_installed);
				break;
			case 4: // only updates
				adapter.filterByState(R.string.pony_state_update);
				break;
			case 5: // show category dialog
				currentFilter = -1;
				if(dialog != null) dialog.dismiss();
				AlertDialog.Builder categoryDialog = new AlertDialog.Builder(PonyManager.this);
				categoryDialog.setTitle("Filter Options");		
				categoryDialog.setMultiChoiceItems(adapter.getCategoryNamesWithCount(), adapter.getCategoryStates(), new OnMultiChoiceClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						adapter.setCategoryFilter(adapter.getCategoryNames()[which], isChecked);						
					}
				});
				categoryDialog.setNegativeButton(android.R.string.cancel, null);
				categoryDialog.setPositiveButton(android.R.string.ok, new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						adapter.filterByCategory();		
						dialog.dismiss();
						adapter.notifyDataSetChanged();
					}
				});
				categoryDialog.show();				
				break;
			default:
				break;
			}	
			adapter.sort(currentSortASC);
			adapter.notifyDataSetChanged();
			if(dialog != null) dialog.dismiss();
		}				
	};
	
	public void setProgressbarVisibility(boolean visible){
		 int v = (visible) ? View.VISIBLE : View.GONE;
		 ((ProgressBar)findViewById(R.id.progress_circular)).setVisibility(v);
	 }
	
	public void setDownloadAllVisibility(boolean visible){
		 int v = (visible) ? View.VISIBLE : View.GONE;
		 ((ImageButton)findViewById(R.id.btn_title_download_all)).setVisibility(v);
		 ((ImageView)findViewById(R.id.sep_title_download_all)).setVisibility(v);
	}
	
	public void setUpdateAllVisibility(boolean visible){
		 int v = (visible) ? View.VISIBLE : View.GONE;
		 ((ImageButton)findViewById(R.id.btn_title_update_all)).setVisibility(v);
		 ((ImageView)findViewById(R.id.sep_title_update_all)).setVisibility(v);
	}
	
	public void setSetButtonVisibility(boolean visible){
		 int v = (visible) ? View.VISIBLE : View.GONE;
		 ((ImageButton)findViewById(R.id.btn_title_set)).setVisibility(v);
		 ((ImageView)findViewById(R.id.sep_title_set)).setVisibility(v);
	}
	
	public void setPreferencesVisibility(boolean visible){
		 int v = (visible) ? View.VISIBLE : View.GONE;
		 ((ImageButton)findViewById(R.id.btn_title_preferences)).setVisibility(v);
		 ((ImageView)findViewById(R.id.sep_title_preferences)).setVisibility(v);
	}
	 
	public void updateTitle(){
		setDownloadAllVisibility(false);
		setUpdateAllVisibility(false);
		setSetButtonVisibility(false);
		setPreferencesVisibility(false);
		
		if(isWallpaperInUse() == false){
			setSetButtonVisibility(true);
		}
		if(isWallpaperInUse()){
			setPreferencesVisibility(true);
		}
		
		if(adapter != null){
			if(adapter.hasUpdate())
				setUpdateAllVisibility(true);
			if(adapter.hasNotInstalled())
				setDownloadAllVisibility(true);
		}	
		
		filterListener.onClick(null, currentFilter);
	}
	
	private boolean isWallpaperInUse(){
		WallpaperInfo currentWallpaper = WallpaperManager.getInstance(this).getWallpaperInfo();
		if(currentWallpaper == null){
			return false;
		}else{
			return currentWallpaper.getPackageName().equals("com.overkill.live.pony");
		}
	}
	 
	@Override
	public void onDownloadStart(String ID) {	
		DownloadPony p = adapter.getItem(ID);
		if(p == null) return;
		p.setState(R.string.pony_state_loading);
		p.setDoneFileCount(0);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}

	@Override
	public void onDownloadChanged(String ID, int filesDone) {
		DownloadPony p = adapter.getItem(ID);
		if(p == null) return;
		p.setDoneFileCount(filesDone);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});		
	}

	@Override
	public void onDownloadDone(String ID) {
		DownloadPony p = adapter.getItem(ID);
		if(p == null) return;
		p.setState(R.string.pony_state_installed);
		SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("lastupdate_" + p.getFolder(), p.getLastUpdate());
		editor.commit();
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	updateTitle(); }
		});			
	}

	@Override
	public void onDownloadError(String ID, final String error) {
		final DownloadPony p = adapter.getItem(ID);
		if(p == null) return;
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				p.setState(R.string.pony_state_not_installed);				
				Toast.makeText(PonyManager.this, "Error while downloading \"" + p.getName() + "\"\n" + error, Toast.LENGTH_LONG).show();
				adapter.notifyDataSetChanged();
				
				// clean up already finished files
				File folder = new File(localFolder, p.getFolder());
				if(folder.isDirectory() == false) return;
				File[] files = folder.listFiles();
				if(files == null) return;
				for(File f : files){
					f.delete();
				}
				folder.delete();
			}
		});
	}
	
	@Override
	public void imageError(String ID, final String error) {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	Toast.makeText(PonyManager.this, error, Toast.LENGTH_LONG).show(); }
		});	
	}
	
	@Override
	public void imageComplete(String ID, Bitmap image) {
		final DownloadPony p = adapter.getItem(ID);
		if(p == null) return;
		p.setImage(image);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}
	
	public void onTitleClick(View view){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		switch (view.getId()) {
		case R.id.btn_title_filter:
			dialog.setTitle("Filter Options");
			dialog.setItems(filterOptions, filterListener);
			dialog.show();
			break;
		case R.id.btn_title_preferences:
			startActivity(new Intent(this, LiveWallpaperSettings.class));
			break;
		case R.id.btn_title_help:	
			showHelpDialog();
			break;
		case R.id.btn_title_update_all:
			int count_update = 0;
			for(int i = 0; i < adapter.getCount(); i++){
				DownloadPony p = adapter.getItem(i);
				if(p.getState() == R.string.pony_state_update)
					count_update++;
			}
			dialog.setTitle(R.string.manager_update_all);
			dialog.setMessage(getResources().getQuantityString(R.plurals.numberOfPoniesForAction, count_update, getString(R.string.manager_update), count_update));
			dialog.setNegativeButton(android.R.string.no, null);
			dialog.setPositiveButton(android.R.string.yes, new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					for(int i = 0; i < adapter.getCount(); i++){
						DownloadPony p = adapter.getItem(i);
						if(p.getState() == R.string.pony_state_update)
							actionUpdate(p);
					}				
					updateTitle();		
				}
			});
			dialog.show();	
			break;
		case R.id.btn_title_download_all:	
			int count_download = 0;
			for(int i = 0; i < adapter.getCount(); i++){
				DownloadPony p = adapter.getItem(i);
				if(p.getState() == R.string.pony_state_not_installed)
					count_download++;
			}
			dialog.setTitle(R.string.manager_download_all);
			dialog.setMessage(getResources().getQuantityString(R.plurals.numberOfPoniesForAction, count_download, getString(R.string.manager_download), count_download));
			dialog.setNegativeButton(android.R.string.no, null);
			dialog.setPositiveButton(android.R.string.yes, new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					for(int i = 0; i < adapter.getCount(); i++){
						DownloadPony p = adapter.getItem(i);
						if(p.getState() == R.string.pony_state_not_installed)
							actionDownload(p);
					}				
					updateTitle();		
				}
			});
			dialog.show();	
			break;
		case R.id.btn_title_set:
			Intent intent = new Intent();
			intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
			startActivityForResult(intent, 0);
			Toast.makeText(this, R.string.set_wallpaper, Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("PonyManager", "onActivityResult");
		updateTitle();
		super.onActivityResult(requestCode, resultCode, data);
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
	    Random r = new Random();
	    setTheme(THEMES[r.nextInt(THEMES.length)]);
		super.onCreate(savedInstanceState);      
	    setContentView(R.layout.pony_manager);	
	    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.system_title);
	    updateTitle();
		localFolder = PonyManager.selectFolder(this);
		asynImageLoader = new AsynImageLoader(this, this);
		
		SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
		if(preferences.getBoolean("updatedFolderNameDialog", false) == false){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Updated folder names");
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setMessage(Html.fromHtml("With this update some of the pony folder names have changed.<br>To prevent you from having duplicate ponies, it's suggested to delete all ponies marked with <font color=\"#00ffff\">this color</font>."));
			dialog.setPositiveButton(android.R.string.ok, null);
			dialog.show();			
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("updatedFolderNameDialog", true);
			editor.commit();
		}
		
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				getListView().showContextMenuForChild(arg1);				
			}			
		});
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(adapter != null) return;
		new Thread(new Runnable() {			
			@Override
			public void run() {
				final ArrayList<DownloadPony> ponies = loadPonies();
				adapter = new DownloadPonyAdapter(PonyManager.this, R.layout.manager_item_pony, ponies);
				SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
				for(int i = 0; i < adapter.getCount(); i++){
                    DownloadPony p = adapter.getItem(i);
                    if(p.getState() == R.string.pony_state_not_installed){
                    	asynImageLoader.push(p.getFolder(), REMOTE_BASE_URL + p.getFolder() + "/preview.gif");
                    }else{
                    	adapter.getItem(i).setImage(BitmapFactory.decodeFile(new File(localFolder, p.getFolder() + "/preview.gif").getPath()));
                    }
        			long lastUpdateLocal = preferences.getLong("lastupdate_" + p.getFolder(), 0);
        			long lastUpdateRemote = p.getLastUpdate();
        			if((lastUpdateRemote > lastUpdateLocal) && p.getState() != R.string.pony_state_not_installed)
        				adapter.getItem(i).setState(R.string.pony_state_update);
				}
				asynImageLoader.start();
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {						
						setListAdapter(adapter);	
						updateTitle();
						setProgressbarVisibility(false);	
					}
				});
			}
		}).start();
		setProgressbarVisibility(true);
	}
	
	private ArrayList<DownloadPony> loadPonies(){
		ArrayList<DownloadPony> ponyList = new ArrayList<DownloadPony>();
		try {
			URL listFile = new URL(REMOTE_LIST_URL);
			URLConnection urlCon = listFile.openConnection();
			urlCon.setConnectTimeout(10000);
			urlCon.setReadTimeout(10000);
			BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {		 
				line = line.trim();
				if(line.startsWith("'"))
					continue;
				final String data[] = ToolSet.splitWithQualifiers(line, ",", "[", "]");
				if(data.length < 5)
					continue;
				File local = new File(localFolder, data[2]);
				int state = R.string.pony_state_not_installed;
				if(local.exists())
					state = R.string.pony_state_installed;
				DownloadPony p = new DownloadPony(data[0], data[2], Integer.valueOf(data[3].trim()), Long.valueOf(data[4].trim()), state);
				
				String[] categories;
				if(data[1].contains(","))
					categories = data[1].trim().split(",");
				else
					categories = new String[] {data[1].trim()};
				
				p.setCategories(Arrays.asList(categories));
				p.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ponytemp));
				p.setLastUpdate(Long.valueOf(data[4]));
				ponyList.add(p);
				count++;
			}			
		} catch (final Exception e) {
			runOnUiThread(new Runnable() {			
				@Override
				public void run() {	Toast.makeText(PonyManager.this, "Error loading Ponies\nPlease make sure you are connected to the internet\n" + e.getMessage(), Toast.LENGTH_LONG).show(); }
			});
			e.printStackTrace();
		} finally {
			// read local folders
			File[] subFolders = this.localFolder.listFiles(new FileFilter() {				
				@Override
				public boolean accept(File pathname) {
					if(pathname.isDirectory() == false) return false;
					File[] files = pathname.listFiles(new FileFilter() {						
						@Override
						public boolean accept(File pathname) {
							return pathname.getName().equalsIgnoreCase("pony.ini");
						}
					});
					return files.length > 0;
				}
			});
			
			for(File folder : subFolders){
				DownloadPony p = DownloadPony.fromINI(folder);
				if(ponyList.contains(p) == false){
					ponyList.add(p);
				}
			}
		}
//		Collections.sort(ponyList);
		return ponyList;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		DownloadPony p = adapter.getItem((int) info.id);
		Log.i("onCreateContextMenu", "info.id:"+ info.id + " info.postion:" + info.position);
		menu.setHeaderTitle(p.getName());
		//File preview = new File(localFolder, p.getFolder() + "/preview.gif");
		menu.setHeaderIcon(new BitmapDrawable(p.getImage()));
		if(p.getState() == R.string.pony_state_not_installed)
			menu.add(0, ACTION_INSTALL, 0, "Download");
		
		if(p.getState() == R.string.pony_state_update)
			menu.add(0, ACTION_INSTALL, 0, "Update");
		
		if(p.getState() == R.string.pony_state_installed || p.getState() == R.string.pony_state_update || p.getState() == R.string.pony_state_local_only)
			menu.add(0, ACTION_DELETE, 0, "Delete");
		

		menu.add(0, ACTION_REPORT, 0, "Report a problem");
		/*if(p.getState().equals(DownloadPony.STATE_RUNNING))
			menu.add(0, ACTION_STOP, 0, "Stop Download");*/
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		DownloadPony p = adapter.getItem((int) info.id);
		Log.i("onContextItemSelected", "info.id:"+ info.id + " info.postion:" + info.position);
		switch (item.getItemId()) {
		case ACTION_INSTALL:
			actionDownload(p);
			break;
		case ACTION_DELETE:			
			showAskForDeleteDialog(p);
			break;
		case ACTION_STOP:
			break;
		case ACTION_UPDATE:
			actionUpdate(p);
			break;
		case ACTION_REPORT:
			actionReport(p, null, null);
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	public void showAskForDeleteDialog(final DownloadPony p){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getString(R.string.manager_delete_title, p.getName()));
		dialog.setMessage(getString(R.string.manager_delete_message, p.getName()));
		dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				actionDelete(p);
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.no, null);
		dialog.show();
	}
	
	public void actionUpdate(DownloadPony p){
		actionDelete(p);
		actionDownload(p);		
	}
	
	public void actionDownload(DownloadPony p){
		new AsynFolderDownloader(REMOTE_BASE_URL + p.getFolder(), new File(localFolder, p.getFolder()), p.getFolder(), this).start();
		updateTitle();
	}
	
	public void actionReport(final DownloadPony p, String message, String mail){
		LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.pony_manager_report, null);
        final SharedPreferences preferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
    	final EditText editText = (EditText) dialogView.findViewById(R.id.editMessage);
    	final EditText editMail = (EditText) dialogView.findViewById(R.id.editMail);                	
    	editMail.setText(preferences.getString("report_mail", ""));
    	if(mail != null && mail.length() > 0)            	
        	editMail.setText(mail);
    	editText.setText(message);
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_email)
            .setTitle("Report a problem")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {                	
                	dialogView.invalidate();
                	if(editText.getText().length() == 0){
                		Toast.makeText(PonyManager.this, "Please enter a report text", Toast.LENGTH_LONG).show();
                		actionReport(p, editText.getText().toString(), editMail.getText().toString());
                	} else {
                		if(editMail.getText().length() > 0 && checkEmail(editMail.getText().toString()) == true){
                			Editor editor = preferences.edit();
                			editor.putString("report_mail", editMail.getText().toString());
                			editor.commit();
                    		sendReport(editText.getText().toString(), editMail.getText().toString(), p);
                		} else if(editMail.getText().length() > 0 && checkEmail(editMail.getText().toString()) == false){
                    		Toast.makeText(PonyManager.this, "Please enter a valid email", Toast.LENGTH_LONG).show();  
                    		actionReport(p, editText.getText().toString(), editMail.getText().toString());              			
                		} else {
                    		sendReport(editText.getText().toString(), editMail.getText().toString(), p);
                		}
                	}
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
	}
	
	public void sendReport(final String message, final String mail, final DownloadPony p){
		final ProgressDialog progressDialog;
		progressDialog = new ProgressDialog(PonyManager.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Sending...");
		progressDialog.setCancelable(false);
		Thread t = new Thread(new Runnable() {			
			@Override
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("http://android.ov3rk1ll.com/mlp/report.php");

			    try {
			        // Add your data
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("pony", p.getName()));
			        nameValuePairs.add(new BasicNameValuePair("lastupdate", String.valueOf(p.getLastUpdate())));
			        nameValuePairs.add(new BasicNameValuePair("message", message));
			        nameValuePairs.add(new BasicNameValuePair("mail", mail));
			        nameValuePairs.add(new BasicNameValuePair("phone_manufacturer", Build.MANUFACTURER));
			        nameValuePairs.add(new BasicNameValuePair("phone_model", Build.MODEL));
			        nameValuePairs.add(new BasicNameValuePair("phone_sdk", String.valueOf(Build.VERSION.SDK_INT)));
			        for (NameValuePair nameValuePair : nameValuePairs) {
						Log.i("nameValuePair", nameValuePair.getName() + ":" + nameValuePair.getValue());
					}
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			        
			        // Execute HTTP Post Request
			        httpclient.execute(httppost);
			        runOnUiThread(new Runnable() {						
						@Override
						public void run() {
					        progressDialog.hide();			
					        Toast.makeText(PonyManager.this, "Thank you for your report", Toast.LENGTH_SHORT).show();
						}
					});			        
			    } catch (ClientProtocolException e) {
			        // TODO Auto-generated catch block
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			    }
			}
		});
		progressDialog.show();
		t.start();		
	}
	
	public void actionDelete(DownloadPony p){
		SharedPreferences sharedPreferences = getSharedPreferences(MyLittleWallpaperService.TAG, MODE_PRIVATE);
		File folder = new File(localFolder, p.getFolder());
		File[] files = folder.listFiles();
		for(File f : files){
			f.delete();
		}
		folder.delete();
		// Remove to pony from the settings, this should trigger the Engine to remove to pony from activePony list
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove("usepony_" + p.getName());
		editor.putBoolean("changed_pony", true);
		editor.commit();
		if(p.getState() == R.string.pony_state_local_only){
			// Remove a local pony from the list since there is no remote data we could download
			adapter.remove(p);
		}else{
			p.setState(R.string.pony_state_not_installed);
		}
		adapter.notifyDataSetChanged();
		updateTitle();
	}
	
	private void showHelpDialog(){
		Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getString(R.string.manager_help));
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.overkill.live.pony" ,0);
			dialog.setTitle(getString(R.string.app_name) + " v" + pinfo.versionName);
		} catch (NameNotFoundException e) {
		}
		dialog.setIcon(android.R.drawable.ic_menu_help);
		dialog.setMessage(Html.fromHtml(getString(R.string.help_text), new ImageGetter() {				
			@Override
			public Drawable getDrawable(String source) {
				int resId = getResources().getIdentifier("drawable/" + source, null, getPackageName());
				Drawable d = getResources().getDrawable(resId);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		}, null));
		dialog.setPositiveButton(android.R.string.ok, null);
		dialog.setNegativeButton("Follow on Twitter", new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(LiveWallpaperSettings.URL_TWITTER));
				startActivity(i);	
				dialog.dismiss();
			}
		});
		dialog.setNeutralButton("Like on Facebook", new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(LiveWallpaperSettings.URL_FACEBOOK));
				startActivity(i);
				dialog.dismiss();
			}
		});
		dialog.show();			
	}
	
	public static boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	public static File selectForcedFolder(Context context, boolean internalStorage){
		SharedPreferences preferences = context.getSharedPreferences(MyLittleWallpaperService.SETTINGS_NAME, MODE_PRIVATE);
    	File folder = null;
    	if(internalStorage){
    		folder = new File(context.getFilesDir(), "ponies");    
    	}else{
    		folder = new File(Environment.getExternalStorageDirectory(), "ponies");
    	}
    	Editor editor = preferences.edit();
		editor.putString("localFolder", folder.getPath());
		editor.commit();
		PonyManager.createNoMedia(folder);
		return folder;
	}
	
    public static File selectFolder(Context context){    	
		SharedPreferences preferences = context.getSharedPreferences(MyLittleWallpaperService.SETTINGS_NAME, MODE_PRIVATE);
    	String path = preferences.getString("localFolder", null);
    	File folder = null;
    	if(path == null){ // Choose for the first time
    		if(PonyManager.isSDMounted())
    			folder = new File(Environment.getExternalStorageDirectory(), "ponies");
    		else
    			folder = new File(context.getFilesDir(), "ponies");    
    		Editor editor = preferences.edit();
    		editor.putString("localFolder", folder.getPath());
    		editor.commit();
    	}else{
    		folder = new File(path);
    	}

    	// create folder and place .nomedia file if needed
		PonyManager.createNoMedia(folder);
		Log.i("selectFolder", folder.getPath());
    	return folder;
    }
    
    public static void createNoMedia(File folder){
    	if(folder.isDirectory() == false){
    		folder.mkdir();
		}
    	File nomedia = new File(folder, ".nomedia");
    	if(nomedia.exists()) return;
		try {
			nomedia.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
}
}
