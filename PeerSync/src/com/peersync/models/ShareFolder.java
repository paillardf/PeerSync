package com.peersync.models;

import java.util.ArrayList;

import com.peersync.events.DataBaseManager;

public class ShareFolder {

	private final String UID;
	private final String asbolutePath;
	private ArrayList<StackVersion> stackVersionList;




	public ShareFolder(String UID) {
		this.UID = UID;
		stackVersionList = new ArrayList<StackVersion>();
		asbolutePath = DataBaseManager.getDataBaseManager().getSharedFolderRootPath(UID);
	}

	public ArrayList<StackVersion> getStackVersionList(){
		return null;//TODO 
	}
	public void addStackVersion(StackVersion stackVersion) {
		this.stackVersionList.add(stackVersion);
		
	}

	public String getUID() {
		return UID;
	}

	public static String AbsoluteFromRelativePath(String relative,String baseDir)
	{
		return baseDir.concat(relative);
	}

	public static String RelativeFromAbsolutePath(String absolute,String baseDir)
	{
		return absolute.replace(baseDir, "");
	}

	public String getAbsFolderRootPath()
	{

		return asbolutePath;
	}

	

	


}
