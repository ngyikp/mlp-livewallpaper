package com.overkill.live.pony;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;

import com.overkill.live.pony.engine.Pony;
import com.overkill.live.pony.engine.RenderEngine;
import com.overkill.ponymanager.PonyManager;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyLittleWallpaperService extends WallpaperService {
	public static final String SETTINGS_NAME = "mlpWallpaper";	
	public static final String TAG = "mlpWallpaper";	
	public static String VERSION = "";
	
	// Debug Settings
	public static final boolean DEBUG = false;
	public static final boolean SHOWPONYBOX = false;
	public static final boolean INTERACTIONLINES = false;
	
	private boolean RENDER_ON_SWIPE = true;	
	
    public ArrayList<Pony> selectablePonies = new ArrayList<Pony>();
    
    public static Random rand;

    public File localFolder;
    
    
    private BroadcastReceiver mStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        }
    };
    
    @Override
    public void onCreate() {    	
        super.onCreate();                       
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(this.getClass().getPackage().getName(), 0);
	        VERSION = pinfo.versionName;
		} catch (NameNotFoundException e) {;}        
        rand = new Random();
        localFolder = PonyManager.selectFolder(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        registerReceiver(mStorageReceiver, filter);
    }
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(mStorageReceiver);
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
        
        SpriteEngine() {
        	super();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
        	this.engine = new RenderEngine(getBaseContext(), getSurfaceHolder());            	
            preferences = MyLittleWallpaperService.this.getSharedPreferences(TAG, MODE_PRIVATE);        
            preferences.registerOnSharedPreferenceChangeListener(this);
            
//            Editor editor = preferences.edit();
//            editor.putLong("savedTime", SystemClock.elapsedRealtime());
//            editor.putBoolean("added_pony", true);
//            editor.putBoolean("changed_pony", true);
//            editor.commit();
            onSharedPreferenceChanged(preferences, "startup");
        	super.onCreate(surfaceHolder);
        	this.previewMode = super.isPreview();
        }
        
        private synchronized void loadSelectablePonies(){
        	localFolder = PonyManager.selectFolder(MyLittleWallpaperService.this);
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
				    	Pony tmp = Pony.fromFile(ponyFolder, true);
				    	if(selectablePonies.contains(tmp) == false)
				    		selectablePonies.add(Pony.fromFile(ponyFolder));
				    }
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
			Log.i(TAG + ".loadSelectablePonies", "Got " + selectablePonies.size() + " Ponies");
        }
        
        private synchronized void selectPonies(SharedPreferences sharedPreferences){
        	for(Pony p : this.engine.getPonies()){
	        	p.cleanUp();
	        }
	        
	        this.engine.clearPonies();
	        
	        for(Pony p : selectablePonies){
	        	if(sharedPreferences.getBoolean("usepony_" + p.name, false) == true && this.engine.getPonies().contains(p) == false){
	        		this.engine.addPony(p);
	        		Log.i(TAG + ".selectPonies", "Added \"" + p.name + "\" to activePonies");
	        	}
	        }
        }
        
		@Override
		public synchronized void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			if(key.equals("savedTime") == false && key.equals("startup") == false){ return; }
			RenderEngine.loading = true;
			Thread t = new Thread(new Runnable() {				
				@Override
				public void run() {
					Editor editor = sharedPreferences.edit();
					
					if(key.equals("startup")){
						Log.i("onSharedPreferenceChanged", "startup was true. calling loadSelectablePonies() and selectPonies()");
						loadSelectablePonies();
						selectPonies(sharedPreferences);
					}
					
					if(sharedPreferences.getBoolean("added_pony", false) == true){
						Log.i("onSharedPreferenceChanged", "added_pony was true. calling loadSelectablePonies()");
						loadSelectablePonies();
						editor.putBoolean("added_pony", false);
					}					
					if(sharedPreferences.getBoolean("changed_pony", false) == true){
						Log.i("onSharedPreferenceChanged", "changed_pony was true. calling selectPonies()");
						selectPonies(sharedPreferences);
						editor.putBoolean("changed_pony", false);
					}
					if(sharedPreferences.getBoolean("changed_folder", false) == true){
						Log.i("onSharedPreferenceChanged", "changed_folder was true. calling loadSelectablePonies() and selectPonies()");
						engine.reloadLocalFolder();
						loadSelectablePonies();
						selectPonies(sharedPreferences);
						editor.putBoolean("changed_folder", false);
					}
					
					engine.setShowDebugText(sharedPreferences.getBoolean("debug_info", false));
					engine.setShowEffects(sharedPreferences.getBoolean("show_effects", false));
					engine.setMaxFramerate(Integer.valueOf(sharedPreferences.getString("framerate_cap", "10")));
					engine.setScale(Float.valueOf(sharedPreferences.getString("pony_scale", "1")));			
					RenderEngine.MOVEMENT_DELAY_MS = Integer.valueOf(sharedPreferences.getString("movement_delay_ms", "100"));
					
					engine.setInteraction(
							sharedPreferences.getBoolean("interact_pony", false),
							sharedPreferences.getBoolean("interact_user", false));

					RENDER_ON_SWIPE = sharedPreferences.getBoolean("render_on_swipe", true);
					
					// get Background image if we want one			
					String filePath = sharedPreferences.getString("background_image", null);
					engine.setBackgroundColor(sharedPreferences.getInt("background_color", 0xff000000));
					if(filePath != null){
						File file = new File(filePath);
						if(file.exists()){
							engine.setBackground(filePath);
						}						
					}
					engine.setUseBackgroundImage(sharedPreferences.getBoolean("background_global", false));   
					
			        RenderEngine.loading = false;
			        editor.commit();
				}
			});	
			t.start();
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
//           previewMode = isPreview();
//           Log.i("onSurfaceChanged", "previewMode:" + previewMode);
        	if(previewMode){
        		this.engine.setWallpaperSize(-1, -1);
        	}else{
        		this.engine.setWallpaperSize(getWallpaperDesiredMinimumWidth(), getWallpaperDesiredMinimumHeight());
        	}

//    		this.engine.setWallpaperSize(getWallpaperDesiredMinimumWidth(), getWallpaperDesiredMinimumHeight());
//    		this.engine.setWallpaperSize(-1,-1);
            this.engine.setFrameSize(width, height);
            RenderEngine.ready = true;
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
        	if(previewMode)
                engine.setOffset(0);     
        	else  
            	engine.setOffset(xPixels);   
        	
            if(RENDER_ON_SWIPE) engine.render();
        }
        
        @Override
        public Bundle onCommand(String action, int x, int y, int z,	Bundle extras, boolean resultRequested) {
        	if(action.equals(WallpaperManager.COMMAND_TAP) && RenderEngine.CONFIG_INTERACT_TOUCH == true){
        		try{
	        		long currentTime = SystemClock.elapsedRealtime();
	        		for(Pony p : this.engine.getPonies()){
		            	if(p.isPonyAtLocation(x, y)){
		            		p.touch(currentTime);
		            	}
		            }	
        		}catch(NullPointerException e){
        			// a part of the pony seems to be not ready yet
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