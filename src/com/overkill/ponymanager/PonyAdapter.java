package com.overkill.ponymanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	private List<DownloadPony> allPonies;
	private List<DownloadPony> filteredPonies;	
	
	public PonyAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
	}
	
	

	public PonyAdapter(Context context, int textViewResourceId,	List<DownloadPony> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
		this.allPonies = new LinkedList<DownloadPony>(objects);
		this.resetFilter();
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
        	
        	if(p.getState() == R.string.pony_state_installed)
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.GREEN);
        	if(p.getState() == R.string.pony_state_not_installed)
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.RED);
        	if(p.getState() == R.string.pony_state_update)
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.BLUE);
        	
        	if(p.getState() == R.string.pony_state_loading){
        		((TextView)v.findViewById(R.id.textState)).setTextColor(Color.rgb(255, 126, 0));
        		((TextView)v.findViewById(R.id.textInfo)).setText(context.getString(R.string.pony_info_loading, p.getDoneFileCount() ,p.getTotalFileCount()));
        	}else{
        		((TextView)v.findViewById(R.id.textInfo)).setText(context.getString(R.string.pony_info, p.getTotalFileCount(), p.getBytes()));
        	}      
        }
        return v;
	}
	
	public boolean hasUpdate(){
		for(int i = 0; i < getCount(); i++){
			if(getItem(i).getState() == R.string.pony_state_update)
				return true;
		}
		return false;
	}
	
	public boolean hasNotInstalled(){
		for(int i = 0; i < getCount(); i++){
			if(getItem(i).getState() == R.string.pony_state_not_installed)
				return true;
		}
		return false;
	}
	
	public void resetFilter(){
		this.filteredPonies = new LinkedList<DownloadPony>(this.allPonies);
	}
	
	@Override
	public int getCount() {
		return this.filteredPonies.size();
	}
}