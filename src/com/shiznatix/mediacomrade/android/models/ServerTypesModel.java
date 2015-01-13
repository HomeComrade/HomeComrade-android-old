package com.shiznatix.mediacomrade.android.models;

import com.shiznatix.mediacomrade.android.R;

public class ServerTypesModel {
	static public String TOTEM_SERVER = "totem";
	static public String OMXPLAYER_SERVER = "omxplayer";
	
	public ServerTypesModel() {
		
	}
	
	public boolean isValidServerType(String serverType) {
		if (serverType.equals(TOTEM_SERVER)) {
			return true;
		}
		else if (serverType.equals(OMXPLAYER_SERVER)) {
			return true;
		}
		
		return false;
	}
	
	public int getMenuForRemote(String serverType) {
		if (serverType.equals(TOTEM_SERVER)) {
			return R.menu.remote_totem;
		}
		else if (serverType.equals(OMXPLAYER_SERVER)) {
			return R.menu.remote_omxplayer;
		}
		
		return R.menu.remote_totem;
	}
}