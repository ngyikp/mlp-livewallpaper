package com.overkill.live.pony;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import com.overkill.ponymanager.pony.PonyManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import android.widget.Toast;

public class LiveWallpaperSettings extends PreferenceActivity {
	public static final String URL_WEB = "http://android.ov3rk1ll.com";
	public static final String URL_TWITTER = "https://twitter.com/OV3RK1LL";
	public static final String URL_FACEBOOK = "http://www.facebook.com/MLPLiveWallpaper";
	public static final String PAYPAL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=4E99YZ7MYNAEE";
	
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	String poniesName[];
    boolean poniesState[];
    
	private Uri selectedImageUri;
	private File localFolder;
    
	private static final int CROP_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;
		
	@Override
	protected void onDestroy() {
		// Put saveTime to settings so the WallpaperEngine can pick up changes
		editor.putLong("savedTime", SystemClock.elapsedRealtime());
		editor.commit();
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(MyLittleWallpaperService.SETTINGS_NAME);
        addPreferencesFromResource(R.xml.preferences);
        
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        editor = sharedPreferences.edit();

        localFolder = PonyManager.selectFolder(this);
        
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(this.getClass().getPackage().getName(), 0);
	        ((Preference)findPreference("more_version")).setSummary(pinfo.versionName);
		} catch (NameNotFoundException e) {
		}
		
