package com.overkill.live.pony;

import java.io.FileNotFoundException;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.PointF;
import android.util.Log;

public class Sprite {
	
	private String fileName;
	private Movie gif;
	private int duration;
	private int spriteWidth;
	private int spriteHeight;
	
	private boolean initialized = false;
	
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
			Log.i("Sprite", "loading " + fileName);
			this.gif = Movie.decodeStream(MyLittleWallpaperService.assets.open(fileName));
			this.duration = gif.duration();
			this.duration = Math.max(this.duration, 1);
			this.spriteWidth = gif.width();
			this.spriteHeight = gif.height();
			this.initialized = true;
			Log.i("Sprite", "took " + (System.currentTimeMillis() - t0) + " ms to load");
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
		return this.spriteHeight;
	}
	
	/**
	 * Returns the width to the GIF
	 * @return width to the GIF
	 */
	public float getSpriteWidth() {
		if(!initialized) this.initialize();
		return this.spriteWidth;
	}
	
	/**
	 * Updates the current frame for the given time
	 * @param globalTime The time to find the frame for
	 */
	public void update(long globalTime) {
		if(!initialized) this.initialize();
		int pos = (int)(globalTime % this.duration);
        this.gif.setTime(pos);
	}

	/**
	 * Draws the GIF frame on the canvas at the given position
	 * @param canvas The {@link Canvas} to draw on
	 * @param position The position on the canvas
	 */
	public void draw(Canvas canvas, PointF position) {
		if(canvas == null || position == null) return;
		if(!initialized) this.initialize();
		try{
			PointF temp = new PointF(position.x, position.y);
			temp.x += MyLittleWallpaperService.offset;
			this.gif.draw(canvas, temp.x, temp.y);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroys the GifDecoder to free memory
	 */
	public void recycle() {
		this.initialized = false;
		this.gif = null;
	}


	
}
