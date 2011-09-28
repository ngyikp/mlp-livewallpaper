package com.overkill.ponymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.R;
import com.overkill.ponymanager.AsynFolderDownloader.onDownloadListener;
import com.overkill.ponymanager.AsynImageLoader.onImageListener;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Window;
import android.widget.AdapterView;
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
	
	File localFolder;
	
	PonyAdapter adapter;
	
	int currentContextSelection = -1;
	
	@Override
	public void onDownloadStart(int position) {	
		if(position > adapter.getCount()) return;
		adapter.getItem(position).setState(R.string.pony_state_loading);
		adapter.getItem(position).setDoneFileCount(0);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}

	@Override
	public void onDownloadChanged(int position, int filesDone) {
		if(position > adapter.getCount()) return;
		adapter.getItem(position).setDoneFileCount(filesDone);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});		
	}

	@Override
	public void onDownloadDone(int position) {
		if(position > adapter.getCount()) return;
		adapter.getItem(position).setState(R.string.pony_state_installed);
		SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("lastupdate_" + adapter.getItem(position).getFolder(), adapter.getItem(position).getLastUpdate());
		editor.commit();
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
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
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, R.string.manager_help, 0, R.string.manager_help).setIcon(android.R.drawable.ic_menu_help);
		if(adapter.hasUpdate())
			menu.add(0, R.string.manager_update_all, 0, R.string.manager_update_all).setIcon(android.R.drawable.ic_menu_upload);
		return super.onPrepareOptionsMenu(menu);
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
		if(position > adapter.getCount()) return;
		adapter.getItem(position).setImage(image);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pony_manager);
		
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
		adapter = new PonyAdapter(this, R.layout.item_pony);
		setListAdapter(adapter);
		adapter.setNotifyOnChange(false);
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
				loadPonies();
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						adapter.notifyDataSetChanged();		
						setProgressBarIndeterminateVisibility(false);				
					}
				});
			}
		}).start();
		setProgressBarIndeterminateVisibility(true);
		
	}
	
	private boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	private void loadPonies(){
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
				final DownloadPony p = new DownloadPony(data[0], data[1], Integer.valueOf(data[2]), Integer.valueOf(data[3]), state);
				p.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ponytemp));
				p.setLastUpdate(Long.valueOf(data[4]));
				AsynImageLoader ail = new AsynImageLoader(REMOTE_BASE_URL + data[1] + "/preview.gif", count, this);
				adapter.add(p);
				ail.start();
				count++;
			}
		} catch (final Exception e) {
			runOnUiThread(new Runnable() {			
				@Override
				public void run() {	Toast.makeText(PonyManager.this, "Error loading Ponies\nPlease make sure you are connected to the internet", Toast.LENGTH_LONG).show(); }
			});
		}	
		SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
		for(int i = 0; i < adapter.getCount(); i++){
			DownloadPony p = adapter.getItem(i);
			long lastUpdateLocal = preferences.getLong("lastupdate_" + p.getFolder(), 0);
			long lastUpdateRemote = p.getLastUpdate();
			if((lastUpdateRemote > lastUpdateLocal) && p.getState() != R.string.pony_state_not_installed)
				adapter.getItem(i).setState(R.string.pony_state_update);
				
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		currentContextSelection = info.position;
		DownloadPony p = adapter.getItem(currentContextSelection);
		menu.setHeaderTitle(p.getName());
		if(p.getState() == R.string.pony_state_installed)
			menu.add(0, ACTION_DELETE, 0, "Delete");

		if(p.getState() == R.string.pony_state_not_installed)
			menu.add(0, ACTION_INSTALL, 0, "Download");
		
		if(p.getState() == R.string.pony_state_update)
			menu.add(0, ACTION_INSTALL, 0, "Update");
		
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
			actionDelete(p);
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
	
	public void actionUpdate(DownloadPony p){
		actionDelete(p);
		actionDownload(p);		
	}
	
	public void actionDownload(DownloadPony p){
		new AsynFolderDownloader(REMOTE_BASE_URL + p.getFolder(), new File(localFolder, p.getFolder()), currentContextSelection, this).start();
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
		adapter.notifyDataSetChanged();
		p.setState(R.string.pony_state_not_installed);
	}
}
