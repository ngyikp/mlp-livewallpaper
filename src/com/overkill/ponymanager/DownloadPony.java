package com.overkill.ponymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.overkill.live.pony.R;
import com.overkill.live.pony.ToolSet;
import com.overkill.live.pony.engine.Pony;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DownloadPony implements Comparable<DownloadPony> {	
	private String name;
	private List<String> categories = new LinkedList<String>();
	private String folder;
	private int state;
	private Bitmap image;
	private long size;
	private int totalFileCount;
	private int doneFileCount;
	private long lastUpdate = 0;
		
	public DownloadPony(String name, String folder, int totalFileCount, long size, int state) {
		this.name = name;
		this.folder = folder;
		this.size = size;
		this.totalFileCount = totalFileCount;
		this.doneFileCount = 0;
		this.state = state;
	}
	
	public DownloadPony(String name, String folder, boolean state) {
		this.name = name;
		this.folder = folder;
		this.doneFileCount = 0;
		this.state = state ? R.string.pony_state_installed : R.string.pony_state_not_installed;
		this.image = BitmapFactory.decodeFile(new File(this.folder, "preview.gif").getPath());
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getTotalFileCount() {
		return totalFileCount;
	}

	public void setTotalFileCount(int totalFileCount) {
		this.totalFileCount = totalFileCount;
	}

	public int getDoneFileCount() {
		return doneFileCount;
	}

	public void setDoneFileCount(int doneFileCount) {
		this.doneFileCount = doneFileCount;
	}

	public String getBytes(){
		return ToolSet.formatBytes(size);
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}	
		
	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	
	public void addCategory(String category) {
		this.categories.add(category);
	}


	@Override
	public boolean equals(Object o) {
		return this.getFolder().equals(((DownloadPony) o).getFolder());
	}

	@Override
	public int compareTo(DownloadPony another) {
		Collator collator = Collator.getInstance(Locale.ENGLISH);
		collator.setStrength(Collator.SECONDARY);
		return collator.compare(getName(), another.getName());
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	public static DownloadPony fromINI(File folder){
		String name = "";
		String[] categories = null;		
    	try{
		    String line = "";
		    File iniFile = new File(folder, "pony.ini");
		    if(iniFile.exists() == false)
			    iniFile = new File(folder, "Pony.ini");
		    BufferedReader br = null;
		    InputStreamReader is = new InputStreamReader(new FileInputStream(iniFile), "UTF-8");
		    if(is.read() == 0x0fffd){		    	
		    	br = new BufferedReader(new InputStreamReader(new FileInputStream(iniFile), "UTF-16"));
		    	Log.i("Pony", "opening " + iniFile.getPath() + " with UTF-16");
		    } else {
		    	br = new BufferedReader(new InputStreamReader(new FileInputStream(iniFile), "UTF-8"));
		    	Log.i("Pony", "opening " + iniFile.getPath() + " with UTF-8");
		    }
		    is.close();
		    while ((line = br.readLine()) != null) {	
		    	if(line.startsWith("'")) continue; //skip comments
			    if(line.toLowerCase().startsWith("name,")){ name = line.substring("name,".length()); continue;}
			    if(line.toLowerCase().startsWith("categories,")){
			    	String category = line.substring("categories,".length());
			        categories = category.replace("\"", "").split(",");
			        continue;
			    }
		    }
			DownloadPony p = new DownloadPony(name, folder.getName(), ToolSet.getFolderItemCount(folder), ToolSet.getFolderSize(folder), R.string.pony_state_local_only);
			p.setCategories(Arrays.asList(categories));
			p.setLastUpdate(0);
			return p;
    	}catch (Exception e) {
			return null;
		}
	}
}
