package com.overkill.ponymanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AsynImageLoader extends Thread{
	
	public interface onImageListener{
		public void imageComplete(String ID, Bitmap image);
		public void imageError(String ID, String error);
	}
	
	private String path;
	private String ID;
	private onImageListener listener;
	
	public AsynImageLoader(String path, String ID, onImageListener listener){
		this.path = path;
		this.ID = ID;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		try {
			Bitmap b = BitmapFactory.decodeStream(new URL(this.path).openStream());
			this.listener.imageComplete(this.ID, b);
		} catch (MalformedURLException e) {
			this.listener.imageError(this.ID, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			this.listener.imageError(this.ID, e.getMessage());
			e.printStackTrace();
		}
		
	}

}
