package com.peersync.models;


public abstract class AbstractFile {
	protected String sharedFolderUID;
	protected String fileHash;
	protected String relFilePath;
	


	
	
	public AbstractFile(String relFilePath,String fileHash,String sharedFolderUID)
	{
		setRelFilePath(relFilePath);
		setFileHash(fileHash);
		setSharedFolderUID(sharedFolderUID);
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


	


	
	@Override
	public boolean equals(Object obj) {
		AbstractFile f = (AbstractFile)obj;
		
		return f.fileHash.equals(fileHash)&&f.relFilePath.equals(relFilePath);
	}

}
