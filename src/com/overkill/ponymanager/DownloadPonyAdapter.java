package com.overkill.ponymanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.overkill.live.pony.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class DownloadPonyAdapter extends SortablePonyAdapter{
	
	private HashMap<String, Boolean> categories = new HashMap<String, Boolean>();
	
	public DownloadPonyAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public DownloadPonyAdapter(Context context, int textViewResourceId,	List<DownloadPony> objects) {
		super(context, textViewResourceId, objects);
		this.loadUniqueCategegories();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)super.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(super.layoutResourceId, parent, false);
        }
        DownloadPony p = super.filteredPonies.get(position);
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
	
	public void loadUniqueCategegories(){
		categories = new HashMap<String, Boolean>();
		for(DownloadPony p : super.allPonies){
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
		super.filteredPonies.clear();
		for(DownloadPony p : this.allPonies){
			for(String c : p.getCategories()){
				c = c.trim().toLowerCase();
				if(this.categories.get(c) == true){
					super.filteredPonies.add(p);
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
}