		// Open twitter link
		((Preference)findPreference("more_link_twitter")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_TWITTER));
				startActivity(i);
				return true;
			}
		});
		
		// Open facebook link
		((Preference)findPreference("more_link_facebook")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_FACEBOOK));
				startActivity(i);
				return true;
			}
		});
		
		// Set Background image (on/off)
		((CheckBoxPreference)findPreference("background_global")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean value = (Boolean) newValue;
				if(value == false){
					// Turn off -> Just do it
					return true;
				}else{
					// Turn on -> If none is saved yet -> Open pick dialog
					if(sharedPreferences.getString("background_image", null) == null){
						showPickImageDialog();
					}
				}
				return true;
			}
		});

		
		final CheckBoxPreference force_internal_storage = (CheckBoxPreference)findPreference("force_internal_storage");
		force_internal_storage.setSummary(getString(R.string.force_local_storage_summary, localFolder.getPath()));
		force_internal_storage.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final boolean value = (Boolean) newValue;
				if(value == false && PonyManager.isSDMounted() == false) return false; // want to save on sd but has no sd card
				// Pick the folder depending on the value
				final File newFolder = PonyManager.selectForcedFolder(LiveWallpaperSettings.this, value);
				// If the folder is different from the current folder
				if(newFolder.equals(localFolder) == false){
					// Create a ProgressDialog
					final ProgressDialog dialog = new ProgressDialog(LiveWallpaperSettings.this);
					dialog.setMessage("From:\n" + localFolder.getPath() + "\nTo:\n" + newFolder.getPath() + "\nPlease wait...");
					dialog.setTitle("Moving Ponies");
					dialog.setCancelable(false);
					Thread thread = new Thread(new Runnable() {						
						@Override
						public void run() {
							try {
								// Move Ponies
								movePonies(localFolder, newFolder);
							} catch (IOException e) {
								e.printStackTrace();
							}
							// Set new folder as current folder and tell the engine
							localFolder = newFolder;
							editor.putBoolean("changed_folder", true);		
							runOnUiThread(new Runnable() {								
								@Override
								public void run() {
									// Update the text on the settings
									force_internal_storage.setSummary(getString(R.string.force_local_storage_summary, localFolder.getPath()));
									force_internal_storage.setChecked(value);
									dialog.dismiss();
								}
							});
						}
					});
					dialog.show();
					thread.start();
					
				}				
				return false;
			}
		});
		
		// Open PayPal Link
		((Preference)findPreference("more_donate_paypal")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL));
				startActivity(i);
				return true;
			}
		});
				
		// 
		((Preference)findPreference("pony_select")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(LiveWallpaperSettings.this, PickPonyActivity.class);
				i.putExtra("localFolder", localFolder.getPath());
				startActivity(i);
				return true;
			}
		});
		
		EditText editTextPonyScale = (EditText)((EditTextPreference)findPreference("pony_scale")).getEditText();
        editTextPonyScale.setKeyListener(DigitsKeyListener.getInstance(false, true));
        ((EditTextPreference)findPreference("pony_scale")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String)newValue;
				// Reset to 1 if user lets field empty
				if(value.equals("") || value == null){
					editor.putString("pony_scale", "1");
					return false;
				}
				return true;
			}
		});
        
        EditText editTextMovementDelay = (EditText)((EditTextPreference)findPreference("movement_delay_ms")).getEditText();
        editTextMovementDelay.setKeyListener(DigitsKeyListener.getInstance(false, false));
        ((EditTextPreference)findPreference("movement_delay_ms")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String)newValue;
				// Reset to 100 if user lets field empty
				if(value.equals("") || value == null){
					editor.putString("movement_delay_ms", "100");
					return false;
				}
				return true;
			}
		});
		
		((Preference)findPreference("background_image")).setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showPickImageDialog();
                return true;
			}
		});
										
		((CheckBoxPreference)findPreference("show_effects")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean value = (Boolean) newValue;
				if(value == false) return true;
				AlertDialog.Builder dialog = new AlertDialog.Builder(LiveWallpaperSettings.this);
				dialog.setTitle(R.string.show_effects_warning_title);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setMessage(R.string.show_effects_warning_message);
				dialog.setPositiveButton(android.R.string.ok, null);
				dialog.show();
				return true;
			}
		});
		
	}
		
	/**
	 * Creates an empty public access file for the crop intent to write to
	 * @return The Uri to the empty file
	 * @throws IOException 
	 */
	private Uri getTempUri() throws IOException {
		File f = getTempFile();
		if(f.exists()) f.delete();
		FileOutputStream fos = openFileOutput(f.getName(), Context.MODE_WORLD_WRITEABLE);
        fos.close();         
        return Uri.fromFile(f);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		if (resultCode != RESULT_OK){
			if(sharedPreferences.getString("background_image", null) == null){
				((CheckBoxPreference)findPreference("background_global")).setChecked(false); 
			}
			return;
		}
		
	    switch(requestCode) { 
		    case PICK_FROM_FILE: 
		    	selectedImageUri = data.getData();
		    	try {
					startImageCropIntent();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	
		    	break;	  
			case CROP_FROM_CAMERA:
				if (resultCode != RESULT_OK) return;
				if (data == null) return;
		        Bundle extras = data.getExtras();	
		        if (extras != null) {	        	
		            File newFile = getBackgroundFile();
		            File temp = getTempFile();
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
		            setNewBackgroundImage(newFile);
		        }	
		        break;
	    }
	}
	
	private void showPickImageDialog(){
		String items[] = {"Use current wallpaper", "Pick image"};
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.background_image_title);
		dialog.setItems(items, new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0){
					// Try to use current wallpaper Image
					WallpaperManager wpm = WallpaperManager.getInstance(LiveWallpaperSettings.this);
					Drawable d = wpm.getDrawable();
					if(d != null){
						try {
							Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
							File image = getBackgroundFile();			
							bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(image));
							setNewBackgroundImage(image);
							Toast.makeText(LiveWallpaperSettings.this, "Save current background image", Toast.LENGTH_LONG).show();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}else{
					// Open Get Content Intent
					Intent intent = new Intent();
			        intent.setType("image/*");
			        intent.setAction(Intent.ACTION_GET_CONTENT);
			        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_from)), PICK_FROM_FILE);
				}
				dialog.dismiss();
			}
		});	
		dialog.show();
	}
	
	private void startImageCropIntent() throws IOException {
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        
        int size = list.size();
        
        if (size == 0) {
        	File newFile = getBackgroundFile();
        	File srcFile = new File(getRealPathFromURI(selectedImageUri));   
        	copyFile(srcFile, newFile);
        	setNewBackgroundImage(newFile);
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
	
//	private void startPickZipIntent(){
//		Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_from)), 999);
//	}
		
	public File getBackgroundFile(){
		return new File(getFilesDir(), "background_" + SystemClock.elapsedRealtime() + ".jpg");
	}
	
	public File getTempFile(){
		return new File(getFilesDir(), "temp_background.jpg");
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
	
	public void movePonies(File from, File to) throws IOException{
		File files[] = from.listFiles();
		if(to.exists() == false) to.mkdir();
		for(File file : files){
			if(file.isDirectory()){
				movePonies(new File(from, file.getName()), new File(to, file.getName()));
			}else{
				copyFile(file, new File(to, file.getName()));
				file.delete();
			}
		}
		from.delete();
	}
	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	public void setNewBackgroundImage(File newFile){
		String oldFilePath = sharedPreferences.getString("background_image", null);
		if(oldFilePath != null){
			File oldFile = new File(oldFilePath);
			if(oldFile.exists())
				oldFile.delete();
		}
        editor.putString("background_image", newFile.getPath());
	}
}
