package com.peersync.tools;

import net.jxta.peer.PeerID;


public class Constants {
	public String PEERNAME;
	public String CONF_FOLDER = "conf";
	//public String PREFERENCES_PATH = CONF_FOLDER+"/"+PEERNAME;
	public PeerID PEERID;
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
