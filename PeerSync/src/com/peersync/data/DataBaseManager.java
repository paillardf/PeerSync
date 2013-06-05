package com.peersync.data;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;

import com.peersync.models.ClassicFile;
import com.peersync.models.Event;
import com.peersync.models.EventsStack;
import com.peersync.models.FileInfo;
import com.peersync.models.FileWithLocalSource;
import com.peersync.models.SharedFileAvailability;
import com.peersync.models.SharedFolder;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.tools.Constants;

public class DataBaseManager extends DbliteConnection{

	private static DataBaseManager instance;

	public static Lock exclusiveAccess = new ReentrantLock();


	private static String DBEVENTSPATH = "/dblite.db";
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
	private static final String SHAREDFOLDERSTABLE = "sharedFolder";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";
	private static final String PEERGROUPFIELD = "peerGroup";
	private static final String EVENT_SIZEFIELD = "filesize";
	private static final String SHAREDFOLDER_NAMEFIELD = "name";

	private static final String FILESINFO_TABLE = "FilesInfo";
	private static final String FILESINFO_ABSOLUTEPATHFIELD = "absolutePath";
	private static final String FILESINFO_UPDATEDATEFIELD = "dateModif";


	private static final String PEERGROUP_TABLE = "PeerGroup";
	private static final String PEERGROUP_ID = "PeerGroupID";
	private static final String PEERGROUP_NAME = "name";
	private static final String PEERGROUP_DESCRIPTION = "description";






	private static final String DOWNLOADINGILESAVAILABILITY_TABLE = "FilesAvailability";
	private static final String DOWNLOADINGFILESAVAILABILITY_HASH = "hash";
	private static final String DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY = "FileAvailability";



	private static final int VERSION = 1;

	public static DataBaseManager getInstance() 
	{
		if(instance==null){
			instance = new DataBaseManager();
		}

		return instance;

	}

	private DataBaseManager()
	{
		super("./"+Constants.getInstance().PREFERENCES_PATH() + DBEVENTSPATH, VERSION);

	}

	@Override
	protected void onCreate() throws SQLException {



		update("create table "+DBEVENTSTABLE+" "+
				"("+DATEFIELD+" numeric, " +
				FILEPATHFIELD+" text, " +
				NEWHASHFIELD + " text, "+
				OLDHASHFIELD + " text, "+
				ACTIONFIELD + " numeric, "+
				EVENT_SIZEFIELD + " numeric, "+
				PARAMETERSFIELD + " text, "+
				OWNERFIELD + " text, "+
				ISFILEFIELD +  " numeric, "+
				SHAREDFOLDERFIELD +  " numeric, "+
				STATUSFIELD + " numeric, "+
				"PRIMARY KEY ("+DATEFIELD+","+FILEPATHFIELD+","+SHAREDFOLDERFIELD+","+NEWHASHFIELD+"))");

		update("create table "+SHAREDFOLDERSTABLE+" "+
				"("+UUIDFIELD+" text, "+
				PEERGROUPFIELD+ " text, "+
				ROOTPATHFIELD+ " text, "+
				SHAREDFOLDER_NAMEFIELD+ " text UNIQUE, "+
				"PRIMARY KEY("+UUIDFIELD+"));");

		update("create table "+FILESINFO_TABLE+" "+
				"("+FILESINFO_ABSOLUTEPATHFIELD+" text, "+
				FILESINFO_UPDATEDATEFIELD+ " long, "+
				"PRIMARY KEY("+FILESINFO_ABSOLUTEPATHFIELD+"));");

		update("create table "+DOWNLOADINGILESAVAILABILITY_TABLE+" "+
				"("+DOWNLOADINGFILESAVAILABILITY_HASH+" text, "+
				DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY+ " text, "+
				"PRIMARY KEY("+DOWNLOADINGFILESAVAILABILITY_HASH+"));");

		update("create table "+PEERGROUP_TABLE+" "+
				"("+PEERGROUP_ID+" text, "+
				PEERGROUP_NAME+ " text, "+
				PEERGROUP_DESCRIPTION+ " text, "+
				"PRIMARY KEY("+PEERGROUP_ID+"));");


	}


	@Override
	protected void onUpdate() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDelete() throws SQLException {
		// TODO Auto-generated method stub

	}


