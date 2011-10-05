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

import com.overkill.ponymanager.PonyManager;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
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
	
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
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
	protected void onDestroy() {
		editor.commit();
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(MyLittleWallpaperService.TAG);
        addPreferencesFromResource(R.xml.preferences);
        
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        editor = sharedPreferences.edit();
        
        
        if(isSDMounted())
			localFolder = new File(Environment.getExternalStorageDirectory(), "ponies");
		else
			localFolder = new File(getFilesDir(), "ponies");
        
        if(localFolder.exists() == false || localFolder.isDirectory() == false){
			localFolder.mkdir();
			File nomedia = new File(localFolder, ".nomedia");
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
					if(sharedPreferences.getString("background_image", null) == null){
						Intent intent = new Intent();
		                intent.setType("image/*");
		                intent.setAction(Intent.ACTION_GET_CONTENT);
		                startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_from)), PICK_FROM_FILE);
					}
				}
				return true;
			}
		});
		
		((CheckBoxPreference)findPreference("force_local_storage")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean value = (Boolean) newValue;
				if(value == false && isSDMounted() == false) return false; // want to save on sd but has no sd card
				// TODO move already installed ponies
				return true;
			}
		});
		
		((Preference)findPreference("more_donate_paypal")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
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
				File[] ponyFolders  = localFolder.listFiles(new FileFilter() {				
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
				
				if(ponyFolders.length == 0){
					// we have no ponies, open PonyMananger
					Toast.makeText(LiveWallpaperSettings.this, R.string.no_ponies_installed, Toast.LENGTH_LONG).show();
					Intent i = new Intent(getBaseContext(), PonyManager.class);
					startActivity(i);
					return false;
				}
				
				poniesName = new String[ponyFolders.length];
				
				for(int i = 0; i < ponyFolders.length; i++){
					String line = "";
				    File iniFile = new File(ponyFolders[i], "pony.ini");
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
		        	poniesState[i] = sharedPreferences.getBoolean("usepony_" + poniesName[i], false);
		        }       
				
				AlertDialog.Builder builder = new AlertDialog.Builder(LiveWallpaperSettings.this);
		        builder.setTitle(R.string.pony_select_title);
		        builder.setMultiChoiceItems(poniesName, poniesState, new DialogInterface.OnMultiChoiceClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {						
						poniesState[which] = isChecked;
						if(isChecked){
							editor.putBoolean("added_pony", true);
						}
						editor.putBoolean("changed_pony", true);					
					}
				});
		        AlertDialog alert = builder.create();
		        alert.setOnDismissListener(new OnDismissListener() {			
					@Override
					public void onDismiss(DialogInterface dialog) {	
						for(int i = 0; i < poniesName.length; i++){
							editor.putBoolean("usepony_" + poniesName[i], poniesState[i]);
						}
					}
				});
		        alert.show();
				return true;
			}
		});
		
		EditText editText = (EditText)((EditTextPreference)findPreference("pony_scale")).getEditText();
        editText.setKeyListener(DigitsKeyListener.getInstance(false, true));
        ((EditTextPreference)findPreference("pony_scale")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String)newValue;
				if(value.equals("") || value == null){
					editor.putString("pony_scale", "1");
					return false;
				}
				return true;
			}
		});
		
		((Preference)findPreference("background_image")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_from)), PICK_FROM_FILE);                
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
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(LiveWallpaperSettings.this, sharedPreferences.getInt("background_color", 0xff000000), new OnAmbilWarnaListener(){
			@Override
			public void onCancel(AmbilWarnaDialog dialog) { }

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				editor.putInt("background_color", color);			
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
	        	
	            File newFile = new File(getFilesDir(), "background_" + SystemClock.elapsedRealtime() + ".jpg");
	            File temp = new File(getFilesDir(), "temp_background.jpg");
	            try {
					copyFile(temp, newFile);
		            temp.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            if(newFile.exists() == false){
	            	Toast.makeText(this, R.string.error_custom_image, Toast.LENGTH_LONG).show();
	            	return;
	            }
	            setNewBackgroundImage(newFile.getPath());
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
        	File newFile = new File(getFilesDir(), "background_" + SystemClock.elapsedRealtime() + ".jpg");
        	File srcFile = new File(getRealPathFromURI(selectedImageUri));   
        	copyFile(srcFile, newFile);
        	setNewBackgroundImage(newFile.getPath());
        	Toast.makeText(this, R.string.no_crop_app, Toast.LENGTH_SHORT).show();     
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
	
	public void movePonies(File from, File to){
		
	}
	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	public void setNewBackgroundImage(String path){
		String oldFilePath = sharedPreferences.getString("background_image", null);
		if(oldFilePath != null){
			File oldFile = new File(oldFilePath);
			if(oldFile.exists())
				oldFile.delete();
		}
        editor.putString("background_image", path);
	}
}
