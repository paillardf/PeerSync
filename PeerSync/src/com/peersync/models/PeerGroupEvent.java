package com.peersync.models;

import net.jxta.peergroup.PeerGroupID;

public class PeerGroupEvent {
	
	public static final int RDV_CONNECTION = 0;
	public static final int STACK_UPDATE = 1;

	
	private int eventID;
	private Object object;
	private PeerGroupID peerGroupID;
	
	public PeerGroupEvent(int eventID, PeerGroupID peerGroupID, Object object) {
		this.eventID = eventID;
		this.object = object;
		this.peerGroupID = peerGroupID;
	}

	public PeerGroupID getPeerGroupID() {
		return peerGroupID;
	}


	public Object getObject() {
		return object;
	}



	public int getID() {
		return eventID;
	}




}
