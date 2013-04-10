package com.peersync.models;

public class FileWithLocalSource extends AbstractFile{
	


	
	private String localSource;
	
	public FileWithLocalSource(String relFilePath,String fileHash,String sharedFolderUID,String localSource)
	{
		super(relFilePath,fileHash,sharedFolderUID);
		this.setLocalSource(localSource);

	}

	public String getLocalSource() {
		return localSource;
	}

	public void setLocalSource(String localSource) {
		this.localSource = localSource;
	}
	
	

}
