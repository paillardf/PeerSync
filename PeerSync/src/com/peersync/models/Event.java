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
	private long length;

	// 0 -> 9 fichier présent physiquement dans le sharedFolder
	public static final int MIN_STATUS_LOCAL = 0;
	public static final int MAX_STATUS_LOCAL = 9;
	public static final int STATUS_LOCAL_OK = 0;
	public static final int STATUS_LOCAL_FORCEOK= 1; //En cas de résolution de conflit, on force l'événement à être OK.
	public static final int STATUS_LOCAL_CONFLICT= 2;
	
	
	// > 9 evenements non encore synchronisé physiquement
	public static final int MIN_STATUS_UNSYNC = 10;
	public static final int MAX_STATUS_UNSYNC = 11;
	public static final int STATUS_UNSYNC = 10;
	public static final int STATUS_UNSYNC_CONFLICT = 11;

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
	public static final String FILESIZE_TAG = "filesize";


	//	public Event(String shareFolderUID, String relFilePath,int is_file,  String newHash,String oldHash,int action,int status) 
	//	{
	//		this(shareFolderUID, )
	//		setDate(System.currentTimeMillis());
	//		setFilepath(relFilePath);
	//		setAction(action);
	//		setOwner(Constants.getInstance().PEERID.toString());
	//		m_sharedFolderUID = shareFolderUID;
	//		setNewHash(newHash);
	//		setOldHash(oldHash);
	//		setStatus(status);
	//		m_isFile = is_file;
	//
	//
	//
	//	}
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

	public Event(String shareFolderUID,long date, String relFilePath,long length,int isFile,String newHash,String oldHash,int action,String owner,int status) 
	{
		relFilePath=SharedFolder.reformatFilePath(relFilePath);
		setDate(date);
		setFilepath(relFilePath);
		setAction(action);
		setOwner(owner);
		m_sharedFolderUID = shareFolderUID;
		setNewHash(newHash);
		setOldHash(oldHash);
		setStatus(status);
		m_isFile = isFile;
		this.length= length;


	}
	
	
	public Event(Event other) 
	{

		setDate(other.getDate());
		setFilepath(other.getFilepath());
		setAction(other.getAction());
		setOwner(other.getOwner());
		m_sharedFolderUID = other.getShareFolderUID();
		setNewHash(other.getNewHash());
		setOldHash(other.getOldHash());
		setStatus(other.getStatus());
		m_isFile = other.isFile();
		this.length= other.getLenght();


	}



	public void checkConflict()
	{
		if(getAction()!=ACTION_CREATE)
		{

			Event e = DataBaseManager.getInstance().getLastEventOfAFile(m_relFilePath,m_sharedFolderUID);

			//TODO : vérifier le bien fondée de la propagation des conflits ( || e.getStatus()==STATUS_CONFLICT )
			if(e!=null && e.getStatus()!=STATUS_LOCAL_FORCEOK)
			{
				if( (e.getNewHash().compareTo(getOldHash())!=0 || e.getStatus()==STATUS_LOCAL_CONFLICT))
				{
					setStatus(STATUS_LOCAL_CONFLICT);
				}
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

	
	public static boolean isPhysicallyPresent(int status)
	{
		return status>=MIN_STATUS_LOCAL && status<=MAX_STATUS_LOCAL;
		
	}

	//On sait jamais, ça peut peut être utile^^
	public String getAbsFilePath()
	{

		return SharedFolder.AbsoluteFromRelativePath(m_relFilePath,DataBaseManager.getInstance().getSharedFolderRootPath(m_sharedFolderUID) );


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

		eventElement.appendChild(resp.createElement(DATE_TAG, ""+getDate()));
		eventElement.appendChild(resp.createElement(PATH_TAG, ""+getFilepath()));
		eventElement.appendChild(resp.createElement(ACTION_TAG, ""+getAction()));
		eventElement.appendChild(resp.createElement(PARAMETERS_TAG, ""+getParameters()));
		eventElement.appendChild(resp.createElement(OWNER_TAG, ""+getOwner()));
		eventElement.appendChild(resp.createElement(NEWHASH_TAG, ""+getNewHash()));
		eventElement.appendChild(resp.createElement(OLDHASH_TAG, ""+getOldHash()));
		eventElement.appendChild(resp.createElement(ISFILE_TAG, ""+isFile()));
		eventElement.appendChild(resp.createElement(FILESIZE_TAG, ""+getLenght()));

	}



	public long getLenght() {
		return length;
	}



	public void setLenght(long lenght) {
		this.length = lenght;
	}





}
