package com.peersync;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import com.commands.ShellConsole;
import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main {

	
	public static void main(String[] args) {

		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\folder\\log.properties");
//		System.setProperty("net.jxta.logging.Logging", "FINEST");
//		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client1";
		
		Constants.getInstance().PEERID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Constants.getInstance().PEERNAME.getBytes());
		DataBaseManager db = DataBaseManager.getInstance();
		db.saveSharedFolder(new SharedFolder("5000", Constants.PsePeerGroupID.toString(), "C:\\PeerSyncTest\\Client1"));
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9789);
		
		
		
		//EventsManager.getEventsManager().startService();
		PeerManager.getInstance();
		ShellConsole s = ShellConsole.getShellConsole();
		s.start();
		
		
	}

}
