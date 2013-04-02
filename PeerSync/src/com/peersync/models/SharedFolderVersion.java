package com.peersync.models;

import java.util.ArrayList;

import com.peersync.events.DataBaseManager;

public class SharedFolderVersion {

	private final String UID;
	private ArrayList<StackVersion> stackVersionList;




	public SharedFolderVersion(String UID) {
		this.UID = UID;
		stackVersionList = new ArrayList<StackVersion>();
	}

	public ArrayList<StackVersion> getStackVersionList(){
		return stackVersionList;
	}
	public void addStackVersion(StackVersion stackVersion) {
		this.stackVersionList.add(stackVersion);
		
	}

	public String getUID() {
		return UID;
	}

	
	

	


}
