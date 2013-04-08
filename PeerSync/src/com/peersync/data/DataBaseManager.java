package com.peersync.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import com.peersync.models.Event;
import com.peersync.models.EventsStack;
import com.peersync.models.FileToSync;
import com.peersync.models.SharedFolder;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.tools.Constants;

public class DataBaseManager extends DbliteConnection{

	private static DataBaseManager instance;


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
	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";
	private static final String PEERGROUPFIELD = "peerGroup";


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
				PARAMETERSFIELD + " text, "+
				OWNERFIELD + " text, "+
				ISFILEFIELD +  " numeric, "+
				SHAREDFOLDERFIELD +  " numeric, "+
				STATUSFIELD + " numeric, "+
				"PRIMARY KEY ("+DATEFIELD+","+FILEPATHFIELD+","+SHAREDFOLDERFIELD+","+NEWHASHFIELD+"))");

		update("create table "+DBSHAREDFOLDERSTABLE+" "+
				"("+UUIDFIELD+" text, "+
				PEERGROUPFIELD+ " text, "+
				ROOTPATHFIELD+ " text, "+
				"PRIMARY KEY("+UUIDFIELD+"));");
	}


	@Override
	protected void onUpdate() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDelete() throws SQLException {
		// TODO Auto-generated method stub

	}


	/** Vérifie que les events sont bien taggés avec le bon SharedFolder.
	 * 	Dans le cas contraire, update la base de données pour revenir à une situation correcte.
	 * 	Cas d'appel de cette méthode : Ajout d'un sous SharedFolder au sein d'un SharedFolder existant.
	 */
	public void checkEventsIntegrity()
	{
		try
		{



			ResultSet rs = query("select e1."+FILEPATHFIELD+", sf."+ROOTPATHFIELD+",e1."+SHAREDFOLDERFIELD
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
					if(newRelFilepath != null && newFolderUID!=null)
						update("Update "+DBEVENTSTABLE+" set "+FILEPATHFIELD+"='"+newRelFilepath+"',"+SHAREDFOLDERFIELD+"='"+newFolderUID+"' where "+FILEPATHFIELD+"='"+oldRelFilepath+"' and "+SHAREDFOLDERFIELD+"='"+oldFolderUID+"'");
					else // joue de la role du on delete cascade sur les sharedFolders
						update("delete from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"='"+oldRelFilepath+"' and "+SHAREDFOLDERFIELD+"='"+oldFolderUID+"'");
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
	 * @param absFilePath : Chemin absolu du fichier dont on veut récupérer le dernier événement
	 */
	public Event getLastEventOfAFile(String absFilePath)
	{
		String uid = getSharedFolderOfAFile(absFilePath);
		return getLastEventOfAFile(SharedFolder.RelativeFromAbsolutePath(absFilePath, getSharedFolderRootPath(uid) ),uid);
	}

	/** Récupère le dernier événement pour un fichier donné
	 * 	@param relFilePath : Chemin relatif du fichier dont on veut récupérer le dernier événement
	 * 	@param sharedFolderUID : UID du Shared Folder où se trouve le fichier en question
	 * 	@return le dernier event concernant le fichier
	 */
	public Event getLastEventOfAFile(String relFilePath,String sharedFolderUID)
	{
		Event res = null;
		try
		{
			ResultSet rs = query("select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where "+FILEPATHFIELD+"='"+relFilePath+"' and "+SHAREDFOLDERFIELD+"='"+sharedFolderUID+"'"
					+" and "+DATEFIELD+" = (select max("+DATEFIELD+") from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+"='"+relFilePath+"' and "+SHAREDFOLDERFIELD+"='"+sharedFolderUID+"')"
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



				res = new Event(sharedFolderUID , date, filepath,isFile,newHash,oldHash,action,owner,st);
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

	/** Récupère le dernier événement pour un fichier donné
	 * 	@param e : Event à sauvegarder
	 */
	public void saveEvent(Event e)
	{
		try
		{



			update("Insert into "+DBEVENTSTABLE+" ("+DATEFIELD+","+FILEPATHFIELD+","+NEWHASHFIELD+","+OLDHASHFIELD+","+ACTIONFIELD+","+PARAMETERSFIELD+","+OWNERFIELD+","+SHAREDFOLDERFIELD+","+ISFILEFIELD+","+STATUSFIELD+") VALUES('"+e.getDate()+"','"+e.getFilepath()+"','"+e.getNewHash()+"','"+e.getOldHash()+"',"+e.getAction()+",'"+e.getParameters()+"','"+e.getOwner()+"','"+e.getShareFolderUID()+"',"+e.isFile()+","+e.getStatus()+")");


		}
		catch(SQLException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getFilepath());
			System.err.println(ex.getMessage());
		}

	}

	/** Obtient la liste de tous les événements dont on dispose
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack()
	{
		return loadEventsStack("*",-1,"*");
	}

	/** Obtient la liste des événements d'un Shared Folder donné
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@param UIDFolder : UID du Shared Folder dont on veut récupérer les événements
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String UIDFolder)
	{
		return loadEventsStack(UIDFolder,-1,"*");
	}


	/** Obtient la liste des événements d'un Shared Folder donné depuis une date donnée
	 * 	Appel {@link #loadEventsStack(String,long, String)}
	 * 	@param UIDFolder : UID du Shared Folder dont on veut récupérer les événements
	 * 	@param date : date (en ms depuis l'epoch) à partir de laquelle on veut récupérer les événements
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String UIDFolder,long date)
	{
		return loadEventsStack(UIDFolder,date,"*");
	}

	/** Obtient la liste des événements d'un Shared Folder donné depuis une date donnée et pour un propriétaire donné
	 * 	@param sharedFolderUID : UID du Shared Folder dont on veut récupérer les événements. Joker possible : "*"
	 * 	@param date : date (en ms depuis l'epoch) à partir de laquelle on veut récupérer les événements. Joker possible : -1
	 *  @param owner : pour ne récupérer que les events dont l'"actionneur" est "owner". Joker possible : "*"
	 * 	@return EventsStack
	 */
	public EventsStack loadEventsStack(String sharedFolderUID,long date,String owner)
	{
		EventsStack res = new EventsStack();
		try
		{
			String sqlQuery = "select e."+DATEFIELD+",e."+FILEPATHFIELD+",e."+NEWHASHFIELD+",e."+OLDHASHFIELD+",e."+ACTIONFIELD+",e."+PARAMETERSFIELD+",e."+OWNERFIELD+",e."+SHAREDFOLDERFIELD+",e."+ISFILEFIELD+",e."+STATUSFIELD+" from "+DBEVENTSTABLE+" e"
					+" where 1 ";
			if(!sharedFolderUID.equals("*"))
				sqlQuery += " and "+SHAREDFOLDERFIELD+"='"+sharedFolderUID+"'";
			if(!sharedFolderUID.equals("*"))
				sqlQuery += " and "+OWNERFIELD+"='"+owner+"'";
			if(date!=-1)
				sqlQuery += " and "+DATEFIELD+" > "+date;

			ResultSet rs = query(sqlQuery);
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



				res.addEvent(new Event(sharedFolderUID , dateEvent, filepath,isFile,newHash,oldHash,action,ownerEvent,st));
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
	 *  Retourne les derniers événements en base de données, pour chaque fichier.
	 *  Ne prends pas en compte les événements "Suppression"
	 * 	@param shareFolderUID 
	 * 	@return  Map nom_de_fichier,hash
	 */
	public Map<String,String> getLastEvents(String shareFolderUID)
	{
		Map<String,String> res = new Hashtable<String,String>();



		try
		{



			ResultSet rs = query("select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", sf."+ROOTPATHFIELD+" "+
					"from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" AND "+SHAREDFOLDERFIELD+"='"+shareFolderUID+"' and  e1."+DATEFIELD+" = " +
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
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}


		return res;
	}

	/** Obtient le chemin (absolu) d'un dossier de partage 
	 * 	@param UID : UID du Shared Folder dont on veut récupérer le chemin absolu
	 * 	@return chemin absolu du dossier de partage 
	 */
	public String getSharedFolderRootPath(String UID)
	{
		String res = new String();
		try
		{
			ResultSet rs = query("select sf."+ROOTPATHFIELD+
					" from "+DBSHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" ='"+UID+"'");

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
	
	
	public void saveSharedFolder(SharedFolder sf){
		
		try {
			update("insert into "+DBSHAREDFOLDERSTABLE+ "("+UUIDFIELD+", "+PEERGROUPFIELD+", "+ROOTPATHFIELD+") values ('"+sf.getUID() + "', '"+sf.getPeerGroupUID()+"', '"+sf.getAbsFolderRootPath()+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	/** Obtient l'ensemble des dossiers de partage 
	 */
	public ArrayList<SharedFolder> getAllSharedDirectories()
	{
		ArrayList<SharedFolder> res = new ArrayList<SharedFolder>();
		try {



			ResultSet rs = query("select sf."+UUIDFIELD+",sf."+ROOTPATHFIELD+

					" from "+DBSHAREDFOLDERSTABLE+" sf");


			while(rs.next())
			{
				res.add(new SharedFolder(rs.getString(UUIDFIELD),rs.getString(ROOTPATHFIELD)));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	// get Active group () ret (id, mdp???)
	// get last event since (ShareFolderVersion) ret (List EventsStack)
	// add event (List EventsStack, UID folder) ret ();
	// get file to download (UID folder) ret (list Hash);

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


	/** Récupère la version de pile pour chaque owner pour un dossier donné
	 * 	@param UID: UID du SharedFolder que l'on veut analyser
	 * 	@return SharedFolderVersion
	 */
	public SharedFolderVersion getSharedFolderVersion(String UID){
		SharedFolderVersion res = new SharedFolderVersion(UID);
		Statement statement;

		try {
			ResultSet rs = query("select max("+DATEFIELD+") as version,"+OWNERFIELD+" from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")"+

				" where sf."+ROOTPATHFIELD+"||e1."+FILEPATHFIELD+" like (select "+ROOTPATHFIELD+"||'%' from "+DBSHAREDFOLDERSTABLE+" where "+UUIDFIELD+"='"+UID+"') group by "+OWNERFIELD);


			while(rs.next())
			{
				StackVersion sv = new StackVersion(rs.getString(OWNERFIELD), rs.getLong("version"));
				System.err.println("ID Peer : "+rs.getString(OWNERFIELD)+" version : "+rs.getLong("version"));
				res.addStackVersion(sv);

			}
		} catch (SQLException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	public String getSharedFolderOfAFile(String absFilePath)
	{
		String res =  new String();
		try {
			ResultSet rs = query("select sf."+UUIDFIELD+" from "+SHAREDFOLDERFIELD+" sf where "+ROOTPATHFIELD+" = (select max("+ROOTPATHFIELD+")  from "+SHAREDFOLDERFIELD+" where '"+absFilePath+"' like rootAbsolutePath||'%')");

			while(rs.next())
			{
				res = rs.getString(UUIDFIELD);


			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public ArrayList<FileToSync> getFilesToDownload()
	{
		ArrayList<FileToSync> res = new ArrayList<FileToSync>();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+SHAREDFOLDERFIELD+" "+
					"from "+DBEVENTSTABLE+" e1 where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+" and  e1."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")" +
					" and e1."+NEWHASHFIELD+" NOT IN (select e2."+NEWHASHFIELD+
					" from "+DBEVENTSTABLE+" e2  where e2."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e2."+STATUSFIELD+" = "+Event.STATUS_OK+" and e2."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e2."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e2."+SHAREDFOLDERFIELD+"))";

			ResultSet rs = query(sqlQuery);

			while(rs.next())
			{
				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);
				res.add(new FileToSync(relFilePath, fileHash, sharedFolderUID));


			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	public ArrayList<FileToSync> getFilesWithLocalSource()
	{
		ArrayList<FileToSync> res = new ArrayList<FileToSync>();
		try {
			String sqlQuery = "select e1."+FILEPATHFIELD+",e1."+NEWHASHFIELD+", e1."+SHAREDFOLDERFIELD+",(select sf."+ROOTPATHFIELD+"||e2."+FILEPATHFIELD+
					" from "+DBEVENTSTABLE+" e2 left join "+DBSHAREDFOLDERSTABLE+" sf on (e2."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+") where e2."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e2."+STATUSFIELD+" = "+Event.STATUS_OK+" " +
					" and e2."+NEWHASHFIELD+" = e1."+NEWHASHFIELD+
					" and e2."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e2."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e2."+SHAREDFOLDERFIELD+")) as localSource "+
					" from "+DBEVENTSTABLE+" e1 where e1."+ACTIONFIELD+" <> "+Event.ACTION_DELETE+" and e1."+STATUSFIELD+" = "+Event.STATUS_UNSYNC+" and  e1."+DATEFIELD+" = " +
					" (select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+" and "+SHAREDFOLDERFIELD+"=e1."+SHAREDFOLDERFIELD+")" +
					" and localSource NOT NULL";

			ResultSet rs = query(sqlQuery);

			while(rs.next())
			{

				String relFilePath = rs.getString(FILEPATHFIELD);
				String fileHash = rs.getString(NEWHASHFIELD);
				String sharedFolderUID = rs.getString(SHAREDFOLDERFIELD);
				String localSource = rs.getString("localsource");
				res.add(new FileToSync(relFilePath, fileHash, sharedFolderUID,localSource));

			}
		} catch (SQLException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}







}
