package com.peersync.models;

import com.peersync.data.DataBaseManager;

public class FileInfo {
	
	private String absFilePath;
	private long updateDate;
	private String hash;
	
	public FileInfo(String absFilePath, long updateDate,String hash)
	{
		this.absFilePath = absFilePath;
		this.updateDate = updateDate;
		this.hash = hash;
	}

	public String getAbsFilePath() {
		return absFilePath;
	}

	public void setAbsFilePath(String absFilePath) {
		this.absFilePath = absFilePath;
	}

	public long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(long updateDate) {
		this.updateDate = updateDate;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void save() {
		DataBaseManager.getInstance().saveFileInfo(this);
		
	}

	
	
}
