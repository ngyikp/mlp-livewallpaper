package com.overkill.ponymanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.overkill.live.pony.R;

public class SortablePonyAdapter extends ArrayAdapter<DownloadPony>{

	protected Context context;
	protected int layoutResourceId;
	protected int textViewResourceId;
	protected List<DownloadPony> allPonies;
	protected List<DownloadPony> filteredPonies;	
	
	public SortablePonyAdapter(Context context, int resourceId) {
		super(context, resourceId);		
		this.context = context;
		this.layoutResourceId = resourceId;
		this.allPonies = new LinkedList<DownloadPony>();
		this.filteredPonies = new LinkedList<DownloadPony>();
	}
	
		

	public SortablePonyAdapter(Context context, int resourceId,	List<DownloadPony> objects) {
		super(context, resourceId);
		this.context = context;
		this.layoutResourceId = resourceId;
		this.allPonies = new LinkedList<DownloadPony>(objects);
		this.filteredPonies = new LinkedList<DownloadPony>();
		this.resetFilter();
		this.sort(true);
	}

	public SortablePonyAdapter(Context context, int resourceId, int textViewResourceId) {
		super(context, resourceId, textViewResourceId);
		this.context = context;
		this.layoutResourceId = resourceId;
		this.textViewResourceId = textViewResourceId;
		this.allPonies = new LinkedList<DownloadPony>();
		this.filteredPonies = new LinkedList<DownloadPony>();
	}



	@Override
	public void add(DownloadPony object) {
		this.allPonies.add(object);
	}
			
	public void resetFilter(){
		this.filteredPonies = new LinkedList<DownloadPony>(this.allPonies);
	}
	
	public void filterByName(String name){
		this.filteredPonies.clear();
		for(DownloadPony p : this.allPonies){
			if(p.getName().toLowerCase().contains(name.toLowerCase())){
				this.filteredPonies.add(p);
			}
		}
	}
	
	public void filterByState(int state){
		this.filteredPonies.clear();
		for(DownloadPony p : this.allPonies){
			if(p.getState() == state){
				this.filteredPonies.add(p);
			}
		}
	}
	
	@Override
	public int getCount() {
		return this.filteredPonies.size();
	}
	
	public void sort(boolean ASC){
		if(ASC){
			Collections.sort(this.filteredPonies, new Comparator<DownloadPony>() {
				@Override
				public int compare(DownloadPony a, DownloadPony b) {
					return a.compareTo(b);
				}			
			});
		}else{
			Collections.sort(this.filteredPonies, new Comparator<DownloadPony>() {
				@Override
				public int compare(DownloadPony a, DownloadPony b) {
					return b.compareTo(a);
				}			
			});
		}
	}

	public DownloadPony getItem(int position) {
		if(position > this.filteredPonies.size()) return null;
		return this.filteredPonies.get(position);
	}

	public DownloadPony getItem(String ID) {
		for(DownloadPony p : this.allPonies){
			if(p.getFolder().equals(ID)){
				return p;
			}
		}
		return null;
	}
}