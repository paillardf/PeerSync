package com.peersync;

import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main1 {

	
	public static void main(String[] args) {
		
Constants.getInstance().PEERNAME="client2";
		
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9788);
		PeerManager.getInstance();
		
		
		
	}

}
