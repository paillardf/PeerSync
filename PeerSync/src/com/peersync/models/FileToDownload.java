package com.peersync.models;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.content.ContentID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

public class FileToDownload extends ClassicFile{




	private ContentID contentID;


	public FileToDownload(String relFilePath,String fileHash,String sharedFolderUID,String sharedFolderRootPath, String peerGroupID)
	{
		super(relFilePath,fileHash,sharedFolderUID,sharedFolderRootPath);
		try {
			contentID = IDFactory.newContentID((PeerGroupID) IDFactory.fromURI(new URI(peerGroupID)), true, fileHash.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			e.printStackTrace();
		}

	}




	public ContentID getContentID() {
		return contentID ;

	}




}
