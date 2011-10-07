package com.overkill.ponymanager;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PickPonyDialog extends Dialog {

	SharedPreferences preferences;
	Editor editor;
	
	public PickPonyDialog(Context context, SharedPreferences preferences, Editor editor) {
		super(context);
		this.preferences = preferences;
		this.editor = editor;
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
	}

}
