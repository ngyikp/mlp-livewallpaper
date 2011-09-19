package com.overkill.live.pony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyLittleWallpaperService extends WallpaperService {
	public static final String TAG = "mlpWallpaper";
	
	// Settings
	public static final boolean DEBUG_RENDERTIME = false;
		
	//public ArrayList<Pony> randomPonySet = new ArrayList<Pony>();

    // public ArrayList<Pony> activePonies = new ArrayList<Pony>();
    public ArrayList<Pony> selectablePonies = new ArrayList<Pony>();
    
    public static Random rand;
	    
    public static AssetManager assets;
    File localFolder;
            
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
        
        if(isSDMounted())
			localFolder = new File(Environment.getExternalStorageDirectory(), "ponies");
		else
			localFolder = new File(getFilesDir(), "ponies");
        
		try {
			//String[] ponyFolders  = assets.list("ponies");
			File[] ponyFolders  = localFolder.listFiles(new FileFilter() {				
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			
		    for(File pony : ponyFolders){
		    	Log.i("Pony", "loading folder " + pony.getPath() + " " + pony.exists());
		        selectablePonies.add(createPonyFromFile(pony));
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
    }
    
    private boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
    
    public Pony createPonyFromFile(File folder){
    	Pony p = new Pony("Derp");
    	try{
		    String line = "";
		    File iniFile = new File(folder, "pony.ini");
		    Log.i("Pony", "loading file " + iniFile.getPath() + " " + iniFile.exists());
		    BufferedReader content = new BufferedReader(new FileReader(iniFile));
		    while ((line = content.readLine()) != null) {		    	
			           if(line.startsWith("'")) continue; //skip comments
			           if(line.startsWith("Name")){ p = new Pony(line.substring(5)); continue;}
			           if(line.startsWith("Behavior")){
				           	String[] data = ToolSet.splitWithQualifiers(line, ",", "\"");
				           	
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
    	private RenderEngine engine;
        private SharedPreferences preferences;

        SpriteEngine() {
        	this.engine = new RenderEngine(getBaseContext(), getSurfaceHolder());            	
            preferences = MyLittleWallpaperService.this.getSharedPreferences(TAG, MODE_PRIVATE);        
            preferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(preferences, null);
        }

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			engine.setShowDebugText(sharedPreferences.getBoolean("debug_info", false));
			engine.setMaxFramerate(Integer.valueOf(sharedPreferences.getString("framerate_cap", "10")));
			engine.setScale(Float.valueOf(sharedPreferences.getString("pony_scale", "1.0")));

			// get Background image if we want one
			this.engine.setBackground(sharedPreferences.getInt("background_color", 0xff000000));
			
			String filePath = sharedPreferences.getString("background_image", null);
	        if(sharedPreferences.getBoolean("background_global", false) == false){
	        	this.engine.setBackground((Bitmap) null);
	        }else{
	        	this.engine.setBackground(filePath);
	        }
	        	
	        for(Pony p : this.engine.getPonies()){
	        	p.cleanUp();
	        }
	        
	        this.engine.clearPonies();
	        
	        for(Pony p : selectablePonies){
	        	Log.i(TAG, "do we want \"" + p.name + "\"?");
	        	if(sharedPreferences.getBoolean(p.name, false) == false)
	        		continue;
	        	 this.engine.addPony(p);
	        }
		}  
                        
        @Override
        public void onVisibilityChanged(boolean visible) {
        	this.engine.setVisibility(visible);
        	if(visible){
                this.engine.resume();
            }else{
            	this.engine.pause();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.engine.setFrameSize(width, height);
            this.engine.render();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            this.engine.start();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.engine.stop();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            engine.setOffset(xPixels);     
            engine.render();
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

           
    } // End of SpriteEngine
}