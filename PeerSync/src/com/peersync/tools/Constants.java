package com.peersync.tools;

import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;


public class Constants {
	public String PEERNAME;
	public String CONF_FOLDER = "conf";
	//public String PREFERENCES_PATH = CONF_FOLDER+"/"+PEERNAME;
	public PeerID PEERID;
	
	public static final String PsePeerGroupName = "SECURE PeerGroup";
	public static final PeerGroupID PsePeerGroupID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, PsePeerGroupName.getBytes());

	
	
	
	
	private static Constants instance;
	
	public static Constants getInstance(){
		if(instance==null)
			instance = new Constants();
		return instance;
	}

	public String PREFERENCES_PATH() {
		return CONF_FOLDER+"/"+PEERNAME;
	}
	
	

}
