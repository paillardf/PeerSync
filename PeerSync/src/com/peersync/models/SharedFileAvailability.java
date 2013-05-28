package com.peersync.models;

import com.peersync.network.content.model.FileAvailability;

public class SharedFileAvailability {

	private long fileSize;
	private FileAvailability fileAvailability;
	private long lastModif;
	private String absPath;
	
	public SharedFileAvailability(FileAvailability fa,String absPath,long fs,long lastModif)
	{
		this.fileAvailability=fa;
		this.absPath=absPath;
		this.fileSize=fs;
		this.lastModif=lastModif;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public FileAvailability getFileAvailability() {
		return fileAvailability;
	}
	public void setFileAvailability(FileAvailability fileAvailability) {
		this.fileAvailability = fileAvailability;
	}
	public long getLastModif() {
		return lastModif;
	}
	public void setLastModif(long lastModif) {
		this.lastModif = lastModif;
	}
	
	
	public String getAbsPath() {
		return absPath;
	}
	public void setAbsPath(String absPath) {
		this.absPath = absPath;
	}
	
	public String getHash()
	{
		return fileAvailability.getHash();
	}
	

}
