package com.peersync.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import com.peersync.models.Event;
import com.peersync.models.EventsStack;
import com.peersync.models.SharedFolder;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;

public class DataBaseManager extends DbliteConnection{

	private static DataBaseManager instance;


	private static final String DBEVENTSPATH = "./dblite.db";
	private static final String DBEVENTSTABLE = "events";
	private static final String DATEFIELD = "date";
	private static final String FILEPATHFIELD = "filepath";
	private static final String NEWHASHFIELD = "newhash";
	private static final String OLDHASHFIELD = "oldhash";
	private static final String STATUSFIELD = "status";
	private static final String ACTIONFIELD = "action";
	private static final String PARAMETERSFIELD = "parameters";
	private static final String OWNERFIELD = "owner";
	private static final String SHAREDFOLDERFIELD = "sharedFolder";
	private static final String ISFILEFIELD = "isFile";
	private static final String UUIDFIELD = "uuid";
	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";

	public static DataBaseManager getDataBaseManager() 
	{
		if(instance==null)
			instance = new DataBaseManager();
		return instance;

	}

	private DataBaseManager()
	{


	}
	
	/** Vérifie que les events sont bien taggés avec le bon SharedFolder.
	 * 	Dans le cas contraire, update la base de données pour revenir à une situation correcte.
	 * 	Cas d'appel de cette méthode : Ajout d'un sous SharedFolder au sein d'un SharedFolder existant.
	 */
	public void checkEventsIntegrity()
	{
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
			
					
			

			ResultSet rs = statement.executeQuery("select e1."+FILEPATHFIELD+", sf."+ROOTPATHFIELD+",e1."+SHAREDFOLDERFIELD
					+" from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")");
			while(rs.next())
			{
				String oldRelFilepath = rs.getString(FILEPATHFIELD);
				String rootDir = rs.getString(ROOTPATHFIELD);
				String oldFolderUID = rs.getString(SHAREDFOLDERFIELD);
				String absPath = SharedFolder.AbsoluteFromRelativePath(oldRelFilepath, rootDir);
				String newFolderUID = getSharedFolderOfAFile(absPath);
				String newRelFilepath = SharedFolder.RelativeFromAbsolutePath(absPath, getSharedFolderRootPath(newFolderUID));
				
				
				if(!newFolderUID.equals(oldFolderUID))
				{
					Statement statementUpdate = getConnection().createStatement();
					statementUpdate.setQueryTimeout(30);  // set timeout to 30 sec.
					statementUpdate.executeUpdate("Update "+DBEVENTSTABLE+" set "+FILEPATHFIELD+"='"+newRelFilepath+"',"+SHAREDFOLDERFIELD+"='"+newFolderUID+"' where "+FILEPATHFIELD+"='"+oldRelFilepath+"' and "+SHAREDFOLDERFIELD+"='"+oldFolderUID+"'");
					
				}
					//
					
				

				
			
			}

		}
		catch(SQLException | ClassNotFoundException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
		
			System.err.println(ex.getMessage());
		}
		
	}
	
	public Event getLastEventOfAFile(String absFilePath)
	{
		String uid = getSharedFolderOfAFile(absFilePath);
		return getLastEventOfAFile(SharedFolder.RelativeFromAbsolutePath(absFilePath, getSharedFolderRootPath(uid) ),uid);
	}

	public Event getLastEventOfAFile(String relFilePath,String dirUID)
	{
		Event res = null;
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
		
			ResultSet rs = statement.executeQuery("select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where "+FILEPATHFIELD+"='"+relFilePath+"' and "+SHAREDFOLDERFIELD+"='"+dirUID+"'"
					+" and "+DATEFIELD+" = (select max("+DATEFIELD+") from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"='"+relFilePath+"' and "+SHAREDFOLDERFIELD+"='"+dirUID+"')"
					);
			while(rs.next())
			{
				String filepath = rs.getString(FILEPATHFIELD);
				long date = rs.getLong(DATEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String owner = rs.getString(OWNERFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				int st = rs.getInt(STATUSFIELD);

				
				
				res = new Event(date,filepath,isFile,newHash,oldHash,action,owner,st);
			}

		}
		catch(SQLException | ClassNotFoundException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
		
			System.err.println(ex.getMessage());
		}
		return res;
		
	}
	
	
	public void saveEvent(Event e)
	{
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate("Insert into "+DBEVENTSTABLE+" ("+DATEFIELD+","+FILEPATHFIELD+","+NEWHASHFIELD+","+OLDHASHFIELD+","+ACTIONFIELD+","+PARAMETERSFIELD+","+OWNERFIELD+","+SHAREDFOLDERFIELD+","+ISFILEFIELD+","+STATUSFIELD+") VALUES('"+e.getDate()+"','"+e.getRelPath()+"','"+e.getNewHash()+"','"+e.getOldHash()+"',"+e.getAction()+",'"+e.getParameters()+"','"+e.getOwner()+"','"+e.getShareFolder().getUID()+"',"+e.isFile()+","+e.getStatus()+")");

		}
		catch(SQLException | ClassNotFoundException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getFilepath());
			System.err.println(ex.getMessage());
		}

	}
	
	
	public EventsStack loadEventsStack(String UIDFolder)
	{
		return loadEventsStack(UIDFolder,-1,"*");
	}
	
	public EventsStack loadEventsStack(String UIDFolder,long date)
	{
		return loadEventsStack(UIDFolder,date,"*");
	}
	
	public EventsStack loadEventsStack(String UIDFolder,long date,String owner)
	{
		EventsStack res = new EventsStack();
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
			String sqlQuery = "select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where 1 ";
			if(!UIDFolder.equals("*"))
				sqlQuery += " and "+SHAREDFOLDERFIELD+"='"+UIDFolder+"'";
			if(!UIDFolder.equals("*"))
				sqlQuery += " and "+OWNERFIELD+"='"+owner+"'";
			if(date!=-1)
					sqlQuery += " and "+DATEFIELD+" > "+date;
		
			ResultSet rs = statement.executeQuery(sqlQuery);
			while(rs.next())
			{
				String filepath = rs.getString(FILEPATHFIELD);
				long dateEvent = rs.getLong(DATEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String ownerEvent = rs.getString(OWNERFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				int st = rs.getInt(STATUSFIELD);

				
				
				res.addEvent(new Event(dateEvent,filepath,isFile,newHash,oldHash,action,ownerEvent,st));
			}

		}
		catch(SQLException | ClassNotFoundException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
		
			System.err.println(ex.getMessage());
		}
		return res;
	}
	
	/**
	 *  Retourne les derniers événements en base de données, pour chaque fichier.
	 *  Ne prends pas en compte les événements "Suppression"
	 * 	@return  Map nom_de_fichier,hash
	 */
	public Map<String,String> getLastEvents()
	{
		Map<String,String> res = new Hashtable<String,String>();



		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.



			ResultSet rs = statement.executeQuery("select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", sf."+ROOTPATHFIELD+" "+
					"from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> 3 and  e1."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")");


			while(rs.next())
			{

				// read the result set
				if(rs.getString(ROOTPATHFIELD)!=null)
				{
					String absolutePath = SharedFolder.AbsoluteFromRelativePath(rs.getString(FILEPATHFIELD), rs.getString(ROOTPATHFIELD)) ;
					res.put(absolutePath, rs.getString(NEWHASHFIELD));

				}
				else
					System.err.println("PB de shared folder");
			}
		}
		catch(SQLException | ClassNotFoundException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}


		return res;
	}

	public String getSharedFolderRootPath(String UID)
	{
		String res = new String();
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select sf."+ROOTPATHFIELD+
					" from "+DBSHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" ='"+UID+"'");

			while(rs.next())
			{
				// read the result set
				res = rs.getString(ROOTPATHFIELD);
				//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
				//		        System.out.println("id = " + rs.getString(HASHFIELD));
			}
		}
		catch(SQLException | ClassNotFoundException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;

	}

	public ArrayList<String> getAllSharedDirectories()
	{
		ArrayList<String> res = new ArrayList<String>();
		try {
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();

			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select sf."+UUIDFIELD+
					" from "+DBSHAREDFOLDERSTABLE+" sf");


			while(rs.next())
			{
				res.add(rs.getString(UUIDFIELD));

			}
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	// get Active group () ret (id, mdp???)
	// get last event since (ShareFolderVersion) ret (List EventsStack)
	// add event (List EventsStack, UID folder) ret ();
	// get file to download (UID folder) ret (list Hash);
	
	
	public SharedFolderVersion getSharedFolderVersion(String UID){
		SharedFolderVersion res = new SharedFolderVersion(UID);
		Statement statement;

		try {
			openConnection(DBEVENTSPATH);
			statement = getConnection().createStatement();


			statement.setQueryTimeout(30);  // set timeout to 30 sec.


			ResultSet rs = statement.executeQuery("select max("+DATEFIELD+") as version,"+OWNERFIELD+" from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")"+

				" where sf."+ROOTPATHFIELD+"||e1."+FILEPATHFIELD+" like (select "+ROOTPATHFIELD+"||'%' from "+DBSHAREDFOLDERSTABLE+" where "+UUIDFIELD+"='"+UID+"') group by "+OWNERFIELD);


			while(rs.next())
			{
				StackVersion sv = new StackVersion(rs.getString(OWNERFIELD), rs.getLong("version"));
				System.err.println("ID Peer : "+rs.getString(OWNERFIELD)+" version : "+rs.getLong("version"));
				res.addStackVersion(sv);

			}
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	public String getSharedFolderOfAFile(String absFilePath)
	{
		String res =  new String();
		try {
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();


			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select sf."+UUIDFIELD+" from "+SHAREDFOLDERFIELD+" sf where "+ROOTPATHFIELD+" = (select max("+ROOTPATHFIELD+")  from "+SHAREDFOLDERFIELD+" where '"+absFilePath+"' like rootAbsolutePath||'%')");

			
			while(rs.next())
			{
				res = rs.getString(UUIDFIELD);
				

			}
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


}
