package com.peersync.models;

import net.jxta.document.Element;
import net.jxta.document.StructuredDocument;

import com.peersync.data.DataBaseManager;
public class Event {

	private long m_date;
	private String m_relFilePath;
	private int m_action;
	private String m_parameters;
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

	
	public Event(String shareFolderUID, String relFilePath,int is_file,  String newHash,String oldHash,int action,String owner,int status) 
	{

		setDate(System.currentTimeMillis());
		setFilepath(relFilePath);
		setAction(action);
		setOwner(owner);
		m_sharedFolderUID = shareFolderUID;
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(status);
		m_isFile = is_file;



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

	public Event(String shareFolderId,long date, String relFilePath,int isFile,String newHash,String oldHash,int action,String owner,int status) 
	{

		setDate(date);
		setFilepath(relFilePath);
		setAction(action);
		setOwner(owner);
		m_sharedFolderUID = shareFolderId;
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(status);
		m_isFile = isFile; 

	}
	


	public void checkConflict()
	{
		if(getAction()!=ACTION_CREATE)
		{
<<<<<<< HEAD
			Event e = DataBaseManager.getInstance().getLastEventOfAFile(m_filepath);
			if(e.getNewHash()!=getOldHash())
=======
			Event e = DataBaseManager.getInstance().getLastEventOfAFile(m_relFilePath,m_sharedFolderUID);
			//TODO : v�rifier le bien fond�e de la propagation des conflits ( || e.getStatus()==STATUS_CONFLICT )
			if(e!=null &&  (e.getNewHash()!=getOldHash() || e.getStatus()==STATUS_CONFLICT))

>>>>>>> 66987430f29bdb1c31748b8ca4901cc957596d12
			{
				setStatus(STATUS_CONFLICT);
			}
		}
		
	}
	public void save()
	{
		checkConflict();
		DataBaseManager.getInstance().saveEvent(this);

	}

	public int isFile()
	{
		return m_isFile;

	}
	
	
	//On sait jamais, �a peut peut �tre utile^^
	public String getAbsFilePath()
	{
<<<<<<< HEAD
		return SharedFolder.RelativeFromAbsolutePath(m_filepath,
				DataBaseManager.getInstance().getSharedFolderRootPath(m_sharedFolderUID));

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



=======
		return SharedFolder.AbsoluteFromRelativePath(m_relFilePath,DataBaseManager.getInstance().getSharedFolderRootPath(m_sharedFolderUID) );
>>>>>>> 66987430f29bdb1c31748b8ca4901cc957596d12

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
		return m_relFilePath;
	}


	public void setFilepath(String m_filepath) {
		this.m_relFilePath = m_filepath;
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
