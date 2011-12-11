package com.overkill.ponymanager.pony;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

public class SortablePonyAdapter extends ArrayAdapter<DownloadPony>{

	protected Context context;
	protected int layoutResourceId;
	protected int textViewResourceId;
	protected List<DownloadPony> allPonies;
	protected List<DownloadPony> filteredPonies;		
	private HashMap<String, Boolean> categories = new HashMap<String, Boolean>();
	
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
	
	@Override
	public void remove(DownloadPony object) {
		this.allPonies.remove(object);
		this.filteredPonies.remove(object);
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
	
	public void loadUniqueCategegories(){
		categories = new HashMap<String, Boolean>();
		for(DownloadPony p : this.allPonies){
			for(String s : p.getCategories()){
				s = s.trim().toLowerCase();
				if(categories.containsKey(s) == false){
					categories.put(s, true);
				}
			}
		}
	}
	
	public HashMap<String, Boolean> getCategories(){
		return categories;
	}
	
	public String[] getCategoryNames(){
		return (String[])this.categories.keySet().toArray(new String[this.categories.keySet().size()]);
	}
	
	public String[] getCategoryNamesWithCount(){
		String[] names = this.getCategoryNames();
		for(int i = 0; i < names.length; i++){
			names[i] = names[i] + " (" + this.countPoniesInCategory(names[i]) + ")";
		}
		return names;
	}
	
	public boolean[] getCategoryStates(){
		Boolean[] values = this.categories.values().toArray(new Boolean[this.categories.size()]);
		boolean[] r = new boolean[values.length];
		for(int i = 0; i < values.length; i++){
			r[i] = values[i].booleanValue();
		}
		return r;
	}
	
	public void setCategoryFilter(String category, boolean state){
		this.categories.put(category, state);
	}

	public int countPoniesInCategory(String category){
		int c = 0;
		for(DownloadPony p : this.allPonies){
			if(containsIgnoreCase(p.getCategories(), category)){
				c++;
			}
			Log.i("countPoniesInCategory", "checking " + p.getName() + " for " + category + " => " + c);
		}
		return c;
	}
	
	public void filterByCategory() {
		this.filteredPonies.clear();
		for(DownloadPony p : this.allPonies){
			for(String c : p.getCategories()){
				c = c.trim().toLowerCase();
				if(this.categories.get(c) == true){
					this.filteredPonies.add(p);
					break;
				}
			}
		}
	}
	
	private boolean containsIgnoreCase(List <String> l, String s){
		 Iterator <String> it = l.iterator();
		 while(it.hasNext()){
			 if(it.next().equalsIgnoreCase(s))
				 return true;
		 }
		 return false;
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
					if(a == null || b == null)
						return 0;
					return a.compareTo(b);
				}			
			});
		}else{
			Collections.sort(this.filteredPonies, new Comparator<DownloadPony>() {
				@Override
				public int compare(DownloadPony a, DownloadPony b) {
					if(a == null || b == null)
						return 0;
					return b.compareTo(a);
				}			
			});
		}
	}

	public List<DownloadPony> getAllItems(){
		return this.allPonies;
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