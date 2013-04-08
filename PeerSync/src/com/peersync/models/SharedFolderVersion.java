package com.peersync.models;

import java.util.ArrayList;
import java.util.Map;

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

	public StackVersion getStackVersion(String stackVersionUID){
		for (StackVersion stackVersion : stackVersionList) {
			if(stackVersion.getUID().compareTo(stackVersionUID)==0)
				return stackVersion;
		}
		
		return new StackVersion(stackVersionUID, 0);
	}

	public int size() {
		return stackVersionList.size();
	}

	


}
