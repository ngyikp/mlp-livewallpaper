package com.overkill.live.pony;

import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.util.Log;

public class Sprite {
	
	private String fileName;
	private Movie gif;
	private int duration;
	private int spriteWidth;
	private int spriteHeight;
	private int lastDrawnFrame = -1;
	
	private Bitmap cacheBitmap;
	private Canvas cacheCanvas;
	
	private boolean initialized = false;
	
	private boolean frameInCache = false;
	
	/**
	 * Sets the file and initializes the GifDecoder
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public Sprite(String fileName) throws FileNotFoundException{
		this.fileName = fileName;
	}

	/**
	 * Decodes the GIF File and stores data
	 */
	public void initialize(){
		long t0 = System.currentTimeMillis();
		try {
			this.gif = Movie.decodeStream(MyLittleWallpaperService.assets.open(fileName));
			this.duration = gif.duration();
			this.duration = Math.max(this.duration, 1);
			this.spriteWidth = gif.width();
			this.spriteHeight = gif.height();		
			cacheBitmap = Bitmap.createBitmap(this.spriteWidth, this.spriteHeight, Config.ARGB_4444);
	        cacheCanvas = new Canvas();
	        cacheCanvas.setBitmap(cacheBitmap);
			this.initialized = true;
			if(MyLittleWallpaperService.DEBUG_RENDERTIME) Log.i("Sprite", "took " + (System.currentTimeMillis() - t0) + " ms to load");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Returns the height to the GIF
	 * @return height to the GIF
	 */
	public int getSpriteHeight() {
		if(!initialized) this.initialize();
		return (int) (this.spriteHeight * MyLittleWallpaperService.SCALE);
	}
	
	/**
	 * Returns the width to the GIF
	 * @return width to the GIF
	 */
	public int getSpriteWidth() {
		if(!initialized) this.initialize();
		return (int) (this.spriteWidth * MyLittleWallpaperService.SCALE);
	}
	
	/**
	 * Updates the current frame for the given time
	 * @param globalTime The time to find the frame for
	 */
	public void update(long globalTime) {
		if(!initialized) this.initialize();
		int pos  = (int)(globalTime % this.duration);
        this.gif.setTime(pos);
        if(pos == lastDrawnFrame)
        	frameInCache = true;
        else
        	frameInCache = false;
        lastDrawnFrame = pos;
	}

	/**
	 * Draws the GIF frame on the canvas at the given position
	 * @param canvas The {@link Canvas} to draw on
	 * @param position The position on the canvas
	 */
	public void draw(Canvas canvas, PointF position) {
		if(canvas == null || position == null) return;
		if(!initialized) this.initialize();

		PointF realPosition = new PointF(position.x + MyLittleWallpaperService.offset, position.y);
		// only resize Bitmap if we need to
		if(MyLittleWallpaperService.SCALE != 1.0f){
			// if we need to draw a new frame
			if(frameInCache == false){
				cacheCanvas.drawColor(0, Mode.CLEAR);
				this.gif.draw(cacheCanvas, 0, 0);
			}	            
			canvas.drawBitmap(cacheBitmap, null, new RectF(realPosition.x, realPosition.y, realPosition.x + this.getSpriteWidth(), realPosition.y + this.getSpriteHeight()), null);
		}else{
			this.gif.draw(canvas, realPosition.x, realPosition.y);
		}
	}

	/**
	 * Destroys the Movie to free memory
	 */
	public void recycle() {
		this.initialized = false;
		this.gif = null;
	}	
}
