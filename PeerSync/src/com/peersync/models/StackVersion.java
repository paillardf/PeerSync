package com.peersync.models;

public class StackVersion {
	
	private final long lastUpdate;
	private final String UID;
	
	public StackVersion(String UID, long lastUpdate) {
		this.lastUpdate = lastUpdate;
		this.UID = UID;
	}

	public String getUID() {
		return UID;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

}
