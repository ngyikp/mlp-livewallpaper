package com.overkill.live.pony;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import com.overkill.live.pony.Pony.AllowedMoves;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyLittleWallpaperService extends WallpaperService {
	public static final String TAG = "mlpWallpaper";
	
	// Settings
	public static final boolean DEBUG_RENDERTIME = false;
	public static boolean DEBUG_TEXT = true;
	public static int FPS = 20;
	public static int FRAMEDEALY = 1000 / FPS;
	public static float SCALE = 1.0f;
	
	private final Handler drawHandler = new Handler();
	
	public ArrayList<Pony> randomPonySet = new ArrayList<Pony>();

    public ArrayList<Pony> activePonies = new ArrayList<Pony>();
    public ArrayList<Pony> selectablePonies = new ArrayList<Pony>();
    
    public static Random rand;

	public static int wallpaperWidth;
	public static int wallpaperHeight;
	
	public static int frameWidth;
	public static int frameHeight;

    public static int offset;
    
    public static RectF viewPort;
	
    public static boolean loading = true;
    
    public static Bitmap background = null;
    public int backgroundWidth = 0;
    
    public int backgroundColor = 0;
    
    public double realFPS = 0;
    
    public static AssetManager assets;
    
    public Paint backgroundTextPaint = new Paint();
        
	// Behavior options
	public static final int BO_name = 1;
	public static final int BO_probability = 2;
	public static final int BO_max_duration = 3;
	public static final int BO_min_duration = 4;
	public static final int BO_speed = 5;
	public static final int BO_right_image_path = 6;
	public static final int BO_left_image_path = 7;
	public static final int BO_movement_type = 8;
	public static final int BO_linked_behavior = 9;
	public static final int BO_speaking_start = 10;
	public static final int BO_speaking_end = 11;
	public static final int BO_skip = 12;
	public static final int BO_xcoord = 13;
	public static final int BO_ycoord = 14;
	public static final int BO_object_to_follow = 15;
    
    @Override
    public void onCreate() {
        super.onCreate();        
        
        rand = new Random();
        assets = getAssets();

        WallpaperManager wm  = WallpaperManager.getInstance(this);
        wallpaperWidth = wm.getDesiredMinimumWidth();
        wallpaperHeight = wm.getDesiredMinimumHeight();
        
		try {
			String[] ponyFolders  = assets.list("ponies");
		        
		    for(String pony : ponyFolders){
		    	Log.i("Pony", "loading folder " + pony);
		        selectablePonies.add(createPonyFromFolder("ponies/" + pony));
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		loading = false;
        
        
    }
    
    public Pony createPonyFromFolder(String path){
    	Pony p = new Pony("Derp");
    	try{
		    String line = "";
		    File folder = new File(path);
		    BufferedReader content = new BufferedReader(new InputStreamReader(assets.open(path + "/Pony.ini")));
		    while ((line = content.readLine()) != null) {		    	
			           if(line.startsWith("'")) continue; //skip comments
			           if(line.startsWith("Name")){ p = new Pony(line.substring(5)); continue;}
			           if(line.startsWith("Behavior")){
				           	String[] data = splitWithQualifiers(line, ",", "\"");
				           	
				           	AllowedMoves movement = AllowedMoves.None;
				           	String linked_behavior = "";
							int xcoord = 0;
							int ycoord = 0;
							boolean skip = false;
				           	
							if (data[BO_movement_type].trim().equalsIgnoreCase("none")) {
								movement = AllowedMoves.None;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("horizontal_only")) {
								movement = AllowedMoves.Horizontal_Only;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("vertical_only")) {
								movement = AllowedMoves.Vertical_Only;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("horizontal_vertical")) {
								movement = AllowedMoves.Horizontal_Vertical;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("diagonal_only")) {
								movement = AllowedMoves.Diagonal_Only;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("diagonal_horizontal")) {
								movement = AllowedMoves.Diagonal_Horizontal;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("diagonal_vertical")) {
								movement = AllowedMoves.Diagonal_Vertical;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("all")) {
								movement = AllowedMoves.All;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("mouseover")) {
								movement = AllowedMoves.MouseOver;
							} else if (data[BO_movement_type].trim().equalsIgnoreCase("sleep")) {
								movement = AllowedMoves.Sleep;
							}
							if(data[6].trim().endsWith(".gif") == false)
								continue;
							
							if (data.length > BO_linked_behavior) {
								linked_behavior = data[BO_linked_behavior].trim();
								skip = Boolean.parseBoolean(data[BO_skip].trim());
								xcoord = Integer.parseInt(data[BO_xcoord].trim());
								ycoord = Integer.parseInt(data[BO_ycoord].trim());
							}
							
				            p.addBehavior(
				            		data[BO_name], 
				            		Double.parseDouble(data[BO_probability]), 
				            		Double.parseDouble(data[BO_max_duration]), 
				            		Double.parseDouble(data[BO_min_duration]),
				            		Double.parseDouble(data[BO_speed]),
				            		folder.getPath() + "/" + data[BO_right_image_path].trim(), 
				            		folder.getPath() + "/" + data[BO_left_image_path].trim(), 
				            		movement, 
				            		linked_behavior, 
				            		skip, 
				            		xcoord, 
				            		ycoord);
				            p.linkBehaviors();
				            continue;
			           }
		    		
		    	}
	        content.close();
	        Log.i("loading", p.name + " with " + p.behaviors.size() + " Behaviors");
		  	
    	}catch (Exception e) {
			e.printStackTrace();
		}
		return p;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new SpriteEngine();
    }

    class SpriteEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private float touchX = -1;
        private float touchY = -1;
        //private long startTime;
        private float centerX;
        private float centerY;
        private boolean keyLook = false;
                
        private boolean visible;
        
        private long lastTimeDrawn = 0;

        private SharedPreferences preferences;

        SpriteEngine() {
            lastTimeDrawn = SystemClock.elapsedRealtime() - (1000 / FPS);         
           
            this.centerX = wallpaperWidth/2.0f;
            this.centerY = wallpaperHeight/2.0f;
            
            viewPort = new RectF(0, 0, 0, 0);            
            
        	backgroundTextPaint.setColor(Color.WHITE);
        	backgroundTextPaint.setTextAlign(Align.LEFT);
        	
            preferences = MyLittleWallpaperService.this.getSharedPreferences(TAG, MODE_PRIVATE);        
            preferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(preferences, null);

        }

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			DEBUG_TEXT = sharedPreferences.getBoolean("debug_info", false);
			FPS = Integer.valueOf(sharedPreferences.getString("framerate_cap", "10"));
			FRAMEDEALY = 1000 / FPS;
			
			SCALE = Float.valueOf(sharedPreferences.getString("pony_scale", "1.0"));

			// get Background image if we want one
			backgroundColor = sharedPreferences.getInt("background_color", 0xff000000);
			
			String filePath = sharedPreferences.getString("background_image", null);
	        if(sharedPreferences.getBoolean("background_global", false) == false){
	        	background = null;
	        }else{
	        	if(filePath != null && new File(filePath).exists()){
	        		BitmapFactory.Options opts = new BitmapFactory.Options();
	        		opts.inPurgeable = true;
	        		background = BitmapFactory.decodeFile(filePath, opts);
		            backgroundWidth = background.getWidth();
	        	}else{
	        		background = null;
	        	}
	        }
	        		        
	        activePonies.clear();
	        for(Pony p : selectablePonies){
	        	Log.i(TAG, "do we want \"" + p.name + "\"?");
	        	if(sharedPreferences.getBoolean(p.name, false) == false)
	        		continue;
	        	activePonies.add(p);
	        }
		}  
                
        private final Runnable drawCanvas = new Runnable() {
            public void run() {
            	drawFrame();              
            }
        };
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            // TODO add useful touch interaction
            setTouchEventsEnabled(false);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            drawHandler.removeCallbacks(drawCanvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (this.visible) {
                drawFrame();
            } else {
                drawHandler.removeCallbacks(drawCanvas);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            frameWidth = width;
            frameHeight = height;
            if(wallpaperWidth <= 0 || wallpaperHeight <= 0){
            	wallpaperWidth = frameWidth;
            	wallpaperHeight = frameHeight;
            }
            viewPort = new RectF(0, 0, frameWidth, frameHeight);
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            drawHandler.removeCallbacks(drawCanvas);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            offset = xPixels;      
            drawFrame();
        }

//        @Override
//        public void onTouchEvent(MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            	if(keyLook==false){
//	                touchX = event.getX();
//	                touchY = event.getY();
//	                for(Pony p : activePonies){
//	                	Log.i("Pony[" + p.name + "]", "trigger Touchevent");
//	                	p.setDestination(touchX, touchY);
//	                		/*p.touch();
//	                		drawFrame();
//	                		break;*/
//	                }	              
//            	}else{
//	            	touchX = -1;
//	                touchY = -1;	            		
//            	}
//            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//            	touchX = -1;
//                touchY = -1;
//                keyLook = false;
//            } else {
//            	touchX = -1;
//                touchY = -1;
//            }
//            super.onTouchEvent(event);
//        }

        private void drawFrame() {
            long renderStartTime = SystemClock.elapsedRealtime();
            if((1000 / (renderStartTime - lastTimeDrawn)) > FPS)
            	return;
            
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            long currentTime = SystemClock.elapsedRealtime();
            try {
            	synchronized (holder) {
	            	long t1 = System.currentTimeMillis();
	            	c = holder.lockCanvas(null);
	              	if(DEBUG_RENDERTIME) Log.i("Render Lock Canvas", "took " + (System.currentTimeMillis() - t1) + " ms");
	            	drawBackground(c);
	                if (c != null) {
	                	if(loading == false){
	                    	long t0 = System.currentTimeMillis();
		                	for(int i=0; i < activePonies.size(); i++){
		                		Pony p = activePonies.get(i);
		  	                	p.update(currentTime);
		  	                	p.draw(c);
		  	                }
		                	if(DEBUG_RENDERTIME) Log.i("Render Pony", "took " + (System.currentTimeMillis() - t0) + " ms");
	                	}
	                }
            	}
            } catch (Exception e) {
            	e.printStackTrace();
			}
            finally {
            	long t0 = System.currentTimeMillis();
                if (c != null) holder.unlockCanvasAndPost(c);
                if(DEBUG_RENDERTIME) Log.i("Render Draw Canvas", "took " + (System.currentTimeMillis() - t0) + " ms");
            } 
            realFPS = 1000 / (renderStartTime - lastTimeDrawn);
            if(DEBUG_RENDERTIME) Log.i("Render Frame", "took " + (renderStartTime - lastTimeDrawn) + " ms to render (" + realFPS + " fps)");
            
            // Reschedule the next redraw
            drawHandler.removeCallbacks(drawCanvas);
            if (visible) {
            	// TODO change delay by a small amount if we took to long
//            	long renderDiff = (renderStartTime - lastTimeDrawn) - FRAMEDEALY;
//            	Log.i("Render", "renderDiff = " + renderDiff);
//            	if(renderDiff > 0)
//            		drawHandler.postDelayed(drawCanvas, FRAMEDEALY - renderDiff);
//            	else
            		drawHandler.postDelayed(drawCanvas, FRAMEDEALY);
            }
            lastTimeDrawn = renderStartTime;
        }
        
        private void drawBackground(Canvas c){
        	long t0 = System.currentTimeMillis();
        	if(background == null)
        		c.drawColor(backgroundColor);
        	else
        		c.drawBitmap(background, this.centerX - (backgroundWidth / 2) + offset, 0, null);        	

        	if(DEBUG_TEXT){
	        	c.drawText("My Little Pony Wallpaper / " + activePonies.size() + " ponies active / Scale is " + SCALE + " / " + realFPS + " FPS (cap at " + FPS + ")", 5, 50, backgroundTextPaint);
	        	c.drawText("©2011 ov3rk1ll - http://android.ov3rk1ll.com", 5, 65, backgroundTextPaint);
        	}
        	if(DEBUG_RENDERTIME) Log.i("Render Background", "took " + (System.currentTimeMillis() - t0) + " ms");
        }     
    } // End of SpriteEngine
    
	public static String[] splitWithQualifiers(String SourceText, String TextDelimiter, String TextQualifier) {
		return splitWithQualifiers(SourceText, TextDelimiter, TextQualifier, "");
	}
    public static String[] splitWithQualifiers(String SourceText, String TextDelimiter, String TextQualifier, String ClosingTextQualifier) {
		String[] strTemp;
		String[] strRes; int I; int J; String A; String B; boolean blnStart = false;
		B = "";
		
		if (TextDelimiter != " ") SourceText = SourceText.trim();
		if (ClosingTextQualifier.length() > 0) SourceText = SourceText.replace(ClosingTextQualifier, TextQualifier);
		strTemp = SourceText.split(TextDelimiter);
		for (I = 0; I < strTemp.length; I++) {
		    J = strTemp[I].indexOf(TextQualifier, 0);
		    if (J > -1) {
		        A = strTemp[I].replace(TextQualifier, "").trim();
		        String C = strTemp[I].replace(TextQualifier, "");
		        if (strTemp[I].trim().equals(TextQualifier + A + TextQualifier)) {
		                B = B + A + " \n";
		                blnStart = false;
		        } else if (strTemp[I].trim().equals(TextQualifier + C + TextQualifier)) {
	                B = B + C + " \n";
	                blnStart = false;
		        } else if (strTemp[I].trim().equals(TextQualifier + A)) {
		                B = B + A + TextDelimiter;
		                blnStart = true;
		        } else if (strTemp[I].trim().equals(A)) {
		                B = B + A + TextDelimiter;
		                blnStart = false;
		        } else if (strTemp[I].trim().equals(A + TextQualifier)) {
		                B = B + A + "\n";
		                blnStart = false;
		        }
		    } else {
		        if (blnStart)
		            B = B + strTemp[I] + TextDelimiter;
		        else
		            B = B + strTemp[I] + "\n";
		    }
		}
		if (B.length() > 0) {
		    B = B.substring(0, B.length());
		    strRes = B.split("\n");
		} else {
		    strRes = new String[1];
		    strRes[0] = SourceText;
		}
		return strRes;
	}
}