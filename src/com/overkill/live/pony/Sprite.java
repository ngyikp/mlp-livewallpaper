package com.overkill.live.pony;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class Sprite {
	
	private String fileName;
	private GifDecoder gif;
	private int spriteWidth;
	private int spriteHeight;
	private int frameCount = 0;
	private long lastFrameTime = 0;
	private int currentFrame = 0;	
	
	private boolean initialized = false;
	private Paint renderPaint = new Paint();
	
	/**
	 * Sets the file and initializes the GifDecoder
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public Sprite(String fileName){
		this(fileName, false);	
	}
	
	public Sprite(String fileName, boolean initializeGIF){
		this.fileName = fileName;	
		if(initializeGIF) this.initialize();
	}

	/**
	 * Decodes the GIF File and stores data
	 */
	public void initialize(){
		long t0 = System.currentTimeMillis();
		try {
			GifDecoder decoder = new GifDecoder();
			// Log.i("GifDecoder.read", this.fileName);
			decoder.read(new FileInputStream(this.fileName));
			this.spriteWidth = decoder.width;
			this.spriteHeight = decoder.height;		
			this.frameCount = decoder.getFrameCount();
			this.gif = decoder;
			this.initialized = true;
			if(MyLittleWallpaperService.DEBUG) Log.i("Sprite", "took " + (System.currentTimeMillis() - t0) + " ms to load " + fileName + " needs " + ToolSet.formatBytes(this.spriteWidth*this.spriteHeight*this.frameCount*4));
		} catch (OutOfMemoryError e) {
			this.initialized = false;
		} catch (FileNotFoundException e) {
			this.initialized = false;
		}		
	}
	
	/**
	 * Returns the height to the GIF
	 * @return height to the GIF
	 */
	public int getSpriteHeight() {
		if(!initialized) this.initialize();
		return (int) (this.spriteHeight * RenderEngine.CONFIG_SCALE);
	}
	
	/**
	 * Returns the width to the GIF
	 * @return width to the GIF
	 */
	public int getSpriteWidth() {
		if(!initialized) this.initialize();
		return (int) (this.spriteWidth * RenderEngine.CONFIG_SCALE);
	}
	
	/**
	 * Updates the current frame for the given time
	 * @param globalTime The time to find the frame for
	 */
	public void update(long globalTime) {
		if(!this.initialized) this.initialize();
		if (globalTime > this.lastFrameTime + this.gif.getDelay(currentFrame)) {
			this.lastFrameTime = globalTime;
			this.currentFrame++;
			if (this.currentFrame >= this.frameCount) {
				this.currentFrame = 0;
			}
		}
	}

	/**
	 * Draws the GIF frame on the canvas at the given position
	 * @param canvas The {@link Canvas} to draw on
	 * @param position The position on the canvas
	 */
	public void draw(Canvas canvas, Point position) {
		if(canvas == null || position == null) return;
		if(!this.initialized) this.initialize();

		Point realPosition = new Point(position.x + RenderEngine.OFFSET, position.y);
		canvas.drawBitmap(this.gif.getFrame(currentFrame), null, new Rect(realPosition.x, realPosition.y, realPosition.x + this.getSpriteWidth(), realPosition.y + this.getSpriteHeight()), renderPaint);
	}
	
	/**
	 * releases and cleans up objects
	 */
	public void destroy(){
		//Log.i("Sprite.destroy", this.fileName);
		if(this.gif != null) this.gif.destroy();
		this.gif = null;
		this.initialized = false;
	}
}
