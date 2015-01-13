package com.shiznatix.mediacomrade.android.entities;

public class LastDirectory {
	public int lastDirectoryid;
	public String url;
	public String directory;
	
	public LastDirectory(int lastDirectory, String url, String directory) {
		this.lastDirectoryid = lastDirectory;
		this.url = url;
		this.directory = directory;
	}
}