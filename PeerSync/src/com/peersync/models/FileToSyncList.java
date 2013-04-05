package com.peersync.models;

import java.util.ArrayList;

import com.peersync.data.DataBaseManager;

public class FileToSyncList {
	
	private ArrayList<FileToSync> filesToDownload;
	private ArrayList<FileToSync> filesWithLocalSource;
	
	
	public void reload()
	{
		refreshFilesToDownload();
		refreshFilesWithLocalSource();
		
	}
	
	public void refreshFilesToDownload()
	{
		filesToDownload = DataBaseManager.getDataBaseManager().getFilesToDownload();
	}
	
	public void refreshFilesWithLocalSource()
	{
		filesWithLocalSource = DataBaseManager.getDataBaseManager().getFilesWithLocalSource();
	}

	public ArrayList<FileToSync> getFilesWithLocalSource() {
		return filesWithLocalSource;
	}



	public ArrayList<FileToSync> getFilesToDownload() {
		return filesToDownload;
	}
	
	public ArrayList<FileToSync> getFilesToDownload(String sharedFolderUID) {
		ArrayList<FileToSync> res = new ArrayList<FileToSync>();
		for(FileToSync ft : filesToDownload)
		{
			if(ft.getSharedFolderUID().equals(sharedFolderUID))
				res.add(ft);
		}
		return res;
	}
	
	public ArrayList<FileToSync> getFilesWithLocalSource(String sharedFolderUID) {
		ArrayList<FileToSync> res = new ArrayList<FileToSync>();
		for(FileToSync ft : filesWithLocalSource)
		{
			if(ft.getSharedFolderUID().equals(sharedFolderUID))
				res.add(ft);
		}
		return res;
	}



}
