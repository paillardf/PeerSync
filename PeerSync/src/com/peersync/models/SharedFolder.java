package com.peersync.models;

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
		if(relative.equals("\\"))
			return baseDir;
		return baseDir.concat(relative);
	}

	public static String RelativeFromAbsolutePath(String absolute,String baseDir)
	{
		String res = absolute.replace(baseDir, "");
		if(res.length()==0)
			res="\\";
		return res;
	}

	public String getAbsFolderRootPath()
	{

		return asbolutePath;
	}

	

	


}
