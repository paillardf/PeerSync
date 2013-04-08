package com.peersync;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManagerThread;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main2 {

	
	public static void main(String[] args) {
		Constants.getInstance().PEERNAME = "client3";
		DataBaseManager db = DataBaseManager.getInstance();
		db.saveSharedFolder(new SharedFolder("5000", "", "C:\\Users\\Florian\\Desktop\\Share test\\Client3"));
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9787);
		EventsManagerThread.getEventsManagerThread().start();
		PeerManager.getInstance();
	}

}
