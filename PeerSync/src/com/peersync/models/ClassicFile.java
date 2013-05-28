package com.peersync.models;


public class ClassicFile {
	protected String sharedFolderUID;
	protected String fileHash;
	protected String relFilePath;
	

	protected String sharedFolderRootPath;
	private long size;
	


	
	
	public ClassicFile(String relFilePath,String fileHash,long fileSize, String sharedFolderUID,String sharedFolderRootPath)
	{
		setRelFilePath(relFilePath);
		this.size = fileSize;
		setFileHash(fileHash);
		setSharedFolderUID(sharedFolderUID);
		this.sharedFolderRootPath=sharedFolderRootPath;
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
		ClassicFile f = (ClassicFile)obj;
		
		return f.fileHash.equals(fileHash)&&f.relFilePath.equals(relFilePath)&&sharedFolderUID.equals(f.sharedFolderUID);
	}




	public String getAbsFilePath() {
		return sharedFolderRootPath+relFilePath;
	}




	public long getFileSize() {
		return size;
	}




	

}
