package com.overkill.live.pony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import com.overkill.live.pony.Pony.AllowedMoves;
import com.overkill.live.pony.Pony.Directions;
import com.overkill.ponymanager.PonyManager;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyLittleWallpaperService extends WallpaperService {
	public static final String TAG = "mlpWallpaper";	
	public static String VERSION = "";
	// Settings
	public static final boolean DEBUG = false;
	public static final boolean SHOWPONYBOX = false;
	public static final boolean INTERACTIONLINES = false;
	
	private boolean RENDER_ON_SWIPE = true;	
	
    public ArrayList<Pony> selectablePonies = new ArrayList<Pony>();
    
    public static Random rand;

    public File localFolder;

    @Override
    public void onCreate() {    	
        super.onCreate();                       
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(this.getClass().getPackage().getName(),0);
	        VERSION = pinfo.versionName;
		} catch (NameNotFoundException e) {;}        
        rand = new Random();              
        selectLocalFolder();
    }
    
    public void selectLocalFolder(){
    	if(PonyManager.isSDMounted())
			localFolder = new File(Environment.getExternalStorageDirectory(), "ponies");
		else
			localFolder = new File(getFilesDir(), "ponies");    
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
        private boolean previewMode = false;
		//private boolean ready = false;
        
        SpriteEngine() {
        	this.previewMode = false;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
        	this.engine = new RenderEngine(getBaseContext(), getSurfaceHolder());            	
            preferences = MyLittleWallpaperService.this.getSharedPreferences(TAG, MODE_PRIVATE);        
            preferences.registerOnSharedPreferenceChangeListener(this);
            Editor editor = preferences.edit();
            editor.putBoolean("added_pony", true);
            editor.putBoolean("changed_pony", true);
            editor.commit();
            //onSharedPreferenceChanged(preferences, null);
        	super.onCreate(surfaceHolder);
        }
        
        public void loadSelectablePonies(){
        	selectLocalFolder();
			if(localFolder.exists()){
				try {
					File[] ponyFolders  = localFolder.listFiles(new FileFilter() {				
						@Override
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});
					selectablePonies.clear();
				    for(File ponyFolder : ponyFolders){
				    	//Pony tmp = createPonyFromFile(pony, true);
				    	//if(selectablePonies.contains(tmp) == false)
				    		selectablePonies.add(Pony.fromFile(ponyFolder));
				    }
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
        }
        
        public void selectPonies(SharedPreferences sharedPreferences){
        	for(Pony p : this.engine.getPonies()){
	        	p.cleanUp();
	        }
	        
	        this.engine.clearPonies();
	        
	        for(Pony p : selectablePonies){
	        	Log.i(TAG, "do we want \"" + p.name + "\"? " + sharedPreferences.getBoolean("usepony_" + p.name, false));
	        	if(sharedPreferences.getBoolean("usepony_" + p.name, false) == true)
	        		this.engine.addPony(p);
	        }
        }
        
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			//RenderEngine.loading = true;
//			Thread t = new Thread(new Runnable() {				
//				@Override
//				public void run() {
					Log.i("onSharedPreferenceChanged", "key=" + key + " start");
					Editor editor = sharedPreferences.edit();
					if(sharedPreferences.getBoolean("added_pony", false) == true){
						loadSelectablePonies();
						editor.putBoolean("added_pony", false);
					}					
					if(sharedPreferences.getBoolean("changed_pony", false) == true){
						selectPonies(sharedPreferences);
						editor.putBoolean("changed_pony", false);
					}
					editor.commit();
					
					engine.setShowDebugText(sharedPreferences.getBoolean("debug_info", false));
					engine.setShowEffects(sharedPreferences.getBoolean("show_effects", false));
					engine.setMaxFramerate(Integer.valueOf(sharedPreferences.getString("framerate_cap", "10")));
					engine.setScale(Float.valueOf(sharedPreferences.getString("pony_scale", "1")));			

					engine.setInteraction(
							sharedPreferences.getBoolean("interact_pony", false),
							sharedPreferences.getBoolean("interact_user", false));

					RENDER_ON_SWIPE = sharedPreferences.getBoolean("render_on_swipe", true);
					
					// get Background image if we want one			
					String filePath = sharedPreferences.getString("background_image", null);
					engine.setBackground(sharedPreferences.getInt("background_color", 0xff000000));
			        if(sharedPreferences.getBoolean("background_global", false) == false || filePath == null){
						engine.setBackground(sharedPreferences.getInt("background_color", 0xff000000));
			        }else{
			        	engine.setBackground(filePath);
			        }       
			        //RenderEngine.loading = false;
					Log.i("onSharedPreferenceChanged", "key=" + key + " done");
//				}
//			});	
//			t.start();
		}  
                        
        @Override
        public void onVisibilityChanged(boolean visible) {
        	this.engine.setVisibility(visible);
        	if(visible/* && !RenderEngine.loading*/){
                this.engine.resume();
            }else{
            	this.engine.pause();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            /*previewMode = isPreview();
            Log.i("onSurfaceChanged", "previewMode:" + previewMode);
        	if(previewMode)
        		this.engine.setWallpaperSize(-1, -1);
        	else
        		this.engine.setWallpaperSize(getWallpaperDesiredMinimumWidth(), getWallpaperDesiredMinimumHeight());*/

    		this.engine.setWallpaperSize(getWallpaperDesiredMinimumWidth(), getWallpaperDesiredMinimumHeight());
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
        	/*if(previewMode)
                engine.setOffset(0);     
        	else*/  
            if(RENDER_ON_SWIPE) engine.render();
        }
        
        @Override
        public Bundle onCommand(String action, int x, int y, int z,	Bundle extras, boolean resultRequested) {
        	if(action.equals(WallpaperManager.COMMAND_TAP) && RenderEngine.CONFIG_INTERACT_TOUCH == true){
            	Log.i("onCommand", action + " [" + x + "," + y + "]");
        		long currentTime = SystemClock.elapsedRealtime();
        		for(Pony p : this.engine.getPonies()){
	            	if(p.isPonyAtLocation(x, y)){
	            		p.touch(currentTime);
	            	}
	            }	
        	}
        	return super.onCommand(action, x, y, z, extras, resultRequested);
        }
        
//        @Override
//        public void onTouchEvent(MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            	if(RenderEngine.CONFIG_INTERACT_TOUCH){
//	            	long currentTime = SystemClock.elapsedRealtime();
//	            	int touchX = (int) event.getX();
//	            	int touchY = (int) event.getY();
//		            for(Pony p : this.engine.getPonies()){
//		            	if(p.isPonyAtLocation(touchX, touchY)){
//		            		p.touch(currentTime);
//		            	}
//		            }
//            	}
//            }
//            super.onTouchEvent(event);
//        }

           
    } // End of SpriteEngine
}