package com.overkill.live.pony;

import java.io.File;

import com.overkill.live.pony.engine.RenderEngine;
import com.overkill.ponymanager.pony.PonyManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SDcardMountStateChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(RenderEngine.ready == false) return;
		String action = intent.getAction();
        Log.i("ExternalMediaFormatActivity", "got action " + action);
        File sdcardFolder = PonyManager.selectForcedFolder(context, false);
        File usedFolder = PonyManager.selectFolder(context);
        if(sdcardFolder.equals(usedFolder)){
        	SharedPreferences preferences = context.getSharedPreferences(MyLittleWallpaperService.SETTINGS_NAME, Context.MODE_PRIVATE);  
        	Editor editor = preferences.edit();
        	editor.putBoolean("changed_folder", true);
        	editor.commit();
        }

	}

}
