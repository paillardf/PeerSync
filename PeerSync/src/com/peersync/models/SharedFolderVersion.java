package com.peersync.models;

import java.util.ArrayList;

public class SharedFolderVersion {

	private final String UID;
	private String name;
	private ArrayList<StackVersion> stackVersionList;


	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public SharedFolderVersion(String UID,String name) {
		this.UID = UID;
		this.name=name;
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
