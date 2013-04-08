package com.peersync;

import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main2 {

	
	public static void main(String[] args) {
		Constants.getInstance().PEERNAME="client3";
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9787);
		PeerManager.getInstance();
	}

}
