package com.peersync.events;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
public class Event extends DbliteConnection{

	private long m_date;
	private String m_filepath;
	private int m_action;
	private String m_parameters;
	private File m_file;
	private String m_owner;
	private String m_hash;
	private String m_sharedFolderUUID;

	private static final String DBEVENTSPATH = "./dblite.db";
	private static final String DBEVENTSTABLE = "events";
	private static final String DATEFIELD = "date";
	private static final String FILEPATHFIELD = "filepath";
	private static final String HASHFIELD = "hash";
	private static final String ACTIONFIELD = "action";
	private static final String PARAMETERSFIELD = "parameters";
	private static final String OWNERFIELD = "owner";
	private static final String SHAREDFOLDERFIELD = "sharedFolder";
	private static final String ISFILEFIELD = "isFile";
	private static final String UUIDFIELD = "uuid";
	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";

	public Event(long d,String filepath,int action,String owner,String uuidShareFolder) throws Exception {
		super(DBEVENTSPATH);
		setDate(d);
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		setShareFolder(uuidShareFolder);
		openFile();

	}
	
	public Event(String filepath,String hash,int action,String owner,String uuidShareFolder) throws Exception {
		super(DBEVENTSPATH);
		setDate(Calendar.getInstance().getTime().getTime());
		setFilepath(filepath);
		setAction(action);
		setOwner(owner);
		setShareFolder(uuidShareFolder);
		setHash(hash);
		m_file = new File(getFilepath());
		

	}

	public static String AbsoluteFromRelativePath(String relative,String baseDir)
	{
		return baseDir.concat(relative);
	}
	
	public static String RelativeFromAbsolutePath(String absolute,String baseDir)
	{
		return absolute.replace(baseDir, "");
	}
	
	public String getSharedFolderRootPath(String uuid)
	{
		String res = new String();
		try
		{
				Statement statement = getConnection().createStatement();
				statement.setQueryTimeout(30);  // set timeout to 30 sec.
				
				ResultSet rs = statement.executeQuery("select sf."+ROOTPATHFIELD+
						" from "+DBSHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" ='"+uuid+"'");

				while(rs.next())
				{
					// read the result set
					res = rs.getString(ROOTPATHFIELD);
					//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
					//		        System.out.println("id = " + rs.getString(HASHFIELD));
				}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;
		
	}
	
	public void save()
	{
		try
		{
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			//if(e.getDate().after(d))
			int isFile = m_file.isFile()? 1 : 0;
			String relFilePath = RelativeFromAbsolutePath(m_filepath,getSharedFolderRootPath(m_sharedFolderUUID));
			statement.executeUpdate("Insert into events ("+DATEFIELD+","+FILEPATHFIELD+","+HASHFIELD+","+ACTIONFIELD+","+PARAMETERSFIELD+","+OWNERFIELD+","+SHAREDFOLDERFIELD+","+ISFILEFIELD+") VALUES('"+m_date+"','"+relFilePath+"','"+m_hash+"',"+m_action+",'"+m_parameters+"','"+m_owner+"','"+m_sharedFolderUUID+"',"+isFile+")");

		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}

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

	public String getShareFolder() {
		return m_sharedFolderUUID;
	}

	public void setShareFolder(String m_shareFolder) {
		this.m_sharedFolderUUID = m_shareFolder;
	}



}
