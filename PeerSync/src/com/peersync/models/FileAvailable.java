package com.peersync.models;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.content.ContentID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;


public class FileAvailable {

	private String absFilePath;
	private String hash;
	private ContentID contentID;
	
	
	public FileAvailable(String absFilePath,String hash, String peerGroupID)
	{
		this.setAbsFilePath(absFilePath);
		this.setHash(hash);
		try {
			contentID = IDFactory.newContentID((PeerGroupID) IDFactory.fromURI(new URI(peerGroupID)), true, hash.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			e.printStackTrace();
		}
		
	}


	public String getAbsFilePath() {
		return absFilePath;
	}


	public void setAbsFilePath(String absFilePath) {
		this.absFilePath = absFilePath;
	}


	public String getHash() {
		return hash;
	}


	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public ContentID getContentID() {
		return contentID ;

	}
	
	@Override
	public boolean equals(Object obj) {
		FileAvailable f = (FileAvailable)obj;
		
		return f.hash.equals(hash)&&f.absFilePath.equals(absFilePath);
	}
	
}
