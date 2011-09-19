package com.overkill.live.pony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class LiveWallpaperSettings extends PreferenceActivity {
	private static final String URL = "http://android.ov3rk1ll.com";
	private static final String PAYPAL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=4E99YZ7MYNAEE";
	
	String poniesName[];
    boolean poniesState[];
    
	private Uri selectedImageUri;
	private File localFolder;
    
	private static final int CROP_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;
	
	private boolean isSDMounted(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(MyLittleWallpaperService.TAG);
        addPreferencesFromResource(R.xml.preferences);
        
        if(isSDMounted())
			localFolder = new File(Environment.getExternalStorageDirectory(), "ponies");
		else
			localFolder = new File(getFilesDir(), "ponies");
        
			//String[] ponyFolders  = assets.list("ponies");
		File[] ponyFolders  = localFolder.listFiles(new FileFilter() {				
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		poniesName = new String[ponyFolders.length];
		
		for(int i = 0; i < ponyFolders.length; i++){
			String line = "";
		    File iniFile = new File(ponyFolders[i], "pony.ini");
		    Log.i("Pony", "loading file " + iniFile.getPath() + " " + iniFile.exists());
		    BufferedReader content;
			try {
				content = new BufferedReader(new FileReader(iniFile));
				while ((line = content.readLine()) != null) {		    	
			    	if(line.startsWith("'")) continue;
				    if(line.startsWith("Name")){ poniesName[i] = line.substring(5).trim(); break;}
			    }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
                
        poniesState = new boolean[poniesName.length];
        
        for(int i = 0; i < poniesName.length; i++){
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
		
		((CheckBoxPreference)findPreference("background_global")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean value = (Boolean) newValue;
				if(value == false){
					return true;
				}else{
					if(getPreferenceManager().getSharedPreferences().getString("background_image", null) == null){
						Intent intent = new Intent();
		                intent.setType("image/*");
		                intent.setAction(Intent.ACTION_GET_CONTENT);
		                startActivityForResult(Intent.createChooser(intent, "Pick image from"), PICK_FROM_FILE);
					}
				}
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
		
		EditText editText = (EditText)((EditTextPreference)findPreference("pony_scale")).getEditText();
        editText.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		((Preference)findPreference("background_image")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick image from"), PICK_FROM_FILE);                
                return true;
			}
		});
						
		((Preference)findPreference("background_color")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				colorpicker();
                return true;
			}
		});
				
	}
	
	public void colorpicker(){
	    
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(LiveWallpaperSettings.this, getPreferenceManager().getSharedPreferences().getInt("background_color", 0xff000000), new OnAmbilWarnaListener(){
			@Override
			public void onCancel(AmbilWarnaDialog dialog) { }

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				Editor editor = getPreferenceManager().getSharedPreferences().edit();
				editor.putInt("background_color", color);
				editor.commit();				
			}				
		});

	    dialog.show();
	}
	
	/**
	 * Creates an empty public access file for the crop intent to write to
	 * @return The Uri to the empty file
	 * @throws IOException 
	 */
	private Uri getTempUri() throws IOException {
		File f = new File(getFilesDir(), "temp_background.jpg");
		if(f.exists()) f.delete();
		FileOutputStream fos = openFileOutput("temp_background.jpg", Context.MODE_WORLD_WRITEABLE);
        fos.close();         
        return Uri.fromFile(f);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		if (resultCode != RESULT_OK){ ((CheckBoxPreference)findPreference("background_global")).setChecked(false); return;}

	    switch(requestCode) { 
	    case PICK_FROM_FILE: 
	    	selectedImageUri = data.getData();
	    	try {
				doCrop();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

	    	break;	  
		case CROP_FROM_CAMERA:
			if (resultCode != RESULT_OK) return;
			if (data == null) return;
	        Bundle extras = data.getExtras();	
	        if (extras != null) {
	        	
	            File newfile = new File(getFilesDir(), "background.jpg");
	            File temp = new File(getFilesDir(), "temp_background.jpg");
	            try {
					copyFile(temp, newfile);
		            temp.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            if(newfile.exists() == false){
	            	Toast.makeText(this, "No image found", Toast.LENGTH_LONG).show();
	            	return;
	            }
	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
	            editor.putString("background_image", newfile.getPath());
	            editor.commit();
	        }	
	        break;
	    }
	}
	
	private void doCrop() throws IOException {
    	
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        
        int size = list.size();
        
        if (size == 0) {	        
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
        	
            return;
        } else {
        	intent.setData(selectedImageUri);
            
        	int w = getWallpaperDesiredMinimumWidth();
        	int h = getWallpaperDesiredMinimumHeight();
        	
            intent.putExtra("outputX", w);
            intent.putExtra("outputY", h);
            intent.putExtra("aspectX", w);
            intent.putExtra("aspectY", h);
            intent.putExtra("scale", true);
            
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            
        	
        	Intent i = new Intent(intent);
	        ResolveInfo res	= list.get(0);

	        i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

	        startActivityForResult(i, CROP_FROM_CAMERA);
        }
	}
		
	
	public static void copyFile(File src, File dst) throws IOException {
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    } finally {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
}
