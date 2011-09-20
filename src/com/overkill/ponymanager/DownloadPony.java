package com.overkill.ponymanager;

import android.graphics.Bitmap;

public class DownloadPony {	
	private String name;
	private String folder;
	private int state;
	private Bitmap image;
	private int size;
	private int totalFileCount;
	private int doneFileCount;
		
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
		return formatBytes(size);
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

	private String formatBytes(float bytes) {
	    String units[] = {"B", "KB", "MB", "GB", "TB"};
	  
	    bytes = Math.max(bytes, 0);
	    int pow = (int) Math.floor(((bytes != 0) ? Math.log(bytes) : 0) / Math.log(1024));
	    pow = Math.min(pow, units.length - 1);
	  
	    bytes /= Math.pow(1024, pow);
	  
	    return  String.format("%.2f", bytes) + ' ' + units[pow];
	}
	
}
