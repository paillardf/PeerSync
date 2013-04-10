package com.peersync.models;

import java.util.ArrayList;

import com.peersync.data.DataBaseManager;

public class FileToSyncList {
	
	private ArrayList<FileToDownload> filesToDownload;
	private ArrayList<FileWithLocalSource> filesWithLocalSource;
	private String peerGroupId;
	
	public FileToSyncList(String peerGroupId)
	{
		this.peerGroupId = peerGroupId;
	}
	public void reload()
	{
		refreshFilesToDownload();
		refreshFilesWithLocalSource();
		
	}
	
	public void refreshFilesToDownload()
	{
		filesToDownload = DataBaseManager.getInstance().getFilesToDownload(peerGroupId);
	}
	
	public void refreshFilesWithLocalSource()
	{
		filesWithLocalSource = DataBaseManager.getInstance().getFilesWithLocalSource(peerGroupId);
	}

	public ArrayList<FileWithLocalSource> getFilesWithLocalSource() {
		return filesWithLocalSource;
	}



	public ArrayList<FileToDownload> getFilesToDownload() {
		return filesToDownload;
	}
	
	public ArrayList<FileToDownload> getFilesToDownloadInASharedFolder(String sharedFolderUID) {
		ArrayList<FileToDownload> res = new ArrayList<FileToDownload>();
		for(FileToDownload ft : filesToDownload)
		{
			if(ft.getSharedFolderUID().equals(sharedFolderUID))
				res.add(ft);
		}
		return res;
	}
	
	public ArrayList<FileWithLocalSource> getFilesWithLocalSourceInASharedFolder(String sharedFolderUID) {
		ArrayList<FileWithLocalSource> res = new ArrayList<FileWithLocalSource>();
		for(FileWithLocalSource ft : filesWithLocalSource)
		{
			if(ft.getSharedFolderUID().equals(sharedFolderUID))
				res.add(ft);
		}
		return res;
	}
	
	

	public String getPeerGroupId() {
		return peerGroupId;
	}

	public void setPeerGroupId(String peerGroupId) {
		this.peerGroupId = peerGroupId;
	}



}
