package com.peersync.models;

import com.peersync.data.DataBaseManager;

public class SharedFolder {

	private final String UID;
	private final String peerGroupUID;
	private final String asbolutePath;
	private final String name;





	public SharedFolder(String UID,String peerGroupUID, String absDirPath,String name) {
		this.UID = UID;
		this.peerGroupUID = peerGroupUID;
		asbolutePath = absDirPath;
		this.name=name;
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


	@Override
	public boolean equals(Object obj) {
		return UID.equals(((SharedFolder) obj).UID);
	}

	public String getName() {
		return name;
	}



}
