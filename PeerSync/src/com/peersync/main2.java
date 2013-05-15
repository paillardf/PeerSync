package com.peersync;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManager;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main2 {

	
	public static void main(String[] args) {
		Constants.getInstance().PEERNAME = "client3";
		Constants.getInstance().PEERID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Constants.getInstance().PEERNAME.getBytes());

		DataBaseManager db = DataBaseManager.getInstance();
		db.saveSharedFolder(new SharedFolder("5000", Constants.PsePeerGroupID.toString(), "C:\\PeerSyncTest\\Client3"));
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9787);
		EventsManager.getEventsManager().startService();
		PeerManager.getInstance();
	}

}
