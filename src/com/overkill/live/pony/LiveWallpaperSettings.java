package com.overkill.live.pony;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;

public class LiveWallpaperSettings extends PreferenceActivity {
	private static final String URL = "http://android.ov3rk1ll.com";
	private static final String PAYPAL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=JBA3WQ9LAFH8C&lc=US&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";
	
	String poniesName[];
    boolean poniesState[];
	private static final int IMAGE_PICK = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(MyLittleWallpaperService.TAG);
        addPreferencesFromResource(R.xml.preferences);
        
        try {
			poniesName  = getAssets().list("ponies");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        //poniesName = new String[ponyFolders.length];
        poniesState = new boolean[poniesName.length];
        
        for(int i = 0; i < poniesName.length; i++){
        	//poniesName[i] = ponyFolders[i].getName();
        	poniesState[i] = getPreferenceManager().getSharedPreferences().getBoolean(poniesName[i], false);
        }
        
        
        
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(this.getClass().getPackage().getName(),0);
	        ((Preference)findPreference("more_version")).setSummary(pinfo.versionName);
		} catch (NameNotFoundException e) {
		}
		
		Preference link = (Preference)findPreference("more_link");
		link.setSummary(URL);
		link.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
				startActivity(i);
				return true;
			}
		});
		
		((Preference)findPreference("more_donate")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL));
				startActivity(i);
				return true;
			}
		});
				
		((Preference)findPreference("pony_select")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(LiveWallpaperSettings.this);
		        builder.setTitle("Pick ponies");
		        builder.setMultiChoiceItems(poniesName, poniesState, new DialogInterface.OnMultiChoiceClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						poniesState[which] = isChecked;
					}
				});
		        AlertDialog alert = builder.create();
		        alert.setOnDismissListener(new OnDismissListener() {			
					@Override
					public void onDismiss(DialogInterface dialog) {	
						Editor editor = getPreferenceManager().getSharedPreferences().edit();
						for(int i = 0; i < poniesName.length; i++){
							editor.putBoolean(poniesName[i], poniesState[i]);
						}
						editor.commit();
					}
				});
		        alert.show();
				return true;
			}
		});
		
		
		((Preference)findPreference("background_image")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, IMAGE_PICK);
				return true;
			}
		});
				
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case IMAGE_PICK:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};

	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();

	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
	            editor.putString("background_image", filePath);
	            editor.commit();
	        }
	    }
	}
	
}
