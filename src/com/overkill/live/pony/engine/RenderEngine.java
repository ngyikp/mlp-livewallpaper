package com.overkill.live.pony.engine;

import java.io.File;
import java.util.ArrayList;

import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.R;
import com.overkill.ponymanager.PonyManager;

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
import android.util.Log;
import android.view.SurfaceHolder;

public class RenderEngine {
	public static boolean CONFIG_DEBUG_TEXT = false;
	public static boolean CONFIG_SHOW_EFFECTS = false;
	public static boolean CONFIG_INTERACT_PONY = false;
	public static boolean CONFIG_INTERACT_TOUCH = true;
	public static int MOVEMENT_DELAY_MS = 100;
	static int CONFIG_FPS = 10;
	public static int CONFIG_FRAME_DELAY = 1000 / CONFIG_FPS;
	public static float CONFIG_SCALE = 1.0f;
	public static int OFFSET;
    public static File localFolder;
	
	public static int PADDING_TOP = 50;
	public static int PADDING_BOTTOM = 0;
	
	//private UpdateThread updateThread;
    private RenderThread renderThread;    
    
    private SurfaceHolder surfaceHolder;
    private Context context;
    private boolean visible = false;
    
    private Bitmap backgroundBitmap = null;
    private int backgroundWidth = 0;    
    private int backgroundColor = 0;
    public Paint backgroundTextPaint = new Paint();
    
	private long lastTimeDrawn;
	
    public static ArrayList<Pony> activePonies = new ArrayList<Pony>();
    
	private long realFPS;
	
	private Point screenCenter;
	private Point wallpaperCenter;

	public static Rect screenBounds;
	public static Rect visibleScreenArea;
	
	public boolean ready = false;	
	public static boolean loading = false;
	
	public RenderEngine(Context context, SurfaceHolder surfaceHolder){        
    	backgroundTextPaint.setColor(Color.WHITE);
    	backgroundTextPaint.setTextAlign(Align.LEFT);
    	RenderEngine.screenBounds = new Rect(0, 0, 0, 0);
    	RenderEngine.visibleScreenArea = new Rect(0, 0, 0, 0);
    	this.surfaceHolder = surfaceHolder;
    	this.context = context;
    	this.lastTimeDrawn = 0;
    	this.visible = true;
    	RenderEngine.localFolder = PonyManager.selectFolder(this.context);
    	
    	//this.updateThread = new UpdateThread(this);
    	this.renderThread = new RenderThread(this);
	}
	
	public void render(){
		this.render(SystemClock.elapsedRealtime());
	}
	
