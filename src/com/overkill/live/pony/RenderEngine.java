package com.overkill.live.pony;

import java.io.File;
import java.util.ArrayList;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.SystemClock;
import android.view.SurfaceHolder;

public class RenderEngine {
	public static boolean CONFIG_DEBUG_TEXT = true;
	public static int CONFIG_FPS = 20;
	public static int CONFIG_FRAME_DELAY = 1000 / CONFIG_FPS;
	public static float CONFIG_SCALE = 1.0f;
	public static int OFFSET;
	
    private RenderThread renderThread;
    private SurfaceHolder surfaceHolder;
    private boolean visible = false;
    
    private Bitmap backgroundBitmap = null;
    private int backgroundWidth = 0;    
    private int backgroundColor = 0;
    public Paint backgroundTextPaint = new Paint();
    
	private long lastTimeDrawn;
	
    private ArrayList<Pony> activePonies = new ArrayList<Pony>();
	private long realFPS;
	private Point screenCenter;

	public static Rect screenBounds;
	public static Rect visibleScreenArea;
	
	public RenderEngine(Context context, SurfaceHolder surfaceHolder){        
    	backgroundTextPaint.setColor(Color.WHITE);
    	backgroundTextPaint.setTextAlign(Align.LEFT);
    	RenderEngine.screenBounds = new Rect(0, 0, 0, 0);
    	RenderEngine.visibleScreenArea = new Rect(0, 0, 0, 0);
    	this.surfaceHolder = surfaceHolder;
    	this.renderThread = new RenderThread(this);
    	WallpaperManager wm  = WallpaperManager.getInstance(context);
    	this.setWallpaperSize(wm.getDesiredMinimumWidth(), wm.getDesiredMinimumHeight());
    	this.lastTimeDrawn = 0;
    	this.visible = true;
	}
	
    public void render(){
    	if(visible == false) return;
    	long renderStartTime = SystemClock.elapsedRealtime();
    	
        if((renderStartTime - lastTimeDrawn) < RenderEngine.CONFIG_FRAME_DELAY)
        	return;
        
        //Log.i("render? " + fpsCap, "fps " + (1000 / Math.max(1, (renderStartTime - lastTimeDrawn))));
        Canvas canvas = null;
        try{
            canvas = this.surfaceHolder.lockCanvas(null);
            synchronized (this.surfaceHolder) {
                this.drawFrame(canvas);
            }
        }finally{
            if(canvas != null){
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    	realFPS = 1000 / Math.max(1, (renderStartTime - lastTimeDrawn));
        lastTimeDrawn = renderStartTime;
    }

    protected void drawFrame(Canvas canvas) {   	
        long currentTime = SystemClock.elapsedRealtime();
        try {
            this.renderBackground(canvas);
            if (canvas != null) {
	               	for(int i=0; i < activePonies.size(); i++){
	               		Pony p = activePonies.get(i);
	  	               	p.update(currentTime);
	  	               	p.draw(canvas);
	               	}
           }
        } catch (Exception e) {
        	e.printStackTrace();
		}
    }

    private void renderBackground(Canvas c){
    	if(backgroundBitmap == null)
    		c.drawColor(backgroundColor);
    	else
    		c.drawBitmap(backgroundBitmap, this.screenCenter.x - (backgroundWidth / 2) + OFFSET, 0, null);        	

    	if(CONFIG_DEBUG_TEXT){
        	c.drawText("My Little Pony Wallpaper / " + activePonies.size() + " ponies active / Scale is " + CONFIG_SCALE + " / " + realFPS + " FPS (cap at " + CONFIG_FPS + ")", 5, 50, backgroundTextPaint);
        	c.drawText("©2011 ov3rk1ll - http://android.ov3rk1ll.com", 5, 65, backgroundTextPaint);
    	}
    }   
    
    public void start(){
    	this.renderThread = new RenderThread(this);
        this.renderThread.startRender();
    }

    public void stop(){
        boolean retry = true;
        this.renderThread.stopRender();
        while (retry) {
            try {
                this.renderThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }      
    
    public void setFrameSize(int w, int h){
    	RenderEngine.visibleScreenArea = new Rect(0, 0, w, h);
    	if(RenderEngine.screenBounds.width() <= 0 || RenderEngine.screenBounds.height() <= 0){
        	this.setWallpaperSize(w, h);   		
    	}
    }
    
    public void setWallpaperSize(int w, int h){
        RenderEngine.screenBounds = new Rect(0, 0, w, h);
    	this.screenCenter = new Point(w / 2, h / 2);
    }
    
    public void setBackground(int color){
    	this.backgroundColor = color;
    }
    
    public void setBackground(Bitmap background){
    	this.backgroundBitmap = background;
    	if(background == null) return;
    	this.backgroundWidth = background.getWidth();
    }
    
    public void setBackground(String filePath){
    	if(filePath != null && new File(filePath).exists()){
    		BitmapFactory.Options opts = new BitmapFactory.Options();
    		opts.inPurgeable = true;
    		opts.inInputShareable = true;
    		this.setBackground(BitmapFactory.decodeFile(filePath, opts));
    	}else{
    		backgroundBitmap = null;
    	}
    }
    
    public void setMaxFramerate(int fps){
    	RenderEngine.CONFIG_FPS = fps;
    	RenderEngine.CONFIG_FRAME_DELAY = 1000 / RenderEngine.CONFIG_FPS;
    }
    
    public void setShowDebugText(boolean show){
    	RenderEngine.CONFIG_DEBUG_TEXT = show;
    }
    
    public void setScale(float scale){
    	RenderEngine.CONFIG_SCALE = scale;
    }
    
    public void setOffset(int pixel){
    	RenderEngine.OFFSET = pixel;
    }
    
    public void addPony(Pony pony){
    	this.activePonies.add(pony);
    }
    
    public void clearPonies(){
    	this.activePonies.clear();
    }
    
    public void setVisibility(boolean visible){
    	this.visible = visible;
    }
    
    public void pause(){
    	this.renderThread.pauseRender();
    }
    
    public void resume(){
    	 this.renderThread.resumeRender();
    }
}
