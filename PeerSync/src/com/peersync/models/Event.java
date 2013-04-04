package com.peersync.models;

import java.io.File;

import com.peersync.events.DataBaseManager;
import com.peersync.events.DirectoryReader;
public class Event {

	private long m_date;
	private String m_filepath;
	private int m_action;
	private String m_parameters;
	private File m_file;
	private String m_owner;
	private String m_newHash;
	private SharedFolder m_sharedFolder;
	private int m_status;
	private String m_oldHash;
	private int m_isFile;
	
	private static final int STATUS_OK = 0;
	private static final int STATUS_UNSYNC = 1;
	private static final int STATUS_CONFLICT= 2;
	
	public static final int ACTION_CREATE = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_DELETE = 3;



	// Désormais, se base toujours sur le chemin absolu pour en débuire le sharedFolder auquel il appartient
	// Cela est possible car nous avons décidé que chaque fichier ne peut appartenir qu'a un seul dossier de partage
	// Todo : requete d'update en cas d'apparition d'un nouveau sous dossier de partage dans un dossier de partage (retagger les events avc le new sharedfolder)
	public Event(String filepath,String newHash,String oldHash,int action,String owner) 
	{

		setDate(System.currentTimeMillis());
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolder = new SharedFolder(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile(filepath));
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(STATUS_OK);
		m_file = new File(getFilepath());
		m_isFile = m_file.isFile()? 1 : 0;



	}
	
	public Event(String filepath,String newHash,String oldHash,int action,String owner,int status) 
	{

		setDate(System.currentTimeMillis());
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolder = new SharedFolder(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile(filepath));
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(status);
		m_file = new File(getFilepath());
		m_isFile = m_file.isFile()? 1 : 0;

	}

	public Event(long date,String filepath,int isFile,String newHash,String oldHash,int action,String owner,int status) 
	{

		setDate(date);
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolder = new SharedFolder(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile(filepath));
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(status);
		m_file = new File(getFilepath());
		m_isFile = isFile; 

	}
	


	public void checkConflict()
	{
		if(getAction()!=ACTION_CREATE)
		{
			Event e = DataBaseManager.getDataBaseManager().getLastEventOfAFile(m_filepath);
			if(e.getNewHash()!=getOldHash())
			{
				setStatus(STATUS_CONFLICT);
			}
		}
		
	}
	public void save()
	{
		checkConflict();
		DataBaseManager.getDataBaseManager().saveEvent(this);

	}

	public int isFile()
	{
		return m_isFile;

	}

	public String getRelPath()
	{
		return SharedFolder.RelativeFromAbsolutePath(m_filepath,m_sharedFolder.getAbsFolderRootPath());

	}
	private void openFile() throws Exception
	{
		m_file = new File(getFilepath());

		if(m_file.exists() && (m_file.isFile() || m_file.isDirectory()))
		{
			setNewHash(DirectoryReader.calculateHash(m_file));


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

	public String getNewHash() {
		return m_newHash;
	}

	private void setNewHash(String m_hash) {
		this.m_newHash = m_hash;
	}

	public SharedFolder getShareFolder() {
		return m_sharedFolder;
	}



	public int getStatus() {
		return m_status;
	}



	public void setStatus(int m_status) {
		this.m_status = m_status;
	}



	public String getOldHash() {
		return m_oldHash;
	}



	public void setOldHash(String m_oldHash) {
		this.m_oldHash = m_oldHash;
	}





}
