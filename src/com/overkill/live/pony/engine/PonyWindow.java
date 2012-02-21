package com.overkill.live.pony.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class PonyWindow {
	private String ponyName;
	private String behaviorName = null;
	
	//private Sprite currentImage;
	
	// TODO replace old image with first frame (Bitmap object) of the gif
//	private Sprite oldImageLeft = null;
//	private Sprite oldImageRight = null;
	
	private Bitmap preloadImageLeft;
	private Sprite currentImageLeft;
	
	private Bitmap preloadImageRight;
	private Sprite currentImageRight;
	
	private int windowWidth;
	private int windowHeight;
	
	private Point position = new Point(0, 0);
	
//	private Point offset = new Point(0, 0);
	
	private boolean ponyDirection;
	public boolean shouldBeSleeping = false;
	
	private Thread preloadImageThread;
	
	private boolean visible = false;
	
	public PonyWindow(){
		// TODO ???
	}
	
	public PonyWindow(String ponyName){
		this.setPonyName(ponyName);
	}
	
	public synchronized void setImages(Sprite imageLeft, Sprite imageRight){
		if(imageLeft.isInitialized()){
			this.windowWidth = imageLeft.getSpriteWidth();
			this.windowHeight = imageLeft.getSpriteHeight();
		} else {
			preloadImageLeft = BitmapFactory.decodeFile(imageLeft.fileName);
			this.windowWidth = preloadImageLeft.getWidth();
			this.windowHeight = preloadImageLeft.getHeight();
		}
		this.currentImageLeft = imageLeft;
		
		if(imageRight.isInitialized()){
			this.windowWidth = imageRight.getSpriteWidth();
			this.windowHeight = imageRight.getSpriteHeight();
		} else {
			preloadImageRight = BitmapFactory.decodeFile(imageRight.fileName);
			this.windowWidth = preloadImageRight.getWidth();
			this.windowHeight = preloadImageRight.getHeight();
		}
		this.currentImageRight = imageRight;
		
		if(this.ponyDirection){
			preloadImageThread = new Thread(new Runnable() {				
				@Override
				public void run() {
					if(currentImageRight.isInitialized() == false){			
						currentImageRight.initialize("window");				
					}
					if(preloadImageRight != null) preloadImageRight.recycle();
					preloadImageRight = null;
					setVisible(true);
					if(currentImageLeft.isInitialized() == false){			
						currentImageLeft.initialize("window");				
					}
					if(preloadImageLeft != null) preloadImageLeft.recycle();
					preloadImageLeft = null;
				}
			});
		}else{
			preloadImageThread = new Thread(new Runnable() {				
				@Override
				public void run() {
					if(currentImageLeft.isInitialized() == false){			
						currentImageLeft.initialize("window");				
					}
					if(preloadImageLeft != null) preloadImageLeft.recycle();
					preloadImageLeft = null;
					setVisible(true);
					if(currentImageRight.isInitialized() == false){			
						currentImageRight.initialize("window");					
					}
					if(preloadImageRight != null) preloadImageRight.recycle();
					preloadImageRight = null;
				}
			});
		}
		preloadImageThread.start();
	}
	
	public Sprite getCurrentImage(){
		if(ponyDirection)
			return this.currentImageRight;
		else
			return this.currentImageLeft;
	}
	
	public Bitmap getPreloadImage(){
		if(ponyDirection)
			return this.preloadImageRight;
		else
			return this.preloadImageLeft;
	}
			
	public int getWidth(){
		return (int) (this.windowWidth * RenderEngine.CONFIG_SCALE);
	}
	
	public int getHeight(){
		return (int) (this.windowHeight * RenderEngine.CONFIG_SCALE);
	}
	
	public void update(long globalTime, String origin){
		if(this.getCurrentImage().isInitialized() == true){
			this.getCurrentImage().update(globalTime, "window-" + origin);
		}
	}
	
	public void draw(Canvas canvas){
		if(this.getCurrentImage().isInitialized() == true){
			this.getCurrentImage().draw(canvas, getLocation());
//			Log.i("PonyWindow[" + ponyName + "]", this.offset.toString());
//			Paint p = new Paint();
//			p.setColor(Color.RED);
//			canvas.drawCircle(this.position.x + RenderEngine.OFFSET, this.position.y, 10, p);
		}else {
			canvas.drawBitmap(getPreloadImage(), getLocation().x, getLocation().y, null);
		}
	}
	
//	public void setOffset(Point offset){
//		this.offset = offset;
//	}
//	
//	public void setOffset(int x, int y){
//		this.offset.x = x;
//		this.offset.y = y;
//	}
//	
//	public int getX(){
//		return this.position.x - offset.x;
//	}
//	
//	public int getY(){
//		return this.position.y - offset.y;
//	}
	
	public Point getLocation(){
		return this.position;
	}
	
//	public Point getOffsetLocation(){
//		return new Point(getX(), getY());
//	}
	
	public void setLocation(Point newPosition){
		this.position = newPosition;
//		this.position.offset(offset.x, offset.y);
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

	public void setPonyName(String ponyName) {
		this.ponyName = ponyName;
	}

	public String getPonyName() {
		return ponyName;
	}

	public boolean getPonyDirection() {
		return ponyDirection;
	}

	public void setPonyDirection(boolean ponyDirection) {
		this.ponyDirection = ponyDirection;
	}
	
//	public Rect testFrame(Point location){
//		Point point = new Point(location.x - offset.x, location.y - offset.y);
//		return new Rect(point.x, point.y, point.x + this.getWidth(), point.y + this.getHeight());		
//	}
//	
//	public Rect getFrame(){
//		return new Rect(getX(), getY(), getX() + this.getWidth(), getY() + this.getHeight());
//	}
}