	/** V�rifie que les events sont bien tagg�s avec le bon SharedFolder.
	 * 	Dans le cas contraire, update la base de donn�es pour revenir � une situation correcte.
	 * 	Cas d\"appel de cette m�thode : Ajout d\"un sous SharedFolder au sein d\"un SharedFolder existant.
	 */
	public void checkEventsIntegrity()
	{
		try
		{



			ResultSet rs = query("select e1."+FILEPATHFIELD+", sf."+ROOTPATHFIELD+",e1."+SHAREDFOLDERFIELD
					+" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")");
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
					//					if(newRelFilepath != null && newFolderUID!=null)
					//						update("Update "+DBEVENTSTABLE+" set "+FILEPATHFIELD+"=\""+newRelFilepath+"\","+SHAREDFOLDERFIELD+"=\""+newFolderUID+"\" where "+FILEPATHFIELD+"=\""+oldRelFilepath+"\" and "+SHAREDFOLDERFIELD+"=\""+oldFolderUID+"\"");
					//					else // joue de la role du on delete cascade sur les sharedFolders
					update("delete from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"=\""+oldRelFilepath+"\" and "+SHAREDFOLDERFIELD+"=\""+oldFolderUID+"\"");
				}
				//





			}

		}
		catch(SQLException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found

			System.err.println(ex.getMessage());
		}

	}




	/** Raccourcis pour appeler {@link #getLastEventOfAFile(String, String)} avec uniquement un chemin absolu de fichier
	 * @param absFilePath : Chemin absolu du fichier dont on veut r�cup�rer le dernier �v�nement
	 */
	public Event getLastEventOfAFile(String absFilePath)
	{
		String uid = getSharedFolderOfAFile(absFilePath);
		return getLastEventOfAFile(SharedFolder.RelativeFromAbsolutePath(absFilePath, getSharedFolderRootPath(uid) ),uid);
	}



	public ArrayList<SharedFolder> getSharedFolders(String peerGroupID)
	{
		ArrayList<SharedFolder> res = new ArrayList<SharedFolder>();
		try {



			ResultSet rs = query("select sf."+UUIDFIELD+",sf."+ROOTPATHFIELD+",sf."+SHAREDFOLDER_NAMEFIELD+

					" from "+SHAREDFOLDERSTABLE+" sf where sf."+PEERGROUPFIELD+"=\""+peerGroupID+"\" AND sf."+ROOTPATHFIELD+" IS NOT NULL and sf."+ROOTPATHFIELD+" <>''");


			while(rs.next())
			{
				res.add(new SharedFolder(rs.getString(UUIDFIELD),peerGroupID,rs.getString(ROOTPATHFIELD),rs.getString(SHAREDFOLDER_NAMEFIELD)));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;

	}
	/** R�cup�re le dernier �v�nement pour un fichier donn�
	 * 	@param relFilePath : Chemin relatif du fichier dont on veut r�cup�rer le dernier �v�nement
	 * 	@param sharedFolderUID : UID du Shared Folder o� se trouve le fichier en question
	 * 	@return le dernier event concernant le fichier
	 */
	public Event getLastEventOfAFile(String relFilePath,String sharedFolderUID)
	{
		Event res = null;
		try
		{
			ResultSet rs = query("select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+EVENT_SIZEFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where "+FILEPATHFIELD+"=\""+relFilePath+"\" and "+SHAREDFOLDERFIELD+"=\""+sharedFolderUID+"\""
					+" and "+DATEFIELD+" = (select max("+DATEFIELD+") from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"=\""+relFilePath+"\" and "+SHAREDFOLDERFIELD+"=\""+sharedFolderUID+"\")"
					);
			while(rs.next())
			{
				Long filesize = rs.getLong(EVENT_SIZEFIELD);
				String filepath = rs.getString(FILEPATHFIELD);
				long date = rs.getLong(DATEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String owner = rs.getString(OWNERFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				int st = rs.getInt(STATUSFIELD);



				res = new Event(sharedFolderUID , date, filepath,filesize,isFile,newHash,oldHash,action,owner,st);
			}

		}
		catch(SQLException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found

			System.err.println(ex.getMessage());
		}
		return res;

	}

	/** R�cup�re le dernier �v�nement pour un fichier donn�
	 * 	@param e : Event � sauvegarder
	 */
	public void saveEvent(Event e)
	{
		try
		{



			update("Insert into "+DBEVENTSTABLE+" ("+DATEFIELD+","+FILEPATHFIELD+","+NEWHASHFIELD+","+OLDHASHFIELD+","+ACTIONFIELD+","+PARAMETERSFIELD+","+OWNERFIELD+","+SHAREDFOLDERFIELD+","+ISFILEFIELD+","+EVENT_SIZEFIELD+","+STATUSFIELD+") VALUES(\""+e.getDate()+"\",\""+e.getFilepath()+"\",\""+e.getNewHash()+"\",\""+e.getOldHash()+"\","+e.getAction()+",\""+e.getParameters()+"\",\""+e.getOwner()+"\",\""+e.getShareFolderUID()+"\","+e.isFile()+","+e.getLenght()+","+e.getStatus()+")");


		}
		catch(SQLException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getFilepath());
			System.err.println(ex.getMessage());
		}

	}

	/** Obtient la liste de tous les �v�nements dont on dispose
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack()
	{
		return loadEventsStack("*",-1,"*");
	}

	/** Obtient la liste des �v�nements d\"un Shared Folder donn�
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@param UIDFolder : UID du Shared Folder dont on veut r�cup�rer les �v�nements
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String UIDFolder)
	{
		return loadEventsStack(UIDFolder,-1,"*");
	}


	/** Obtient la liste des �v�nements d\"un Shared Folder donn� depuis une date donn�e
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@param UIDFolder : UID du Shared Folder dont on veut r�cup�rer les �v�nements
	 * 	@param date : date (en ms depuis l\"epoch) � partir de laquelle on veut r�cup�rer les �v�nements
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String UIDFolder,long date)
	{
		return loadEventsStack(UIDFolder,date,"*");
	}

	/** Obtient la liste des �v�nements d\"un Shared Folder donn� depuis une date donn�e et pour un propri�taire donn�
	 * 	@param sharedFolderUID : UID du Shared Folder dont on veut r�cup�rer les �v�nements. Joker possible : "*"
	 * 	@param date : date (en ms depuis l\"epoch) � partir de laquelle on veut r�cup�rer les �v�nements. Joker possible : -1
	 *  @param owner : pour ne r�cup�rer que les events dont l\""actionneur" est "owner". Joker possible : "*"
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String sharedFolderUID,long date,String owner)
	{
		EventsStack res = new EventsStack();
		try
		{
			String sqlQuery = "select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+EVENT_SIZEFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where 1 ";
			if(!sharedFolderUID.equals("*"))
				sqlQuery += " and "+SHAREDFOLDERFIELD+"=\""+sharedFolderUID+"\"";
			if(!sharedFolderUID.equals("*"))
				sqlQuery += " and "+OWNERFIELD+"=\""+owner+"\"";
			if(date!=-1)
				sqlQuery += " and "+DATEFIELD+" > "+date;

			ResultSet rs = query(sqlQuery);
			while(rs.next())
			{
				String filepath = rs.getString(FILEPATHFIELD);
				long dateEvent = rs.getLong(DATEFIELD);
				long filesize = rs.getLong(EVENT_SIZEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String ownerEvent = rs.getString(OWNERFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				int st = rs.getInt(STATUSFIELD);



				res.addEvent(new Event(sharedFolderUID , dateEvent, filepath,filesize, isFile,newHash,oldHash,action,ownerEvent,st));
			}

		}
		catch(SQLException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found

			System.err.println(ex.getMessage());
		}
		return res;
	}






	/**
	 *  Retourne les derniers �v�nements en base de donn�es, pour chaque fichier.
	 *  Ne prends pas en compte les �v�nements "Suppression", les events avec un status diff�rent de "OK", ni les dossiers
	 * 	@param shareFolderUID 
	 * 	@return  Map nom_de_fichier,hash
	 */
	private SharedFileAvailability getLocalFilesAvailability(String hash)
	{

		try
		{

			String sql = "select e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+",(case when e1."+FILEPATHFIELD+"=\"\\\" THEN sf."+ROOTPATHFIELD+" ELSE sf."+ROOTPATHFIELD+"||e1."+FILEPATHFIELD+" end) as absPath,fi."+FILESINFO_UPDATEDATEFIELD+" "+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+") left join "+FILESINFO_TABLE+" fi on (absPath=fi."+FILESINFO_ABSOLUTEPATHFIELD+") "+
					"where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+ISFILEFIELD+" =1 and e1."+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+" and e1."+NEWHASHFIELD+"=\""+hash+"\" and  e1."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")";


			ResultSet rs = query(sql);




			while(rs.next())
			{

				// read the result set
				if(rs.getString(NEWHASHFIELD)!=null)
				{

					FileAvailability fa = new FileAvailability(rs.getString(NEWHASHFIELD));
					fa.addSegment(0, rs.getLong(EVENT_SIZEFIELD));
					SharedFileAvailability res = new SharedFileAvailability(fa,rs.getString("absPath"),rs.getLong(EVENT_SIZEFIELD),rs.getLong(FILESINFO_UPDATEDATEFIELD));
					return res;

				}
				else
					System.err.println("PB de shared folder");
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}


		return null;
	}



	public HashMap<String,ArrayList<Event>> getEventsInConflict(String shareFolderUID)
	{
		HashMap<String,ArrayList<Event>> res = new HashMap<String,ArrayList<Event>>();
		try
		{
			//TODO voir si on peut pas faire mieux ...
			String sql = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+DATEFIELD+", e1."+EVENT_SIZEFIELD+", e1."+ISFILEFIELD+", e1."+OLDHASHFIELD+", e1."+ACTIONFIELD+", e1."+OWNERFIELD+", e1."+STATUSFIELD+
					" from "+DBEVENTSTABLE+" e1  where  e1."+SHAREDFOLDERFIELD+"=\""+shareFolderUID+"\" and (e1."+STATUSFIELD+" = "+Event.STATUS_LOCAL_CONFLICT+" OR e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC_CONFLICT+") and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+" and "+OWNERFIELD+"=e1."+OWNERFIELD+")"+
					" and e1."+FILEPATHFIELD+" in(select ev."+FILEPATHFIELD+" from "+DBEVENTSTABLE+" ev where (ev."+STATUSFIELD+" = "+Event.STATUS_LOCAL_CONFLICT+" OR ev."+STATUSFIELD+" = "+Event.STATUS_UNSYNC_CONFLICT+") and ev."+DATEFIELD+"=" +
					" (select max("+DATEFIELD+") from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"=ev."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=ev."+SHAREDFOLDERFIELD+"))";

			System.out.println(sql);
			ResultSet rs = query(sql);
			while(rs.next())
			{
				String relFilePath = rs.getString(FILEPATHFIELD);
				long date = rs.getLong(DATEFIELD);
				long length = rs.getLong(DATEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String owner = rs.getString(OWNERFIELD);
				int status = rs.getInt(STATUSFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				Event e = new Event(shareFolderUID,date,relFilePath,length,isFile,newHash,oldHash,action,owner,status);
				String absPath = e.getAbsFilePath();
				if(res.containsKey(absPath))
					res.get(absPath).add(e);
				else
				{
					ArrayList<Event> al = new ArrayList<Event>();
					al.add(e);
				res.put(absPath, al);
				}
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

	public HashMap<String,ArrayList<Event>> getEventsInConflict()
	{
		HashMap<String,ArrayList<Event>> res = new HashMap<String,ArrayList<Event>>();
		try
		{
			String sql = "select e1."+SHAREDFOLDERFIELD+", e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+DATEFIELD+", e1."+EVENT_SIZEFIELD+", e1."+ISFILEFIELD+", e1."+OLDHASHFIELD+", e1."+ACTIONFIELD+", e1."+OWNERFIELD+", e1."+STATUSFIELD+
					" from "+DBEVENTSTABLE+" e1  where (e1."+STATUSFIELD+" = "+Event.STATUS_LOCAL_CONFLICT+" OR e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC_CONFLICT+") and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+" and "+OWNERFIELD+"=e1."+OWNERFIELD+")"+
							" and e1."+FILEPATHFIELD+" in(select ev."+FILEPATHFIELD+" from "+DBEVENTSTABLE+" ev where (ev."+STATUSFIELD+" = "+Event.STATUS_LOCAL_CONFLICT+" OR ev."+STATUSFIELD+" = "+Event.STATUS_UNSYNC_CONFLICT+") and ev."+DATEFIELD+"=" +
							" (select max("+DATEFIELD+") from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"=ev."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=ev."+SHAREDFOLDERFIELD+"))";

			ResultSet rs = query(sql);
			while(rs.next())
			{
				String sharedFolder = rs.getString(SHAREDFOLDERFIELD);
				String relFilePath = rs.getString(FILEPATHFIELD);
				long date = rs.getLong(DATEFIELD);
				long length = rs.getLong(EVENT_SIZEFIELD);
				String newHash = rs.getString(NEWHASHFIELD);
				String oldHash = rs.getString(OLDHASHFIELD);
				int action = rs.getInt(ACTIONFIELD);
				String owner = rs.getString(OWNERFIELD);
				int status = rs.getInt(STATUSFIELD);
				int isFile = rs.getInt(ISFILEFIELD);
				Event e = new Event(sharedFolder,date,relFilePath,length,isFile,newHash,oldHash,action,owner,status);
				String absPath = e.getAbsFilePath();
				if(res.containsKey(absPath))
					res.get(absPath).add(e);
				else
				{
					ArrayList<Event> al = new ArrayList<Event>();
					al.add(e);
				res.put(absPath, al);
				}
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






	/**
	 *  Retourne les derniers �v�nements en base de donn�es, pour chaque fichier.
	 *  Ne prends pas en compte les �v�nements "Suppression", ni les events avec un status diff�rent de "OK"
	 * 	@param shareFolderUID 
	 * 	@return  Map nom_de_fichier,hash
	 */
	public Map<String,FileInfo> getLastEvents(String shareFolderUID)
	{
		Map<String,FileInfo> res = new Hashtable<String,FileInfo>();

		try
		{


			String sql = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", sf."+ROOTPATHFIELD+", (case when e1."+FILEPATHFIELD+"=\"\\\" THEN sf."+ROOTPATHFIELD+" ELSE sf."+ROOTPATHFIELD+"||e1."+FILEPATHFIELD+" end) as absPath,fi."+FILESINFO_UPDATEDATEFIELD+" "+
					"from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+") left join "+FILESINFO_TABLE+" fi on (absPath=fi."+FILESINFO_ABSOLUTEPATHFIELD+") where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" AND e1."+SHAREDFOLDERFIELD+"=\""+shareFolderUID+"\" and e1."+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+" and  e1."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+" and "+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+")";


			ResultSet rs = query(sql);




			while(rs.next())
			{

				// read the result set
				if(rs.getString(ROOTPATHFIELD)!=null)
				{
					String absolutePath = rs.getString("absPath") ;
					res.put(absolutePath, new FileInfo(absolutePath, rs.getLong(FILESINFO_UPDATEDATEFIELD),rs.getString(NEWHASHFIELD)));

				}
				else
					System.err.println("PB de shared folder");
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

	
	/** Obtient le chemin (absolu) d\"un dossier de partage 
	 * 	@param UID : UID du Shared Folder dont on veut r�cup�rer le chemin absolu
	 * 	@return chemin absolu du dossier de partage 
	 */
	public SharedFolder getSharedFolder(String UID)
	{
		SharedFolder res = null;
		try
		{
			ResultSet rs = query("select *"+
					" from "+SHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" =\""+UID+"\"");

			while(rs.next())
			{
				// read the result set
				res = new SharedFolder(rs.getString(UUIDFIELD),rs.getString(PEERGROUPFIELD),rs.getString(ROOTPATHFIELD),rs.getString(SHAREDFOLDER_NAMEFIELD));
				//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
				//		        System.out.println("id = " + rs.getString(HASHFIELD));
			}
		}
		catch(SQLException  e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;

	}
	
	/** Obtient le chemin (absolu) d\"un dossier de partage 
	 * 	@param UID : UID du Shared Folder dont on veut r�cup�rer le chemin absolu
	 * 	@return chemin absolu du dossier de partage 
	 */
	public String getSharedFolderRootPath(String UID)
	{
		String res = new String();
		try
		{
			ResultSet rs = query("select sf."+ROOTPATHFIELD+
					" from "+SHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" =\""+UID+"\"");

			while(rs.next())
			{
				// read the result set
				res = rs.getString(ROOTPATHFIELD);
				//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
				//		        System.out.println("id = " + rs.getString(HASHFIELD));
			}
		}
		catch(SQLException  e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;

	}

	public void saveFileInfo(FileInfo fi){
		boolean update = false;
		try {
			ResultSet rs = query("select fi."+FILESINFO_ABSOLUTEPATHFIELD+
					" from "+FILESINFO_TABLE+" fi where "+FILESINFO_ABSOLUTEPATHFIELD+"=\""+fi.getAbsFilePath()+"\"");

			while(rs.next())
			{
				if(rs.getString(FILESINFO_ABSOLUTEPATHFIELD)!=null)
					update = true;

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if(update)
				update("Update "+FILESINFO_TABLE+" set "+FILESINFO_UPDATEDATEFIELD+"="+fi.getUpdateDate()+" where "+FILESINFO_ABSOLUTEPATHFIELD+"=\""+fi.getAbsFilePath()+"\"");
			else
				update("insert into "+FILESINFO_TABLE+ "("+FILESINFO_ABSOLUTEPATHFIELD+", "+FILESINFO_UPDATEDATEFIELD+") values (\""+fi.getAbsFilePath() + "\", "+fi.getUpdateDate()+")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveSharedFolder(SharedFolder sf){

		boolean update = false;
		String name = sf.getName();
		boolean found=false;
		try {
			int cpt=0;
			while(!found)
			{
				cpt++;
				ResultSet rs = query("select sf."+SHAREDFOLDER_NAMEFIELD+
						" from "+SHAREDFOLDERSTABLE+" sf where sf."+SHAREDFOLDER_NAMEFIELD+"=\""+name+"\"");
				while(rs.next())
				{
					if(rs.getString(SHAREDFOLDER_NAMEFIELD)!=null)
						name+=cpt;
				}
				
			}
			
			
			ResultSet rs = query("select sf."+UUIDFIELD+
					" from "+SHAREDFOLDERSTABLE+" sf where sf."+UUIDFIELD+"=\""+sf.getUID()+"\"");

			while(rs.next())
			{
				if(rs.getString(UUIDFIELD)!=null)
					update = true;

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if(update)
				update("Update "+SHAREDFOLDERSTABLE+" set "+PEERGROUPFIELD+"=\""+sf.getPeerGroupUID()+"\", "+ROOTPATHFIELD+"=\""+sf.getAbsFolderRootPath()+"\","+SHAREDFOLDER_NAMEFIELD+"=\""+name+"\"  where "+UUIDFIELD+"=\""+sf.getUID()+"\"");
			else
				update("insert into "+SHAREDFOLDERSTABLE+ "("+UUIDFIELD+", "+PEERGROUPFIELD+", "+ROOTPATHFIELD+","+SHAREDFOLDER_NAMEFIELD+") values (\""+sf.getUID() + "\", \""+sf.getPeerGroupUID()+"\", \""+sf.getAbsFolderRootPath()+"\",\""+name+"\")");
		} catch (SQLException e) {
			e.printStackTrace();
		}



	}

	/** Obtient l\"ensemble des dossiers de partage 
	 */
	public ArrayList<SharedFolder> getAllSharedDirectories()
	{
		ArrayList<SharedFolder> res = new ArrayList<SharedFolder>();
		try {



			ResultSet rs = query("select sf."+UUIDFIELD+",sf."+ROOTPATHFIELD+",sf."+PEERGROUPFIELD+",sf."+SHAREDFOLDER_NAMEFIELD+

					" from "+SHAREDFOLDERSTABLE+" sf where sf."+ROOTPATHFIELD+" IS NOT NULL and sf."+ROOTPATHFIELD+" <>''");


			while(rs.next())
			{
				res.add(new SharedFolder(rs.getString(UUIDFIELD),rs.getString(PEERGROUPFIELD),rs.getString(ROOTPATHFIELD),rs.getString(SHAREDFOLDER_NAMEFIELD)));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}



	public EventsStack getEventsToSync(SharedFolderVersion sfv)
	{
		EventsStack res = new EventsStack();
		for(StackVersion sv : sfv.getStackVersionList())
		{
			res.addAll(loadEventsStack(sfv.getUID(),sv.getLastUpdate(),sv.getUID()));
		}
		res.setAllEventsToSync();
		return res;

	}

	public void cleanDonwloadingFileAvailabilities()
	{
		String sqlQuery = "Delete from "+DOWNLOADINGILESAVAILABILITY_TABLE+
				" where "+DOWNLOADINGFILESAVAILABILITY_HASH+" NOT IN"+
				" (select e1."+NEWHASHFIELD+
				" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+
				" and e1."+ISFILEFIELD+"=1"+
				" and  e1."+DATEFIELD+" = " +
				" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+"))";
		try {
			update(sqlQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public void eraseFileAvailability(String hash)
	{
		String sqlQuery = "Delete from "+DOWNLOADINGILESAVAILABILITY_TABLE+
				" where "+DOWNLOADINGFILESAVAILABILITY_HASH+" = \""+hash+"\"";
		try {
			update(sqlQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public void saveFileAvailability(FileAvailability fa)
	{
		boolean update = false;
		try {
			ResultSet rs = query("select "+DOWNLOADINGFILESAVAILABILITY_HASH+
					" from "+DOWNLOADINGILESAVAILABILITY_TABLE+" where "+DOWNLOADINGFILESAVAILABILITY_HASH+"=\""+fa.getHash()+"\"");

			while(rs.next())
			{
				if(rs.getString(DOWNLOADINGFILESAVAILABILITY_HASH)!=null)
					update = true;

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if(update)
				update("Update "+DOWNLOADINGILESAVAILABILITY_TABLE+" set "+DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY+"='"+fa.toXML().toString()+"' where "+DOWNLOADINGFILESAVAILABILITY_HASH+"=\""+fa.getHash()+"\"");
			else
			{
				update("insert into "+DOWNLOADINGILESAVAILABILITY_TABLE+ " ("+DOWNLOADINGFILESAVAILABILITY_HASH+", "+DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY+") values (\""+fa.getHash() + "\", '"+fa.toXML().toString()+"')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}





	private SharedFileAvailability getDownloadingFilesAvailibility(String hash)
	{
		try {
			ResultSet rs = query("select "+DOWNLOADINGFILESAVAILABILITY_HASH+", "+DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY+", "+
					" (select e1."+EVENT_SIZEFIELD+
					" from "+DBEVENTSTABLE+" e1 where e1."+NEWHASHFIELD+"=\""+hash+"\" LIMIT 1) as size"+
					" from "+DOWNLOADINGILESAVAILABILITY_TABLE+" where "+DOWNLOADINGFILESAVAILABILITY_HASH+"=\""+hash+"\"");

			while(rs.next())
			{

				String fileAvailabilitiy = rs.getString(DOWNLOADINGFILESAVAILABILITY_FILEAVAILABILITY);
				try {
					Reader r = new StringReader(fileAvailabilitiy);
					StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, r);
					XMLElement e =(XMLElement)doc.getChildren().nextElement();
					SharedFileAvailability res = new SharedFileAvailability(new FileAvailability(e),Constants.TEMP_PATH+Constants.getInstance().PEERNAME+"/"+hash+".tmp",rs.getLong("size"),-1);
					return res;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}




	/** R�cup�re la version de pile pour chaque owner pour un dossier donn�
	 * 	@param UID: UID du SharedFolder que l\"on veut analyser
	 * 	@return SharedFolderVersion
	 */
	public SharedFolderVersion getSharedFolderVersion(String UID){
		SharedFolderVersion res = new SharedFolderVersion(UID);
		Statement statement;

		try {
			ResultSet rs = query("select max("+DATEFIELD+") as version,"+OWNERFIELD+" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")"+

				" where sf."+ROOTPATHFIELD+"||e1."+FILEPATHFIELD+" like (select "+ROOTPATHFIELD+"||\"%\" from "+SHAREDFOLDERSTABLE+" where "+UUIDFIELD+"=\""+UID+"\") group by "+OWNERFIELD);


			while(rs.next())
			{
				StackVersion sv = new StackVersion(rs.getString(OWNERFIELD), rs.getLong("version"));
				res.addStackVersion(sv);

			}
		} catch (SQLException  e) {
			e.printStackTrace();
		}
		return res;
	}


	public String getSharedFolderOfAFile(String absFilePath)
	{
		String res =  new String();
		try {
			ResultSet rs = query("select sf."+UUIDFIELD+" from "+SHAREDFOLDERFIELD+" sf where "+ROOTPATHFIELD+" = (select max("+ROOTPATHFIELD+")  from "+SHAREDFOLDERFIELD+" where \""+absFilePath+"\" like rootAbsolutePath||\"%\")");

			while(rs.next())
			{
				res = rs.getString(UUIDFIELD);


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}


	public String getPeerGroupOfASharedFolder(String SharedFolder)
	{
		String res =  new String();
		try {
			ResultSet rs = query("select sf."+PEERGROUPFIELD+" from "+SHAREDFOLDERSTABLE+" sf where "+UUIDFIELD+" = \""+SharedFolder+"\"");

			while(rs.next())
			{
				res = rs.getString(PEERGROUPFIELD);


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}


	public ArrayList<ClassicFile> getFilesToRemove(String peerGroupId )
	{
		ArrayList<ClassicFile> res = new ArrayList<ClassicFile> ();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+", e1."+SHAREDFOLDERFIELD+",sf."+ROOTPATHFIELD+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" = "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+
					" and e1."+ISFILEFIELD+"=1"+
					" and sf."+PEERGROUPFIELD+"=\""+peerGroupId+"\""+
					" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")";


			ResultSet rs = query(sqlQuery);
			while(rs.next())
			{

				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);

				res.add(new ClassicFile(relFilePath, fileHash,rs.getLong(EVENT_SIZEFIELD), sharedFolderUID, rs.getString(ROOTPATHFIELD)));


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}


	public ArrayList<ClassicFile> getFoldersToRemove(String peerGroupId )
	{
		ArrayList<ClassicFile> res = new ArrayList<ClassicFile> ();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+", e1."+SHAREDFOLDERFIELD+",sf."+ROOTPATHFIELD+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" = "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+
					" and e1."+ISFILEFIELD+"=0"+
					" and sf."+PEERGROUPFIELD+"=\""+peerGroupId+"\""+
					" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")";


			ResultSet rs = query(sqlQuery);
			while(rs.next())
			{

				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);


				res.add(new ClassicFile(relFilePath, fileHash,rs.getLong(EVENT_SIZEFIELD), sharedFolderUID, rs.getString(ROOTPATHFIELD)));


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}



		return res;

	}

	public ArrayList<ClassicFile> getFilesToSyncConcernByThisHash(String hash)
	{
		ArrayList<ClassicFile> res = new ArrayList<ClassicFile> ();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+", e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+", e1."+SHAREDFOLDERFIELD+",sf."+ROOTPATHFIELD+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+
					" and e1."+ISFILEFIELD+"=1"+
					" and e1."+NEWHASHFIELD+"=\""+hash+"\""+
					" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")" ;

			ResultSet rs = query(sqlQuery);

			while(rs.next())
			{
				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);
				res.add(new ClassicFile(relFilePath, fileHash, rs.getLong(EVENT_SIZEFIELD), sharedFolderUID, rs.getString(ROOTPATHFIELD)));


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}



		return res;

	}


	public ArrayList<ClassicFile> getFilesToDownload(String sharedFolderUID)
	{
		ArrayList<ClassicFile> res = new ArrayList<ClassicFile>();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+", e1."+SHAREDFOLDERFIELD+",sf."+PEERGROUPFIELD+",sf."+ROOTPATHFIELD+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+
					" and e1."+ISFILEFIELD+"=1"+
					" and e1."+SHAREDFOLDERFIELD+"=\""+sharedFolderUID+"\""+
					" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")" +
					" and e1."+NEWHASHFIELD+" NOT IN (select e2."+NEWHASHFIELD+
					" from "+DBEVENTSTABLE+" e2  where e2."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e2."+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+" and e2."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e2."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e2."+SHAREDFOLDERFIELD+"))";


			ResultSet rs = query(sqlQuery);
			Set<String> setHash = new HashSet<String>();
			while(rs.next())
			{

				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String peerGroupId = rs.getString(PEERGROUPFIELD);

				if(setHash.add(fileHash))
					res.add(new ClassicFile(relFilePath, fileHash, rs.getLong(EVENT_SIZEFIELD), sharedFolderUID, rs.getString(ROOTPATHFIELD)));


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}


	public ArrayList<FileWithLocalSource> getFilesWithLocalSource(String peerGroupId)
	{
		ArrayList<FileWithLocalSource> res = new ArrayList<FileWithLocalSource>();
		try {

			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+", e1."+SHAREDFOLDERFIELD+",sf1."+ROOTPATHFIELD+",(select sf."+ROOTPATHFIELD+"||e2."+FILEPATHFIELD+
					" from "+DBEVENTSTABLE+" e2 left join "+SHAREDFOLDERSTABLE+" sf on (e2."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+") where e2."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e2."+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+" " +
					" and e1."+ISFILEFIELD+"=1"+
					" and e2."+NEWHASHFIELD+" = e1."+NEWHASHFIELD+
					" and sf."+PEERGROUPFIELD+"=\""+peerGroupId+"\""+
					" and e2."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e2."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e2."+SHAREDFOLDERFIELD+" and "+STATUSFIELD+" BETWEEN "+Event.MIN_STATUS_LOCAL+" AND "+Event.MAX_STATUS_LOCAL+" )) as localSource "+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf1 on (e1."+SHAREDFOLDERFIELD+"=sf1."+UUIDFIELD+") where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")" +
					" and localSource NOT NULL";



			ResultSet rs = query(sqlQuery);

			while(rs.next())
			{

				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);
				String localSource = rs.getString("localsource");
				res.add(new FileWithLocalSource(relFilePath, fileHash ,rs.getLong(EVENT_SIZEFIELD) , sharedFolderUID,rs.getString(ROOTPATHFIELD),localSource));

			}
		} catch (SQLException  e) {
			e.printStackTrace();
		}
		return res;
	}




	public void updateEventStatus(String relFilePath,String hash,String sharedFolderUID, int status)
	{

		try {
			update("Update "+DBEVENTSTABLE+" set "+STATUSFIELD+"="+status+" where "+FILEPATHFIELD+"=\""+relFilePath+"\" and "+SHAREDFOLDERFIELD+"=\""+sharedFolderUID+"\" and "+NEWHASHFIELD+"=\""+hash+"\"");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	public ArrayList<ClassicFile> getUnsyncFolder(String peerGroupId)
	{
		ArrayList<ClassicFile> res = new ArrayList<ClassicFile>();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+", e1."+NEWHASHFIELD+", e1."+EVENT_SIZEFIELD+",  e1."+SHAREDFOLDERFIELD+", sf."+ROOTPATHFIELD+
					" from "+DBEVENTSTABLE+" e1 left join "+SHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+") where e1."+ACTIONFIELD+" = "+Event.ACTION_CREATE+" and  e1."+ISFILEFIELD+" = 0  AND sf."+
					PEERGROUPFIELD+"=\""+peerGroupId+"\" and "+STATUSFIELD+"="+Event.STATUS_UNSYNC+" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")";

			ResultSet rs = query(sqlQuery);

			while(rs.next())
			{


				res.add(new ClassicFile(rs.getString(FILEPATHFIELD),rs.getString(NEWHASHFIELD) ,rs.getLong(EVENT_SIZEFIELD) ,rs.getString(SHAREDFOLDERFIELD),rs.getString(ROOTPATHFIELD)));

			}
		} catch (SQLException  e) {
			e.printStackTrace();
		}
		return res;
	}


	public String getSharedFolderPeerGroup(String UID) {

		String res = new String();
		try
		{
			ResultSet rs = query("select sf."+PEERGROUPFIELD+
					" from "+SHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" =\""+UID+"\"");

			while(rs.next())
			{
				// read the result set
				res = rs.getString(PEERGROUPFIELD);
				//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
				//		        System.out.println("id = " + rs.getString(HASHFIELD));
			}
		}
		catch(SQLException  e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;
	}

	//retourne la disponibilit� d'un fichier � partir de son hash
	public SharedFileAvailability getSharedFileAvailability(String hash) {
		SharedFileAvailability fa = getLocalFilesAvailability(hash);
		if(fa!=null)
			return fa;
		fa = getDownloadingFilesAvailibility(hash);
		if(fa!=null)
			return fa;
		return new SharedFileAvailability(new FileAvailability(hash),"",0,0);
	}





	public long getFileSize(String hash) {

		long res = 0;
		try {
			String sqlQuery = "select "+EVENT_SIZEFIELD+
					" from "+DBEVENTSTABLE+" e1 where "+NEWHASHFIELD+"=\""+hash+"\" LIMIT 1";


			ResultSet rs = query(sqlQuery);
			while(rs.next())
			{

				res = rs.getLong(EVENT_SIZEFIELD);


			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}







}
