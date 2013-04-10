package com.peersync.models;

import java.io.UnsupportedEncodingException;

import net.jxta.content.ContentID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

public class FileToDownload extends AbstractFile{
	

	

	private ContentID contentID;
	
	
	public FileToDownload(String relFilePath,String fileHash,String sharedFolderUID)
	{
		super(relFilePath,fileHash,sharedFolderUID);

	}
	



	public ContentID getContentID(PeerGroupID groupID) {
		if(contentID==null)
			try {
				contentID = IDFactory.newContentID(groupID, true, fileHash.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		return contentID;
	}
	


	
}
