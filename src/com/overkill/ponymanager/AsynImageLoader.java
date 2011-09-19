package com.overkill.ponymanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AsynImageLoader extends Thread{
	
	public interface onImageListener{
		public void imageComplete(int posititon, Bitmap image);
	}
	
	private String path;
	private int position;
	private onImageListener listener;
	
	public AsynImageLoader(String path, int position, onImageListener listener){
		this.path = path;
		this.position = position;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		try {
			Bitmap b = BitmapFactory.decodeStream(new URL(this.path).openStream());
			this.listener.imageComplete(this.position, b);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
