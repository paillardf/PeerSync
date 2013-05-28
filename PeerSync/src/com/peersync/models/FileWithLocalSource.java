package com.peersync.models;

public class FileWithLocalSource extends ClassicFile{
	


	
	private String localSourcePath;
	
	public FileWithLocalSource(String relFilePath,String fileHash,long fileSize, String sharedFolderUID,String sharedFolderRootPath,String localSourcePath)
	{
		super(relFilePath,fileHash,fileSize,sharedFolderUID,sharedFolderRootPath);
		this.setLocalSourcePath(localSourcePath);

	}

	public String getLocalSourcePath() {
		return localSourcePath;
	}

	public void setLocalSourcePath(String localSource) {
		this.localSourcePath = localSource;
	}
	
	

}
