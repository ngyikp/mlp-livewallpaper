package com.overkill.live.pony.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.ToolSet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class Sprite {
	public static Paint debugPaint = new Paint();
	//private boolean loading = false;
	public String fileName;
	
	private GifDecoder gif = null;
	private Bitmap staticImage;
	private boolean isAnimated = true;
	
	private BitmapFactory.Options opts = new BitmapFactory.Options();
	
	private int spriteWidth = 0;
	private int spriteHeight = 0;
	private int frameCount = 0;
	private long lastFrameTime = 0;
	private int currentFrame = 0;	
	
	private boolean initialized = false;
	
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
		if(initializeGIF) this.initialize("constructor");
		debugPaint.setStyle(Style.STROKE);
		debugPaint.setColor(0xffffffff);
		opts.inScaled = false;
	}

	/**
	 * Decodes the GIF File and stores data
	 */
	public boolean initialize(String reason){
//		if(this.loading) return;
//		this.loading = true;
		long t0 = System.currentTimeMillis();
		try {
			if(fileName.endsWith(".gif")){
				GifDecoder decoder = new GifDecoder();
				decoder.read(new FileInputStream(this.fileName));
				this.spriteWidth = decoder.width;
				this.spriteHeight = decoder.height;		
				this.frameCount = decoder.getFrameCount();
				this.gif = decoder;
				this.isAnimated = true;
			}else{
				this.staticImage = BitmapFactory.decodeFile(fileName, opts);
				this.spriteWidth = this.staticImage.getWidth();
				this.spriteHeight = this.staticImage.getHeight();
				this.isAnimated = false;
			}
//			if(MyLittleWallpaperService.DEBUG)
				Log.i("Sprite[" + reason + "]", "took " + (System.currentTimeMillis() - t0) + " ms to load " + fileName + " needs " + ToolSet.formatBytes(this.spriteWidth*this.spriteHeight*this.frameCount*2));

			this.initialized = true;
		} catch (OutOfMemoryError e) {
			Log.i("Sprite", fileName + " Error: " + e.getMessage());
			this.initialized = false;
		} catch (FileNotFoundException e) {
			Log.i("Sprite", fileName + " Error: " + e.getMessage());
			this.initialized = false;
		}
		return this.initialized;
	}
	
	/**
	 * Returns the height to the GIF
	 * @return height to the GIF
	 */
	public int getSpriteHeight() {
		if(!initialized) this.initialize("getSpriteHeight");
		return (int) (this.spriteHeight * RenderEngine.CONFIG_SCALE);
	}
	
	/**
	 * Returns the width to the GIF
	 * @return width to the GIF
	 */
	public int getSpriteWidth() {
		if(!initialized) this.initialize("getSpriteWidth");
		return (int) (this.spriteWidth * RenderEngine.CONFIG_SCALE);
	}
	
	
	/**
	 * Updates the current frame for the given time
	 * @param globalTime The time to find the frame for
	 */
	public void update(long globalTime, String origin) {
		if(!this.initialized) this.initialize("update-" + origin);
		if(!this.isAnimated) return; // No need to update frames for a static image
		if(this.gif == null) { RenderEngine.suggestRestart(); return; }
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
		//while(loading);
		if(!this.initialized) this.initialize("draw");

		Point realPosition = new Point(position.x + RenderEngine.OFFSET, position.y);
		
		if(this.isAnimated){
			if(this.gif == null) return;
			canvas.drawBitmap(this.gif.getFrame(currentFrame), null,
					new Rect(realPosition.x, realPosition.y, realPosition.x + this.getSpriteWidth(), realPosition.y + this.getSpriteHeight()), null);
		} else {
			if(this.staticImage == null) return;
			canvas.drawBitmap(this.staticImage, null,
					new Rect(realPosition.x, realPosition.y, realPosition.x + this.getSpriteWidth(), realPosition.y + this.getSpriteHeight()), null);
		}
		if(MyLittleWallpaperService.SHOWPONYBOX) 
			canvas.drawRect(new Rect(realPosition.x, realPosition.y, realPosition.x + this.getSpriteWidth(), realPosition.y + this.getSpriteHeight()), debugPaint);
	}
	
	/**
	 * releases and cleans up objects
	 */
	public void destroy(){
		if(this.gif != null) this.gif.destroy();
		if(this.staticImage != null) this.staticImage.recycle();
		this.gif = null;
		this.staticImage = null;
		this.initialized = false;
	}
	
	@Override
	public boolean equals(Object o) {
		Sprite s = (Sprite)o;
		return s.fileName.equals(this.fileName);
	}
	
	public boolean isInitialized(){
		return this.initialized;
	}
}
