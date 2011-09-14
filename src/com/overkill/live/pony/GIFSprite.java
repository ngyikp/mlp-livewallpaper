package com.overkill.live.pony;

import java.io.FileNotFoundException;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

public class GIFSprite {
	
	private String fileName;
	private GifDecoder gif;
	private int fps;
	private long frameTime;
	private int currentFrame;
	private int frameCount;
	private int spriteWidth;
	private int spriteHeight;
	
	private boolean initialized = false;
	
	/**
	 * Sets the file and initializes the GifDecoder
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public GIFSprite(String fileName) throws FileNotFoundException{
		this.fileName = fileName;
		gif = new GifDecoder();
		gif.init();		
	}

	/**
	 * Decodes the GIF File and stores data
	 */
	public void initialize(){
		long t0 = System.currentTimeMillis();
		try {
			Log.i("Sprite", "loading " + fileName);
			if(gif == null){
				gif = new GifDecoder();
				gif.init();
			}
			gif.read(MyLittleWallpaperService.assets.open(fileName));
			this.frameCount = gif.getFrameCount();
			if(this.frameCount > 1)
				this.fps = gif.getDelay(0);
			this.currentFrame = 0;
			this.frameTime = 0;
			this.spriteWidth = gif.width;
			this.spriteHeight = gif.height;
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
		if(globalTime > this.frameTime + this.fps ) {
            this.frameTime = globalTime;
            this.currentFrame +=1;
     
            if(this.currentFrame >= this.frameCount) {
                this.currentFrame = 0;
            }
        }		
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
			RectF dest = new RectF(temp.x, position.y, temp.x + this.spriteWidth, position.y + this.spriteHeight);	 
		    canvas.drawBitmap(this.gif.getFrame(this.currentFrame), null, dest, null);		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroys the GifDecoder to free memory
	 */
	public void recycle() {
		this.gif.recycle();
		this.initialized = false;
		this.gif = null;
	}


	
}
