package com.peersync;

import com.peersync.data.DataBaseManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main {

	
	public static void main(String[] args) {
		Constants.getInstance().PEERNAME = "client4";
		
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9789);
		System.out.println(pref.getPort());
		DataBaseManager.getInstance();
		//PeerManager.getInstance();
		
		
	}

}
