package com.peersync.events;

import java.io.File;

import com.peersync.models.ShareFolder;
public class Event {

	private long m_date;
	private String m_filepath;
	private int m_action;
	private String m_parameters;
	private File m_file;
	private String m_owner;
	private String m_hash;
	private ShareFolder m_sharedFolder;



	public Event(long d,String filepath,int action,String owner,String uuidShareFolder) throws Exception 
	{

		setDate(d);
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolder = new ShareFolder(uuidShareFolder);
		openFile();

	}

	public Event(String filepath,String hash,int action,String owner,String uuidShareFolder) 
	{

		setDate(System.currentTimeMillis());
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolder = new ShareFolder(uuidShareFolder);
		setHash(hash);
		m_file = new File(getFilepath());


	}



	public void save()
	{
		DataBaseManager.getDataBaseManager().saveEvent(this);

	}

	public int isFile()
	{
		return m_file.isFile()? 1 : 0;
		
	}

	public String getRelPath()
	{
		return ShareFolder.RelativeFromAbsolutePath(m_filepath,m_sharedFolder.getAbsFolderRootPath());
		
	}
	private void openFile() throws Exception
	{
		m_file = new File(getFilepath());

		if(m_file.exists() && (m_file.isFile() || m_file.isDirectory()))
		{
			setHash(DirectoryReader.calculateHash(m_file));


		}
		else
		{
			throw new Exception("File Error");

		}




	}
	public long getDate() {
		return m_date;
	}


	public void setDate(long m_date) {
		this.m_date = m_date;
	}


	public int getAction() {
		return m_action;
	}


	public void setAction(int m_action) {
		this.m_action = m_action;
	}


	public String getFilepath() {
		return m_filepath;
	}


	public void setFilepath(String m_filepath) {
		this.m_filepath = m_filepath;
	}


	public String getParameters() {
		return m_parameters;
	}


	public void setParameters(String m_parameters) {
		this.m_parameters = m_parameters;
	}


	public String getOwner() {
		return m_owner;
	}


	public void setOwner(String owner) {
		this.m_owner = owner;
	}

	public String getHash() {
		return m_hash;
	}

	private void setHash(String m_hash) {
		this.m_hash = m_hash;
	}

	public ShareFolder getShareFolder() {
		return m_sharedFolder;
	}





}
