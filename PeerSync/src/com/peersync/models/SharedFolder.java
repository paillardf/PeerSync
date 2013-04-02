package com.peersync.models;

import java.util.ArrayList;

import com.peersync.events.DataBaseManager;

public class SharedFolder {

	private final String UID;
	private final String asbolutePath;




	public SharedFolder(String UID) {
		this.UID = UID;
		asbolutePath = DataBaseManager.getDataBaseManager().getSharedFolderRootPath(UID);
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
