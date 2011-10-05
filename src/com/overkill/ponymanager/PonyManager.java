package com.overkill.ponymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.R;
import com.overkill.ponymanager.AsynFolderDownloader.onDownloadListener;
import com.overkill.ponymanager.AsynImageLoader.onImageListener;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PonyManager extends ListActivity implements onDownloadListener, onImageListener{
	public static final String TAG = "PonyManager";
	public static final String ACTION_REMOVE_PONY = "com.overkill.live.pony.action.removed";
	
	public static final String REMOTE_BASE_URL = "http://mlp-livewallpaper.googlecode.com/svn/assets/";
	public static final int ACTION_INSTALL = 1;
	public static final int ACTION_DELETE = 2;
	public static final int ACTION_STOP = 3;
	public static final int ACTION_UPDATE = 4;
	
	public static final int THEMES[] = {R.style.Theme_Pony_Applejack, R.style.Theme_Pony_Rainbowdash, R.style.Theme_Pony_Fluttershy, R.style.Theme_Pony_PinkiePie, R.style.Theme_Pony_Rarity, R.style.Theme_Pony_TwilightSparkle};
	
	File localFolder;
	
	PonyAdapter adapter;
	
	int currentContextSelection = -1;
	
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
	 
	public void updateTitle(){
		setDownloadAllVisibility(false);
		setUpdateAllVisibility(false);
		setSetButtonVisibility(false);
		if(adapter.hasUpdate())
			setUpdateAllVisibility(true);
		if(adapter.hasNotInstalled())
			setDownloadAllVisibility(true);
		WallpaperInfo currentWallpaper = WallpaperManager.getInstance(this).getWallpaperInfo();
		if(currentWallpaper == null){
			setSetButtonVisibility(true);
		}else{
			if(currentWallpaper.getPackageName().equals("com.overkill.live.pony") == false){
				setSetButtonVisibility(true);				
			}
		}
	}
	 
	@Override
	public void onDownloadStart(int position) {	
		if(position >= adapter.getCount()) return;
		adapter.getItem(position).setState(R.string.pony_state_loading);
		adapter.getItem(position).setDoneFileCount(0);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}

	@Override
	public void onDownloadChanged(int position, int filesDone) {
		if(position >= adapter.getCount()) return;
		adapter.getItem(position).setDoneFileCount(filesDone);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});		
	}

	@Override
	public void onDownloadDone(int position) {
		if(position >= adapter.getCount()) return;
		adapter.getItem(position).setState(R.string.pony_state_installed);
		SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("lastupdate_" + adapter.getItem(position).getFolder(), adapter.getItem(position).getLastUpdate());
		editor.commit();
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	updateTitle();}
		});			
	}

	@Override
	public void onDownloadError(final int position, final String error) {
		if(position > adapter.getCount()) return;
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				adapter.getItem(position).setState(R.string.pony_state_not_installed);				
				Toast.makeText(PonyManager.this, "Error while downloading \"" + adapter.getItem(position).getName() + "\"\n" + error, Toast.LENGTH_LONG).show();
				adapter.notifyDataSetChanged();
				
				// clean up already finished files
				File folder = new File(localFolder, adapter.getItem(position).getFolder());
				File[] files = folder.listFiles();
				for(File f : files){
					f.delete();
				}
				folder.delete();
			}
		});
	}
	
	public void onTitleClick(View view){
		switch (view.getId()) {
		case R.id.btn_title_help:	
			Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.manager_help);
			dialog.setIcon(android.R.drawable.ic_menu_help);
			dialog.setMessage(Html.fromHtml(getString(R.string.help_text), new ImageGetter() {				
				@Override
				public Drawable getDrawable(String source) {
					int resId = getResources().getIdentifier("drawable/" + source, null, getPackageName());
					Drawable d = getResources().getDrawable(resId);
					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					Log.i("ImageGetter", source + " > " + resId);
					return d;
				}
			}, null));
			dialog.setPositiveButton(android.R.string.ok, null);
			dialog.show();			
			break;
		case R.string.manager_update_all:
			for(int i = 0; i < adapter.getCount(); i++){
				DownloadPony p = adapter.getItem(i);
				if(p.getState() == R.string.pony_state_update)
					actionUpdate(p);
			}
			updateTitle();
			break;
		case R.id.btn_title_download_all:	
			for(int i = 0; i < adapter.getCount(); i++){
				DownloadPony p = adapter.getItem(i);
				Log.i("manager_download_all", "download " + p.getName() + "? " +  (p.getState() == R.string.pony_state_not_installed));
				if(p.getState() == R.string.pony_state_not_installed)
					actionDownload(p);
			}
			updateTitle();
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
	public void imageError(int position, final String error) {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	Toast.makeText(PonyManager.this, error, Toast.LENGTH_LONG).show(); }
		});	
	}
	
	@Override
	public void imageComplete(int position, Bitmap image) {
		if(position >= adapter.getCount()) return;
		adapter.getItem(position).setImage(image);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
	    Random r = new Random();
	    setTheme(THEMES[r.nextInt(THEMES.length)]);
		super.onCreate(savedInstanceState);      
	    setContentView(R.layout.pony_manager);	
	    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.system_title);
		if(isSDMounted())
			localFolder = new File(Environment.getExternalStorageDirectory(), "ponies");
		else
			localFolder = new File(getFilesDir(), "ponies");
		
		if(localFolder.isDirectory() == false){
			localFolder.mkdir();
			File nomedia = new File(localFolder, ".nomedia");
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				getListView().showContextMenuForChild(arg1);				
			}			
		});
		new Thread(new Runnable() {			
			@Override
			public void run() {
				final ArrayList<DownloadPony> ponies = loadPonies();
				adapter = new PonyAdapter(PonyManager.this, R.layout.item_pony, ponies);
				SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
				for(int i = 0; i < adapter.getCount(); i++){
                    DownloadPony p = adapter.getItem(i);
					AsynImageLoader ail = new AsynImageLoader(REMOTE_BASE_URL + p.getFolder() + "/preview.gif", i, PonyManager.this);
                    ail.start();
        			long lastUpdateLocal = preferences.getLong("lastupdate_" + p.getFolder(), 0);
        			long lastUpdateRemote = p.getLastUpdate();
        			if((lastUpdateRemote > lastUpdateLocal) && p.getState() != R.string.pony_state_not_installed)
        				adapter.getItem(i).setState(R.string.pony_state_update);
				}
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
	
	public static boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	private ArrayList<DownloadPony> loadPonies(){
		ArrayList<DownloadPony> r = new ArrayList<DownloadPony>();
		try {
			URL listFile = new URL(REMOTE_BASE_URL + "ponies.lst");
			URLConnection urlCon = listFile.openConnection();
			urlCon.setConnectTimeout(5000);
			urlCon.setReadTimeout(5000);
			BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {		 
				line = line.trim();
				if(line.startsWith("'"))
					continue;
				final String data[] = line.split(",");
				if(data.length < 5)
					continue;
				File local = new File(localFolder, data[1]);
				int state = R.string.pony_state_not_installed;
				if(local.exists())
					state = R.string.pony_state_installed;
				DownloadPony p = new DownloadPony(data[0], data[1], Integer.valueOf(data[2]), Integer.valueOf(data[3]), state);
				p.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ponytemp));
				p.setLastUpdate(Long.valueOf(data[4]));
				r.add(p);
				count++;
			}
		} catch (final Exception e) {
			runOnUiThread(new Runnable() {			
				@Override
				public void run() {	Toast.makeText(PonyManager.this, "Error loading Ponies\nPlease make sure you are connected to the internet", Toast.LENGTH_LONG).show(); }
			});
		}	
		return r;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		currentContextSelection = info.position;
		DownloadPony p = adapter.getItem(currentContextSelection);
		menu.setHeaderTitle(p.getName());
		File preview = new File(localFolder, p.getFolder() + "/preview.gif");
		menu.setHeaderIcon(BitmapDrawable.createFromPath(preview.getPath()));
		if(p.getState() == R.string.pony_state_not_installed)
			menu.add(0, ACTION_INSTALL, 0, "Download");
		
		if(p.getState() == R.string.pony_state_update)
			menu.add(0, ACTION_INSTALL, 0, "Update");
		
		if(p.getState() == R.string.pony_state_installed || p.getState() == R.string.pony_state_update)
			menu.add(0, ACTION_DELETE, 0, "Delete");
		
		/*if(p.getState().equals(DownloadPony.STATE_RUNNING))
			menu.add(0, ACTION_STOP, 0, "Stop Download");*/
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(currentContextSelection > adapter.getCount() || currentContextSelection < 0) return false;
		DownloadPony p = adapter.getItem(currentContextSelection);
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
		int index = adapter.getPosition(p);
		new AsynFolderDownloader(REMOTE_BASE_URL + p.getFolder(), new File(localFolder, p.getFolder()), index, this).start();
		updateTitle();
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
		p.setState(R.string.pony_state_not_installed);
		adapter.notifyDataSetChanged();
		updateTitle();
	}
}
