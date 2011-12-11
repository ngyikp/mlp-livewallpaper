package com.overkill.live.pony;

import com.overkill.ponymanager.pony.DownloadPony;
import com.overkill.ponymanager.pony.SortablePonyAdapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

public class SelectPonyAdapter extends SortablePonyAdapter {
	
	public SelectPonyAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}
			
	@Override
	public void add(DownloadPony object) {
		if(super.allPonies.contains(object))
			super.allPonies.remove(object);
		super.add(object);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 final DownloadPony p = getItem(position);

         //User super class to create the View
         View v = super.getView(position, convertView, parent);
         CheckedTextView tv = (CheckedTextView)v.findViewById(android.R.id.text1);
         
         tv.setChecked(p.getState() == R.string.pony_state_installed);
         
         //Put the image on the TextView
         Drawable d = new BitmapDrawable(p.getImage());
         tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

         //Add margin between image and text (support various screen densities)
         int dp5 = (int) (5 * super.context.getResources().getDisplayMetrics().density + 0.5f);
         tv.setCompoundDrawablePadding(dp5);

         return v;
    }

	
	
}
