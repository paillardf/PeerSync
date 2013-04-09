package com.peersync.models;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.content.ContentID;
import net.jxta.id.IDFactory;

public class FileToSync {
	
	private String sharedFolderUID;
	private String fileHash;
	private String relFilePath;
	
	private String localSource = null;
	private ContentID contentID;
	
	
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

	public ContentID getContentID() {
		if(contentID==null)
			try {
				contentID = (ContentID) IDFactory.fromURI(new URI("urn:jxta:uuid-"+fileHash));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		return contentID;
	}
	
	@Override
	public boolean equals(Object obj) {
		FileToSync f = (FileToSync)obj;
		
		return f.fileHash.equals(fileHash)&&f.relFilePath.equals(relFilePath);
	}

	
}
