package com.peersync.models;

public class FileToSync {
	
	private String sharedFolderUID;
	private String fileHash;
	private String relFilePath;
	
	private String localSource = null;
	
	
	public FileToSync(String relFilePath,String fileHash,String sharedFolderUID)
	{
		setRelFilePath(relFilePath);
		setFileHash(fileHash);
		setSharedFolderUID(sharedFolderUID);
	}
	
	public FileToSync(String relFilePath,String fileHash,String sharedFolderUID,String localSource)
	{
		setRelFilePath(relFilePath);
		setFileHash(fileHash);
		setSharedFolderUID(sharedFolderUID);
		setLocalSource(localSource);
	}


	public String getSharedFolderUID() {
		return sharedFolderUID;
	}


	public void setSharedFolderUID(String sharedFolderUID) {
		this.sharedFolderUID = sharedFolderUID;
	}


	public String getFileHash() {
		return fileHash;
	}


	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}


	public String getRelFilePath() {
		return relFilePath;
	}


	public void setRelFilePath(String relFilePath) {
		this.relFilePath = relFilePath;
	}


	public String getLocalSource() {
		return localSource;
	}


	public void setLocalSource(String localSource) {
		this.localSource = localSource;
	}
}
