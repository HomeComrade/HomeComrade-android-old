package com.shiznatix.mediacomrade.android.entities;

import java.util.ArrayList;

public class Server {
	public int serverid;
	public String url;
	public ArrayList<String> options = new ArrayList<String>();
	
	public Server(int serverid, String url) {
		this.serverid = serverid;
		this.url = url;
	}
	
	public boolean hasOption(String option) {
		if (null == this.options) {
			return false;
		}
		
		return this.options.contains(option);
	}
}