    public void render(long globalTime){
    	if(visible == false || ready == false) return;   	
    	// Do only render if enough time elapsed since last time rendering 
        // if((globalTime - lastTimeDrawn) < RenderEngine.CONFIG_FRAME_DELAY) return;
        Canvas canvas = null;
        try{
            canvas = this.surfaceHolder.lockCanvas(null);
            synchronized (this.surfaceHolder) {
                this.drawFrame(canvas, globalTime);
            }
        }finally{
            if(canvas != null){
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    	realFPS = 1000 / Math.max(1, (globalTime - lastTimeDrawn));
        lastTimeDrawn = globalTime;
    }

    public void update(long globalTime){
    	for(int i=0; i < activePonies.size(); i++){
       		activePonies.get(i).update(globalTime);
       		activePonies.get(i).updateSprites(globalTime);
       	}
    }
    
    protected void drawFrame(Canvas canvas, long globalTime) {   	
        try {
        	if (canvas != null) {
        		this.renderBackground(canvas);  
        		if(localFolder.canRead() == false){ this.renderLoadingText(canvas, "Waiting for filesystem... " + localFolder.getPath()); return; }   
        		if(loading == true){ this.renderLoadingText(canvas, "Loading... Please wait..."); return; }   
   		
        		if(activePonies.size() == 0){
            		backgroundTextPaint.setTextAlign(Align.CENTER);
            		canvas.drawText(this.context.getString(R.string.no_ponies_selected), screenCenter.x, screenCenter.y, backgroundTextPaint);
            	}
        		
//        		if(updateThread.ready == false) return;
	            for(int i=0; i < activePonies.size(); i++){
	           		activePonies.get(i).update(globalTime);
	           		activePonies.get(i).updateSprites(globalTime);
	            	activePonies.get(i).draw(canvas);
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
    		c.drawBitmap(backgroundBitmap, this.wallpaperCenter.x - (backgroundWidth / 2) + OFFSET, 0, null);        	

    	if(CONFIG_DEBUG_TEXT){
    		backgroundTextPaint.setTextAlign(Align.LEFT);    		
        	c.drawText(this.context.getString(R.string.debug_text, MyLittleWallpaperService.VERSION, activePonies.size(), CONFIG_SCALE, realFPS, CONFIG_FPS), 5, PADDING_TOP, backgroundTextPaint);
        	c.drawText("©2011 ov3rk1ll - http://android.ov3rk1ll.com", 5, PADDING_TOP + 15, backgroundTextPaint);
    	}   	
    }   
    
    private void renderLoadingText(Canvas c, String text){
    	backgroundTextPaint.setTextAlign(Align.CENTER);
		c.drawText(text, screenCenter.x, screenCenter.y, backgroundTextPaint);
    }
    
    public void start(){
//    	this.updateThread.startUpdate();
		this.renderThread = new RenderThread(this);
    	this.renderThread.startRender();
    }

    public void stop(){
        boolean retry = true;
//        this.updateThread.stopUpdate();
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
    	RenderEngine.visibleScreenArea = new Rect(0, PADDING_TOP, w, h - PADDING_BOTTOM);
    	if(RenderEngine.screenBounds.width() <= 0 || RenderEngine.screenBounds.height() <= 0){
        	this.setWallpaperSize(w, h - PADDING_BOTTOM);   		
    	}
    	this.screenCenter = new Point(w / 2, h / 2);
    }
    
    public void setWallpaperSize(int w, int h){
        RenderEngine.screenBounds = new Rect(0, PADDING_TOP, w, h - PADDING_BOTTOM);
    	this.wallpaperCenter = new Point(w / 2, h / 2);
    	Log.i("setWallpaperSize", "w="+w+" h="+h);
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
    		try{
	    		BitmapFactory.Options opts = new BitmapFactory.Options();
	    		opts.inPurgeable = true;
	    		opts.inInputShareable = true;
	    		this.setBackground(BitmapFactory.decodeFile(filePath, opts));
    		} catch(OutOfMemoryError e){
    			//Toast.makeText(context, "Error loading background image\n" + e.getMessage(), Toast.LENGTH_LONG).show();
    			e.printStackTrace();
    			// fallback to backgroundcolor
    			this.setBackground((Bitmap) null);
    		}
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
    	RenderEngine.activePonies.add(pony);
    }
    
    public void clearPonies(){
    	RenderEngine.activePonies.clear();
    }
    
    public ArrayList<Pony> getPonies(){
    	return RenderEngine.activePonies;
    }
    
    public void setVisibility(boolean visible){
    	this.visible = visible;
    }
    
    public void setShowEffects(boolean show){
    	RenderEngine.CONFIG_SHOW_EFFECTS = show;
    }
    
    public void setInteraction(boolean pony, boolean touch){
    	RenderEngine.CONFIG_INTERACT_PONY = pony;
    	RenderEngine.CONFIG_INTERACT_TOUCH = touch;
    }
    
    public void setPreviewMode(boolean isPreview){
    	if(isPreview)
    		setWallpaperSize(-1, -1);
    }
    
    public File getLocalFolder() {
		return localFolder;
	}

	public void setLocalFolder(File localFolder) {
		RenderEngine.localFolder = localFolder;
	}
	
	public void reloadLocalFolder(){
		RenderEngine.localFolder = PonyManager.selectFolder(this.context);		
	}

	public void pause(){
//    	this.updateThread.pauseUpdate();
    	this.renderThread.pauseRender();
    }
    
    public void resume(){
//    	this.updateThread.resumeUpdate();
    	this.renderThread.resumeRender();
    }

	public void startIfNotRunning() {
		
	}

	public static void suggestRestart() {
		// TODO Stop all child-threads, Clean up arrays in engine, reload config		
	}
}
