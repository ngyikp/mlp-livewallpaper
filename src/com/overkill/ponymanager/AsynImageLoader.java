package com.overkill.ponymanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class AsynImageLoader extends Thread{
	
	public interface onImageListener{
		public void imageComplete(String ID, Bitmap image);
		public void imageError(String ID, String error);
	}

	private static final long MAX_CACHE_AGE = 5 * 24 * 3600 * 1000; // 5 days
	
	private HashMap<String, String> images;
	private Context context;
	private onImageListener listener;
	
	public AsynImageLoader(onImageListener listener, Context context){
		this.images = new HashMap<String, String>();
		this.listener = listener;
		this.context = context;
	}
	
	public void push(String ID, String path){
		images.put(ID, path);
	}
	
	@Override
	public void run() {
		Iterator<String> imageIterator = this.images.keySet().iterator();
		while(imageIterator.hasNext()){
			String ID = imageIterator.next();
			String path = this.images.get(ID);			
			File remoteImage = new File(path);
			try {
				Bitmap b = getFromCache(remoteImage);
				if(b == null){
					b = BitmapFactory.decodeStream(new URL(path).openStream());
					saveToCache(b, remoteImage);
				}
				this.listener.imageComplete(ID, b);
			} catch (MalformedURLException e) {
				this.listener.imageError(ID, e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				this.listener.imageError(ID, e.getMessage());
				e.printStackTrace();
			} finally {
			}
		}	
		this.images.clear();
	}

	private Bitmap getFromCache(File file){
		File cachedFile = getCachedFile(file);
		if(cachedFile.exists() && getFileAge(cachedFile) < MAX_CACHE_AGE){
			return BitmapFactory.decodeFile(cachedFile.getPath());
		}
		return null;
	}
	
	private boolean saveToCache(Bitmap bitmap, File file){
		try {
			File cachedFile = getCachedFile(file);
			bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(cachedFile));
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private long getFileAge(File file){
		return (new Date().getTime())-file.lastModified();
	}
	
	private File getCachedFile(File file){
		String fileName = file.getParentFile().getName() + "_" + file.getName();
		File folder = new File(this.context.getCacheDir(), "preview");
		if(folder.isDirectory() == false){
			folder.mkdir();
		}
		return new File(folder, fileName);		
	}
}
