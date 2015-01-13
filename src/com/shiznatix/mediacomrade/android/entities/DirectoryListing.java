package com.shiznatix.mediacomrade.android.entities;

public class DirectoryListing {
	public String name;
	public boolean isDir;
	public boolean isPlayable;
	public boolean isSelected = false;
	
	public DirectoryListing(String name, String type, boolean isPlayable) {
		this.name = name;
		this.isDir = type.equals("dir");
		this.isPlayable = isPlayable;
	}
}