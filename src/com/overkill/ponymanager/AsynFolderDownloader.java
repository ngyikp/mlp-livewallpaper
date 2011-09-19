package com.overkill.ponymanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

public class AsynFolderDownloader extends Thread {

	public interface onDownloadListener{
		void onDownloadStart(int position);
		void onDownloadChanged(int position, int filesDone);
		void onDownloadDone(int position);
		void onDownloadError(String error);
	}
	
	private String remotePath;
	private File localPath;
	private int position;
	private onDownloadListener listener;
	
	public AsynFolderDownloader(String remotePath, File localPath, int position, onDownloadListener listener) {
		this.localPath = localPath;
		this.remotePath = remotePath;
		if(this.remotePath.endsWith("/") == false)
			this.remotePath = this.remotePath + "/";
		this.position = position;
		this.listener = listener;
	}	
	
	@Override
	public void run() {
		if(localPath.isDirectory() == false){
			localPath.mkdir();
		}
		int fileCount = 0;
		this.listener.onDownloadStart(this.position);
		URL base;
		try {
			base = new URL(remotePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(base.openStream()));
			String line = "";
			while ((line = br.readLine()) != null) {		 
				line = line.trim();
				if(line.startsWith("<li") == false)
					continue;
				int start = line.indexOf("<li><a href=\"") + "<li><a href=\"".length();
				int end = line.indexOf("\"", start);
				String fileName = line.substring(start, end);
				if(fileName.startsWith("."))
					continue;
				
				URL remoteFile = new URL(remotePath + fileName);
				File localFile = new File(localPath, fileName);
                BufferedInputStream bis = new BufferedInputStream(remoteFile.openStream());
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                        baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(localFile);
                fos.write(baf.toByteArray());
                fos.close();
                
                fileCount++;
                this.listener.onDownloadChanged(this.position, fileCount);
				
			}
			this.listener.onDownloadDone(this.position);
		} catch (MalformedURLException e) {
			this.listener.onDownloadError(e.getMessage());
		} catch (IOException e) {
			this.listener.onDownloadError(e.getMessage());
		}
	}
	
	
}
