package com.peersync.models;

import java.io.File;

import net.jxta.document.Element;
import net.jxta.document.StructuredDocument;

import com.peersync.data.DataBaseManager;
import com.peersync.events.DirectoryReader;
public class Event {

	private long m_date;
	private String m_filepath;
	private int m_action;
	private String m_parameters;
	private File m_file;
	private String m_owner;
	private String m_newHash;
	private String m_sharedFolderUID;
	private int m_status;
	private String m_oldHash;
	private int m_isFile;
	
	public static final int STATUS_OK = 0;
	public static final int STATUS_UNSYNC = 1;
	public static final int STATUS_CONFLICT= 2;
	
	public static final int ACTION_CREATE = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_DELETE = 3;
	
	public static final String EVENTID_TAG = "eventID";
	public static final String DATE_TAG = "date";
	public static final String PATH_TAG = "path";
	public static final String ACTION_TAG = "action";
	public static final String PARAMETERS_TAG = "parameters";
	public static final String OWNER_TAG = "owner";
	public static final String NEWHASH_TAG = "new_hash";
	public static final String OLDHASH_TAG = "old_hash";
	public static final String ISFILE_TAG = "isfile";
	// Désormais, se base toujours sur le chemin absolu pour en débuire le sharedFolder auquel il appartient
	// Cela est possible car nous avons décidé que chaque fichier ne peut appartenir qu'a un seul dossier de partage
	// Todo : requete d'update en cas d'apparition d'un nouveau sous dossier de partage dans un dossier de partage (retagger les events avc le new sharedfolder)
	public Event(String shareFolderUID, String filepath,  String newHash,String oldHash,int action,String owner) 
	{

		setDate(System.currentTimeMillis());
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolderUID = shareFolderUID;
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(STATUS_OK);
		m_file = new File(getFilepath());
		m_isFile = m_file.isFile()? 1 : 0;



	}
	// UTILE???
//	public Event(String filepath,String newHash,String oldHash,int action,String owner,int status) 
//	{
//
//		setDate(System.currentTimeMillis());
//		setFilepath(filepath);
//		setAction(action);
//		setOwner(owner);
//		m_sharedFolder = new SharedFolder(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile(filepath));
//		setNewHash(newHash);
//		setOldHash(oldHash);
//		setStatus(status);
//		m_file = new File(getFilepath());
//		m_isFile = m_file.isFile()? 1 : 0;
//
//	}

	public Event(String shareFolderId,long date, String filepath,int isFile,String newHash,String oldHash,int action,String owner,int status) 
	{

		setDate(date);
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		m_sharedFolderUID = shareFolderId;
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
		return SharedFolder.RelativeFromAbsolutePath(m_filepath,
				DataBaseManager.getDataBaseManager().getSharedFolderRootPath(m_sharedFolderUID));

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

	public String getShareFolderUID() {
		return m_sharedFolderUID;
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

	public void attacheToXml(Element element , StructuredDocument resp) {
		Element eventElement = resp.createElement(EVENTID_TAG);
		element.appendChild(eventElement);
		
		eventElement.appendChild(resp.createElement(DATE_TAG, getDate()));
		eventElement.appendChild(resp.createElement(PATH_TAG, getFilepath()));
		eventElement.appendChild(resp.createElement(ACTION_TAG, getAction()));
		eventElement.appendChild(resp.createElement(PARAMETERS_TAG, getParameters()));
		eventElement.appendChild(resp.createElement(OWNER_TAG, getOwner()));
		eventElement.appendChild(resp.createElement(NEWHASH_TAG, getNewHash()));
		eventElement.appendChild(resp.createElement(OLDHASH_TAG, getOldHash()));
		eventElement.appendChild(resp.createElement(ISFILE_TAG, isFile()));
		
	}





}
