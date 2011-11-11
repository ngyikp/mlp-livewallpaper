package com.overkill.live.pony.engine;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

public class PonyWindow {
	private String ponyName;
	private String behaviorName = null;
	
	//private Sprite currentImage;
	
	private Sprite oldImageLeft = null;
	private Sprite oldImageRight = null;
	
	private Sprite currentImageLeft;
	private Sprite currentImageRight;
	
	private int spriteWidth;
	private int spriteHeight;
	
	private Point position = new Point(0, 0);
	
	public boolean ponyDirection;
	public boolean shouldBeSleeping = false;
	
	private Thread preloadImageLeftFirst;
	private Thread preloadImageRightFirst;
	
	private boolean visible = false;
	
	public PonyWindow(){
		// TODO ???
	}
	
	public PonyWindow(String ponyName){
		this.ponyName = ponyName;
	}
	
	public void setImages(Sprite imageLeft, Sprite imageRight){
		if(currentImageLeft != null){
			oldImageLeft = currentImageLeft;
		}
		if(imageLeft.isInitialized()){
			this.spriteWidth = imageLeft.getSpriteWidth();
			this.spriteHeight = imageLeft.getSpriteHeight();
		} else {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imageLeft.fileName, opts);
			this.spriteWidth = opts.outWidth;
			this.spriteHeight = opts.outHeight;
		}
		this.currentImageLeft = imageLeft;
		
		if(currentImageRight != null){
			oldImageRight = currentImageRight;
		}
		if(imageRight.isInitialized()){
			this.spriteWidth = imageRight.getSpriteWidth();
			this.spriteHeight = imageRight.getSpriteHeight();
		} else {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imageRight.fileName, opts);
			this.spriteWidth = opts.outWidth;
			this.spriteHeight = opts.outHeight;
		}
		this.currentImageRight = imageRight;
		preloadImageLeftFirst = new Thread(new Runnable() {				
			@Override
			public void run() {
				if(currentImageLeft.isInitialized() == false){			
					currentImageLeft.initialize("window");				
				}
				setVisible(true);
				if(currentImageRight.isInitialized() == false){			
					currentImageRight.initialize("window");					
				}
				oldImageLeft = null;
				oldImageRight = null;
			}
		});
		preloadImageRightFirst = new Thread(new Runnable() {				
			@Override
			public void run() {
				if(currentImageRight.isInitialized() == false){			
					currentImageRight.initialize("window");				
				}
				setVisible(true);
				if(currentImageLeft.isInitialized() == false){			
					currentImageLeft.initialize("window");				
				}
				oldImageLeft = null;
				oldImageRight = null;
			}
		});
		if(this.ponyDirection){
			preloadImageRightFirst.start();
		}else{
			preloadImageLeftFirst.start();
		}
	}
	
	public Sprite getCurrentImage(){
		if(ponyDirection)
			return this.currentImageRight;
		else
			return this.currentImageLeft;
	}
	
	public Sprite getOldImage(){
		if(ponyDirection)
			return this.oldImageRight;
		else
			return this.oldImageLeft;
	}
		
	public int getSpriteWidth(){
		return (int) (this.spriteWidth * RenderEngine.CONFIG_SCALE);
	}
	
	public int getSpriteHeight(){
		return (int) (this.spriteHeight * RenderEngine.CONFIG_SCALE);
	}
	
	public void update(long globalTime, String origin){
		if(this.getOldImage() != null){
			this.getOldImage().update(globalTime, "oldwindow-" + origin);
		}else if(this.isVisible()){
			this.getCurrentImage().update(globalTime, "window-" + origin);
		}
	}
	
	public void draw(Canvas canvas){
		if(this.getOldImage() != null){
			this.getOldImage().draw(canvas, position);
		}else if(this.isVisible()){
			this.getCurrentImage().draw(canvas, position);
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

	public String getBehaviorName() {
		return behaviorName;
	}

	public void setBehaviorName(String behaviorName) {
		this.behaviorName = behaviorName;
	}
}
