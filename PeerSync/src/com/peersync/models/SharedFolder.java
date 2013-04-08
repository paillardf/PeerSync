package com.peersync.models;

import com.peersync.data.DataBaseManager;

public class SharedFolder {

	private final String UID;
	private final String peerGroupUID;
	private final String asbolutePath;




	public SharedFolder(String UID) {
		this.UID = UID;
		asbolutePath = DataBaseManager.getInstance().getSharedFolderRootPath(UID);
		
		this.peerGroupUID = DataBaseManager.getInstance().getSharedFolderPeerGroup(UID);;
	}

	public SharedFolder(String UID,String peerGroupUID, String absDirPath) {
		this.UID = UID;
		this.peerGroupUID = peerGroupUID;
		asbolutePath = absDirPath;
	}

	public String getUID() {
		return UID;
	}

	public static String AbsoluteFromRelativePath(String relative,String baseDir)
	{
		if(relative == null || baseDir == null || relative.isEmpty() || baseDir.isEmpty()	)
			return null;
		if(relative.equals("\\"))
			return baseDir;
		return baseDir.concat(relative);
	}

	public static String RelativeFromAbsolutePath(String absolute,String baseDir)
	{
		if(absolute == null || baseDir == null || absolute.isEmpty() || baseDir.isEmpty()	)
			return null;
		String res = absolute.replace(baseDir, "");
		if(res.length()==0)
			res="\\";
		return res;
	}

	public String getAbsFolderRootPath()
	{

		return asbolutePath;
	}

	public String getPeerGroupUID() {
		return peerGroupUID;
	}






}
