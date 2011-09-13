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
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class MyLittleWallpaperService extends WallpaperService {
	public static final String TAG = "mlpWallpaper";
	private static final int FPS = 20;
	
	private final Handler drawHandler = new Handler();
	
	public ArrayList<Pony> randomPonySet = new ArrayList<Pony>();

    public ArrayList<Pony> activePonies = new ArrayList<Pony>();
    public ArrayList<Pony> selectablePonies = new ArrayList<Pony>();
    
    public static Random rand;

	public static int wallpaperWidth;
	public static int wallpaperHeight;
	
	public static int frameWidth;
	public static int frameHeight;

    public static float offset;
    
    public static RectF viewPort;
	
    public static boolean loading = true;
    
    public static Bitmap background = null;
    
    public double realFPS = 0;
    
    public static AssetManager assets;
    
    public Paint backgroundTextPaint = new Paint();
    
    void displayFiles (AssetManager mgr, String path) {
        try {
            String list[] = mgr.list(path);
            if (list != null)
                for (int i=0; i<list.length; ++i)
                    {
                        Log.v("Assets:", path +"/"+ list[i]);
                        displayFiles(mgr, path + "/" + list[i]);
                    }
        } catch (Exception e) {
            Log.v("List error:", "can't list" + path);
        }

    }
    
    @Override
    public void onCreate() {
        super.onCreate();        
        
        rand = new Random();
        assets = getAssets();

        WallpaperManager wm = WallpaperManager.getInstance(this);
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
							if (data[8].trim().equalsIgnoreCase("none")) {
								movement = AllowedMoves.None;
							} else if (data[8].trim().equalsIgnoreCase("horizontal_only")) {
								movement = AllowedMoves.Horizontal_Only;
							} else if (data[8].trim().equalsIgnoreCase("vertical_only")) {
								movement = AllowedMoves.Vertical_Only;
							} else if (data[8].trim().equalsIgnoreCase("horizontal_vertical")) {
								movement = AllowedMoves.Horizontal_Vertical;
							} else if (data[8].trim().equalsIgnoreCase("diagonal_only")) {
								movement = AllowedMoves.Diagonal_Only;
							} else if (data[8].trim().equalsIgnoreCase("diagonal_horizontal")) {
								movement = AllowedMoves.Diagonal_Horizontal;
							} else if (data[8].trim().equalsIgnoreCase("diagonal_vertical")) {
								movement = AllowedMoves.Diagonal_Vertical;
							} else if (data[8].trim().equalsIgnoreCase("all")) {
								movement = AllowedMoves.All;
							} else if (data[8].trim().equalsIgnoreCase("mouseover")) {
								movement = AllowedMoves.MouseOver;
							} else if (data[8].trim().equalsIgnoreCase("sleep")) {
								movement = AllowedMoves.Sleep;
							}
							if(data[6].trim().endsWith(".gif") == false)
								continue;
				            p.addBehavior(
				            		data[1], 
				            		Double.parseDouble(data[2]), 
				            		Double.parseDouble(data[3]), 
				            		Double.parseDouble(data[4]),
				            		Double.parseDouble(data[5]),
				            		folder.getPath() + "/" + data[6].trim(), 
				            		folder.getPath() + "/" + data[7].trim(), 
				            		movement, 
				            		null, false, 0, 0);
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
        	backgroundTextPaint.setTextAlign(Align.CENTER);
        	
            preferences = MyLittleWallpaperService.this.getSharedPreferences(TAG, MODE_PRIVATE);        
            preferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(preferences, null);

        }

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			// get Background image if we want one
			String filePath = sharedPreferences.getString("background_image", null);
			Log.i(TAG, "background: " + filePath);
	        if(sharedPreferences.getBoolean("background_global", false) == false){
	        	background = null;
	        }else{
	        	if(filePath != null && new File(filePath).exists())
	        		background = BitmapFactory.decodeFile(filePath);
	        	else
	        		background = null;
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
            setTouchEventsEnabled(true);
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

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            	if(keyLook==false){
	                touchX = event.getX();
	                touchY = event.getY();
	                // Check if there is a pony at the touched point
	                for(Pony p : activePonies){
	                	Log.i("Pony[" + p.name + "]", "trigger Touchevent");
	                	p.setDestination(touchX, touchY);
	                		/*p.touch();
	                		drawFrame();
	                		break;*/
	                }	              
            	}else{
	            	touchX = -1;
	                touchY = -1;	            		
            	}
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
            	touchX = -1;
                touchY = -1;
                keyLook = false;
            } else {
            	touchX = -1;
                touchY = -1;
            }
            super.onTouchEvent(event);
        }

        private void drawFrame() {
            long renderStartTime = SystemClock.elapsedRealtime();
            if((1000 / (renderStartTime - lastTimeDrawn)) > FPS)
            	return;
            
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            long currentTime = SystemClock.elapsedRealtime();
            try {
            	c = holder.lockCanvas();
                //c.save();
            	drawBackground(c);
                if (c != null) {
                	if(loading == false){
	                	for(int i=0; i < activePonies.size(); i++){
	                		Pony p = activePonies.get(i);
	  	                	p.update(currentTime);
	  	                	p.draw(c);
	  	                }
                	}
                	//c.restore();
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            realFPS = 1000 / (renderStartTime - lastTimeDrawn);
          	//Log.i("Render", "Frame took " + (renderStartTime - lastTimeDrawn) + " ms to render (" + (1000 / (renderStartTime - lastTimeDrawn)) + " fps)");
            lastTimeDrawn = renderStartTime;
            
            // Reschedule the next redraw
            drawHandler.removeCallbacks(drawCanvas);
            if (visible) {
                drawHandler.postDelayed(drawCanvas, 1000 / FPS);
            }
        }
        
        private void drawBackground(Canvas c){
        	if(background == null)
        		c.drawColor(Color.BLACK);
        	else
        		c.drawBitmap(background, this.centerX - (background.getWidth() / 2) + offset, 0, null);
        	
        	/*if(loading)
        		c.drawText("My Little Pony Wallpaper / loading...", this.centerX + offset, this.centerY - 15, backgroundTextPaint);        		
        	else
        		c.drawText("My Little Pony Wallpaper / " + activePonies.size() + " ponies active / " + realFPS + " FPS (cap at " + FPS + ")", this.centerX, this.centerY - 15, backgroundTextPaint);
        	c.drawText("©2011 ov3rk1ll", this.centerX, this.centerY, backgroundTextPaint);
        	c.drawText("http://android.ov3rk1ll.com", this.centerX, this.centerY + 15, backgroundTextPaint);*/
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