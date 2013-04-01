package com.peersync.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import com.peersync.models.ShareFolder;
import com.peersync.models.StackVersion;

public class DataBaseManager extends DbliteConnection{

	private static DataBaseManager instance;


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

	public static DataBaseManager getDataBaseManager() 
	{
		if(instance==null)
			instance = new DataBaseManager();
		return instance;

	}

	private DataBaseManager()
	{


	}

	public void saveEvent(Event e)
	{
		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate("Insert into events ("+DATEFIELD+","+FILEPATHFIELD+","+HASHFIELD+","+ACTIONFIELD+","+PARAMETERSFIELD+","+OWNERFIELD+","+SHAREDFOLDERFIELD+","+ISFILEFIELD+") VALUES('"+e.getDate()+"','"+e.getRelPath()+"','"+e.getHash()+"',"+e.getAction()+",'"+e.getParameters()+"','"+e.getOwner()+"','"+e.getShareFolder().getUID()+"',"+e.isFile()+")");

		}
		catch(SQLException | ClassNotFoundException ex)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(ex.getMessage());
		}

	}

	/**
	 *  Retourne les derniers événements en base de données, pour chaque fichier.
	 *  Ne prends pas en compte les événements "Suppression"
	 *  @param UID : UID du dossier que l'on veut "Mappifier"
	 * 	@return  Map nom_de_fichier,hash
	 */
	public Map<String,String> getLastEvents(String UID)
	{
		Map<String,String> res = new Hashtable<String,String>();



		try
		{
			openConnection(DBEVENTSPATH);
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.



			ResultSet rs = statement.executeQuery("select e1."+FILEPATHFIELD+",e1."+HASHFIELD+", sf."+ROOTPATHFIELD+" "+
					"from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> 3 and e1."+SHAREDFOLDERFIELD+"='"+UID+"' and  e1."+DATEFIELD+" = " +
					"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+")");


			while(rs.next())
			{

				// read the result set
				if(rs.getString(ROOTPATHFIELD)!=null)
				{
					String absolutePath = ShareFolder.AbsoluteFromRelativePath(rs.getString(FILEPATHFIELD), rs.getString(ROOTPATHFIELD)) ;
					res.put(absolutePath, rs.getString(HASHFIELD));

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
	
	public ArrayList<StackVersion> getStackVersionList(String UID){
		ArrayList<StackVersion> res = new ArrayList<StackVersion>();
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
				res.add(sv);

			}
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


}
