package com.overkill.ponymanager;

import com.overkill.live.pony.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class PonyAdapter extends ArrayAdapter<DownloadPony>{

	private Context context;
	private int textViewResourceId;

	public PonyAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(this.textViewResourceId, parent, false);
        }
        DownloadPony p = super.getItem(position);
        if (p != null) {          	
        	((ImageView)v.findViewById(R.id.imagePony)).setImageBitmap(p.getImage());

        	((TextView)v.findViewById(R.id.textName)).setText(p.getName());
        	((TextView)v.findViewById(R.id.textState)).setText(p.getState());
        	if(p.getState().equals(DownloadPony.STATE_INSTALLED))
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.GREEN);
        	if(p.getState().equals(DownloadPony.STATE_NOT_INSTALLED))
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.RED);
        	
        	if(p.getState().equals(DownloadPony.STATE_RUNNING)){
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.rgb(255, 126, 0));
        		((TextView)v.findViewById(R.id.textInfo)).setText("Files: " + p.getDoneFileCount() + "/" + p.getTotalFileCount());
        	}else{
        		((TextView)v.findViewById(R.id.textInfo)).setText("Files: " + p.getTotalFileCount() + " Size: " + p.getBytes());
        	}
        	
        	
        }
        return v;
	}
}