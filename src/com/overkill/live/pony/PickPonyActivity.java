package com.overkill.live.pony;

import java.io.File;
import java.util.ArrayList;

import com.overkill.ponymanager.pony.DownloadPony;
import com.overkill.ponymanager.pony.SettingsPonyAdapter;
import com.overkill.ponymanager.pony.SettingsPonyAdapter.ValueChangedListener;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PickPonyActivity extends ListActivity implements ValueChangedListener{
	private final int TOO_MUCH_PONY = 12;
	
	private SettingsPonyAdapter adapter;
	private File localFolder;
	private SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_pony);
		sharedPreferences = getSharedPreferences(MyLittleWallpaperService.SETTINGS_NAME, Context.MODE_PRIVATE);
		
		if(getIntent().hasExtra("localFolder")){
			localFolder = new File(getIntent().getExtras().getString("localFolder"));
		}
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Loading...");
		new Thread(new Runnable() {			
			@Override
			public void run() {
				adapter = new SettingsPonyAdapter(PickPonyActivity.this, PickPonyActivity.this, R.layout.settings_item_pony, getLocalPonies());
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						setListAdapter(adapter);
						onValueChanged();	
						dialog.cancel();
					}
				});				
			}
		}).start();
		dialog.show();

	
	}
	
	public void onOkButtonClick(View view) {
		if(adapter.getAllUsageCount() >= TOO_MUCH_PONY){
			AlertDialog.Builder dialog = new AlertDialog.Builder(PickPonyActivity.this);
			dialog.setTitle(R.string.show_effects_warning_title);
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setMessage(R.string.warning_too_much_pony);
			dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor editor = sharedPreferences.edit();
					adapter.saveSettings(editor);
					// Although we save the settings her, onSharedPreferenceChanged will only trigger after the settings activity is closed 
					// and not when this editor commits
					editor.putBoolean("changed_pony", true);
					editor.commit();
					finish();					
				}
			});
			dialog.show();	
		}else{
			Editor editor = sharedPreferences.edit();
			adapter.saveSettings(editor);
			// Although we save the settings her, onSharedPreferenceChanged will only trigger after the settings activity is closed 
			// and not when this editor commits
			editor.putBoolean("changed_pony", true);
			editor.commit();
			finish();
		}
		
	}
	
	public void onCancelButtonClick(View view){
		finish();
	}
	
	public void onAllOneButtonClick(View view){
		for(DownloadPony p : adapter.getAllItems()){
			p.setUsageCount(1);
		}
		adapter.notifyDataSetChanged();
		onValueChanged();
	}
	
	public void onAllZeroButtonClick(View view){
		for(DownloadPony p : adapter.getAllItems()){
			p.setUsageCount(0);
		}
		adapter.notifyDataSetChanged();
		onValueChanged();
	}
	
	private ArrayList<DownloadPony> getLocalPonies(){
		ArrayList<DownloadPony> ponyList = new ArrayList<DownloadPony>();
		File[] subFolders = localFolder.listFiles(ToolSet.folderContainingINIFileFilter);
	
		for(File folder : subFolders){
			DownloadPony p = DownloadPony.fromINI(folder);
			p.setImage(BitmapFactory.decodeFile(new File(localFolder, p.getFolder() + "/preview.gif").getPath()));
			p.setUsageCount(sharedPreferences.getInt("pony_count_" + p.getFolder(), 0));
			if(ponyList.contains(p) == false){
				ponyList.add(p);
			}
		}
		return ponyList;
	}

	@Override
	public void onValueChanged() {		
		setTitle(getString(R.string.pony_select_title) + " - Total: " + adapter.getAllUsageCount());
		
	}	
	
	@Override
	public void onBackPressed() {
		Toast.makeText(this, "Please use the \"" + getString(android.R.string.ok) + "\" or \"" + getString(android.R.string.cancel) + "\" button", Toast.LENGTH_SHORT).show();
	}
}