package com.overkill.ponymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.overkill.live.pony.R;
import com.overkill.ponymanager.AsynFolderDownloader.onDownloadListener;
import com.overkill.ponymanager.AsynImageLoader.onImageListener;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PonyManager extends ListActivity implements onDownloadListener, onImageListener{
	public static final String REMOTE_BASE_URL = "http://mlp-livewallpaper.googlecode.com/svn/assets/";
	public static final int ACTION_INSTALL = 1;
	public static final int ACTION_DELETE = 2;
	public static final int ACTION_STOP = 3;
	
	File localFolder;
	
	PonyAdapter adapter;
	
	int currentContextSelection = -1;
	
	@Override
	public void onDownloadStart(int position) {	
		adapter.getItem(position).setState(DownloadPony.STATE_RUNNING);
		adapter.getItem(position).setDoneFileCount(0);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}

	@Override
	public void onDownloadChanged(int position, int filesDone) {
		adapter.getItem(position).setDoneFileCount(filesDone);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});		
	}

	@Override
	public void onDownloadDone(int position) {
		adapter.getItem(position).setState(DownloadPony.STATE_INSTALLED);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});			
	}

	@Override
	public void onDownloadError(String error) {
		Log.i("PonyManager", "Download error " + error);			
	}
	
	@Override
	public void imageComplete(int posititon, Bitmap image) {
		adapter.getItem(posititon).setImage(image);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {	adapter.notifyDataSetChanged();	}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		adapter.setNotifyOnChange(false);
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				getListView().showContextMenuForChild(arg1);				
			}			
		});
		setListAdapter(adapter);
		new Thread(new Runnable() {			
			@Override
			public void run() {
				loadPonies();
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						adapter.notifyDataSetChanged();						
					}
				});
			}
		}).start();
		
	}
	
	private boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	private void loadPonies(){
		try {
			URL listFile = new URL(REMOTE_BASE_URL + "ponies.lst");
			BufferedReader br = new BufferedReader(new InputStreamReader(listFile.openStream()));
			String line = "";
			while ((line = br.readLine()) != null) {		 
				line = line.trim();
				if(line.startsWith("'"))
					continue;
				String data[] = line.split(",");
				File local = new File(localFolder, data[1]);
				String state = DownloadPony.STATE_NOT_INSTALLED;
				if(local.exists())
					state = DownloadPony.STATE_INSTALLED;
				DownloadPony p = new DownloadPony(data[0], data[1], Integer.valueOf(data[2]), Integer.valueOf(data[3]), state);
				p.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ponytemp));
				new AsynImageLoader(REMOTE_BASE_URL + data[1] + "/preview.gif", adapter.getCount(), this).start();
				adapter.add(p);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		currentContextSelection = info.position;
		DownloadPony p = adapter.getItem(currentContextSelection);
		if(p.getState().equals(DownloadPony.STATE_INSTALLED))
			menu.add(0, ACTION_DELETE, 0, "Delete");

		if(p.getState().equals(DownloadPony.STATE_NOT_INSTALLED))
			menu.add(0, ACTION_INSTALL, 0, "Download");
		
		/*if(p.getState().equals(DownloadPony.STATE_RUNNING))
			menu.add(0, ACTION_STOP, 0, "Stop Download");*/
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		DownloadPony p = adapter.getItem(currentContextSelection);
		switch (item.getItemId()) {
		case ACTION_INSTALL:
			new AsynFolderDownloader(REMOTE_BASE_URL + p.getFolder(), new File(localFolder, p.getFolder()), currentContextSelection, this).start();
			break;
		case ACTION_DELETE:
			File folder = new File(localFolder, p.getFolder());
			File[] files = folder.listFiles();
			for(File f : files){
				f.delete();
			}
			folder.delete();
			p.setState(DownloadPony.STATE_NOT_INSTALLED);
			adapter.notifyDataSetChanged();
			break;
		case ACTION_STOP:
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
}
