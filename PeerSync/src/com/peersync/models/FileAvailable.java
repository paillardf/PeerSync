package com.peersync.models;

// TODO Méthode(s) de dbmanager retournant une arraylist de ceci
public class FileAvailable {

	private String absFilePath;
	private String hash;
	
	
	public FileAvailable(String absFilePath,String hash)
	{
		this.setAbsFilePath(absFilePath);
		this.setHash(hash);
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
}
