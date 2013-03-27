package com.peersync.models;

import java.util.ArrayList;

public class ShareFolder {
	
	private final String UID;
	private final String peerGroupUID;
	
	public ShareFolder(String UID, String peerGroupUID) {
		this.UID = UID;
		this.peerGroupUID = peerGroupUID;
	}
	
	public ArrayList<StackVersion> getStackVersionList(){
		return null;//TODO 
	}

	public String getUID() {
		return UID;
	}

	public String getPeerGroupUID() {
		return peerGroupUID;
	}


}
