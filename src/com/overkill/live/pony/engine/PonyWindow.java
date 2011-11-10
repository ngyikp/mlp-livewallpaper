package com.overkill.live.pony.engine;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

public class PonyWindow {
	private String ponyName;
	
	private Sprite currentImage;
	private Sprite oldImage = null;
	
	private Sprite leftImage;
	private Sprite rightImage;
	
	private int spriteWidth;
	private int spriteHeight;
	
	private Point position = new Point(0, 0);
	
	public boolean ponyDirection;
	public boolean shouldBeSleeping = false;
	
	private Thread preloadImage;
	
	private boolean visible = false;
	
	public PonyWindow(){
		// TODO ???
	}
	
	public PonyWindow(String ponyName){
		this.ponyName = ponyName;
	}
	
	public void setImage(Sprite image){
		if(currentImage != null){
			oldImage = currentImage;
		}
		if(image.isInitialized()){
			this.spriteWidth = image.getSpriteWidth();
			this.spriteHeight = image.getSpriteHeight();
		} else {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(image.fileName, opts);
			this.spriteWidth = opts.outWidth;
			this.spriteHeight = opts.outHeight;
		}
		this.currentImage = image;
		preloadImage = new Thread(new Runnable() {				
			@Override
			public void run() {
				if(currentImage.isInitialized() == false){			
					boolean b = currentImage.initialize("window");
					Log.i("PonyWindow[" + ponyName + "]", "preload " + currentImage.fileName + " -> " + b);					
				}
				setVisible(true);
				oldImage = null;
			}
		});
		preloadImage.start();
	}
	
	public Sprite getCurrentImage(){
		return this.currentImage;
	}
		
	public int getSpriteWidth(){
		return (int) (this.spriteWidth * RenderEngine.CONFIG_SCALE);
	}
	
	public int getSpriteHeight(){
		return (int) (this.spriteHeight * RenderEngine.CONFIG_SCALE);
	}
	
	public void update(long globalTime, String origin){
		if(this.oldImage != null){
			this.oldImage.update(globalTime, "oldwindow-" + origin);
		}else if(this.isVisible()){
			this.currentImage.update(globalTime, "window-" + origin);
		}
	}
	
	public void draw(Canvas canvas){
		if(this.oldImage != null){
			this.oldImage.draw(canvas, position);
		}else if(this.isVisible()){
			this.currentImage.draw(canvas, position);
		}
	}
	
	public Point getLocation(){
		return this.position;
	}
	
	public void setLocation(Point newPosition){
		this.position = newPosition;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
