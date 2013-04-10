package com.peersync.models;

public class FileWithLocalSource extends ClassicFile{
	


	
	private String localSourcePath;
	
	public FileWithLocalSource(String relFilePath,String fileHash,String sharedFolderUID,String sharedFolderRootPath,String localSourcePath)
	{
		super(relFilePath,fileHash,sharedFolderUID,sharedFolderRootPath);
		this.setLocalSourcePath(localSourcePath);

	}

	public String getLocalSourcePath() {
		return localSourcePath;
	}

	public void setLocalSourcePath(String localSource) {
		this.localSourcePath = localSource;
	}
	
	

}
