package com.overkill.live.pony;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.widget.Toast;

public class LiveWallpaperSettings extends PreferenceActivity {
	private static final String URL = "http://android.ov3rk1ll.com";
	private static final String PAYPAL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=JBA3WQ9LAFH8C&lc=US&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";
	
	String poniesName[];
    boolean poniesState[];
    
	private static final int IMAGE_PICK = 1;
	private static final int CROP_FROM_CAMERA = 2;
	
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
				try {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
					intent.setType("image/*");
					WallpaperManager wm = WallpaperManager.getInstance(LiveWallpaperSettings.this);
		            int wallpaperWidth = wm.getDesiredMinimumWidth();
		            int wallpaperHeight = wm.getDesiredMinimumHeight();
		        	
					intent.putExtra("crop", "true");
		            intent.putExtra("outputX", wallpaperWidth);
		            intent.putExtra("outputX", wallpaperWidth);
		            intent.putExtra("outputY", wallpaperHeight);
		            intent.putExtra("aspectX", wallpaperWidth);
		            intent.putExtra("aspectY", wallpaperHeight);
		            intent.putExtra("scale", true);
		            intent.putExtra("return-data", false);
		            intent.putExtra("setWallpaper", false);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
					intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
					intent.putExtra("noFaceDetection", true); // lol, negative boolean noFaceDetection}
					startActivityForResult(intent, CROP_FROM_CAMERA);
				} catch (Exception e) {
					Toast.makeText(LiveWallpaperSettings.this, "Error", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}

				
				
//				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//				intent.setType("image/*");
//				startActivityForResult(intent, IMAGE_PICK);
				return true;
			}
		});
				
	}
	
	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
		}

		private File getTempFile() {
		if (isSDCARDMounted()) {

		File f = new File(Environment.getExternalStorageDirectory(), "background.jpg");
		try {
		f.createNewFile();
		} catch (IOException e) {
		e.printStackTrace();
		Toast.makeText(this, "IO Error", Toast.LENGTH_LONG).show();
		}
		return f;
		} else {
		return null;
		}
		}

		private boolean isSDCARDMounted(){
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
		}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	    super.onActivityResult(requestCode, resultCode, data); 

	    switch(requestCode) { 
	    case IMAGE_PICK:
	        if(resultCode == RESULT_OK){  
	            doCrop(data.getData());
//	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//	            cursor.moveToFirst();
//
//	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//	            String filePath = cursor.getString(columnIndex);
//	            cursor.close();
//
//	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
//	            editor.putString("background_image", filePath);
//	            editor.commit();
	        }
		case CROP_FROM_CAMERA:
			if (resultCode != RESULT_OK) return;
	        Bundle extras = data.getExtras();	
	        if (extras != null) {
	            //Bitmap photo = extras.getParcelable("data");
	            File newfile = new File(getFilesDir(), "background.jpg");
	            File temp = getTempFile();
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
	            //saveBitmapToFile(filePath, photo); 
	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
	            editor.putString("background_image", newfile.getPath());
	            editor.commit();
	        }	
	        break;
	    }
	}
	
	private void doCrop(Uri selectedImage) {
 
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
 
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
 
        int size = list.size();
 
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show(); 
            return;
        } else {
        	 WallpaperManager wm = WallpaperManager.getInstance(this);
             int wallpaperWidth = wm.getDesiredMinimumWidth();
             int wallpaperHeight = wm.getDesiredMinimumHeight();
        	
            intent.setData(selectedImage);
 
            intent.putExtra("outputX", wallpaperWidth);
            intent.putExtra("outputY", wallpaperHeight);
            intent.putExtra("aspectX", wallpaperWidth);
            intent.putExtra("aspectY", wallpaperHeight);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
 
            Intent i = new Intent(intent);
            //ResolveInfo res = list.get(0);
 
            //i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
            startActivityForResult(i, CROP_FROM_CAMERA);
            
        }
    }
	
	public boolean saveBitmapToFile(String path, Bitmap bitmap) {
		File file = new File(path);
		boolean res = false;
		if (!file.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(file); 
				res = bitmap.compress(CompressFormat.JPEG, 100, fos); 
				fos.close();
			} catch (Exception e) { }
		}
		return res;
	}	
	
	public static void copyFile(File src, File dst) throws IOException {
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
}
