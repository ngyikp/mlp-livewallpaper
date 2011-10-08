package com.overkill.ponymanager;

import java.text.Collator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.overkill.live.pony.ToolSet;

import android.graphics.Bitmap;

public class DownloadPony implements Comparable<DownloadPony> {	
	private String name;
	private List<String> categories = new LinkedList<String>();
	private String folder;
	private int state;
	private Bitmap image;
	private int size;
	private int totalFileCount;
	private int doneFileCount;
	private long lastUpdate = 0;
		
	public DownloadPony(String name, String folder, int totalFileCount, int size, int state) {
		this.name = name;
		this.folder = folder;
		this.size = size;
		this.totalFileCount = totalFileCount;
		this.doneFileCount = 0;
		this.state = state;
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
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
		return this.getName().equals(((DownloadPony) o).getName());
	}

	@Override
	public int compareTo(DownloadPony another) {
		Collator collator = Collator.getInstance(Locale.ENGLISH);
		collator.setStrength(Collator.SECONDARY);
		return collator.compare(getName(), another.getName());
	}
}
