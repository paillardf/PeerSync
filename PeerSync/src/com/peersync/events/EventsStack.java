package com.peersync.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import com.peersync.models.ShareFolder;



public class EventsStack extends DbliteConnection {

	private Vector<Event> m_Events = new Vector<Event>();

	private static final String DBEVENTSPATH = "./dblite.db";
	private static final String DBEVENTSTABLE = "events";
	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String SHAREDFOLDERFIELD = "sharedFolder";
	private static final String DATEFIELD = "date";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";
	private static final String FILEPATHFIELD = "filepath";
	private static final String HASHFIELD = "hash";
	private static final String ACTIONFIELD = "action";
	private static final String UUIDFIELD = "uuid";
	private static final String PARAMETERSFIELD = "parameters";
	private static final String OWNERFIELD = "owner";



	private static final int ACTION_CREATE = 1;
	private static final int ACTION_UPDATE = 2;
	private static final int ACTION_DELETE = 3;

	public EventsStack() throws ClassNotFoundException, SQLException {
		super(DBEVENTSPATH);




	}





	public void createEventsFromScan(String UUID,Map<String,String> createdFiles,Map<String,String> updatedFiles,Set<String> deletedFiles)
	{
		m_Events.clear();
		for (String t : deletedFiles)
		{
			try
			{
				Event e = new Event(t,null,ACTION_DELETE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		for(Entry<String, String> entry : createdFiles.entrySet()) {
			try
			{
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_CREATE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		for(Entry<String, String> entry : updatedFiles.entrySet()) {
			try
			{
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_UPDATE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		save();
	}
	
	
	public void addEvent(Event e)
	{
		m_Events.add(e);
	}

	/**
	 *  Retourne les derniers événements en base de données, pour chaque fichier.
	 *  Ne prends pas en compte les événements "Suppression"
	 *  @param UID : UID du dossier que l'on veut "Mappifier"
	 * 	@return  Map nom_de_fichier,hash
	 */
	public Map<String,String> toMap(String UID)
	{
		Map<String,String> res = new Hashtable<String,String>();



		try
		{
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
		save(System.currentTimeMillis());

	}

	public void save(long d) {


		for (Event e : m_Events)
		{
			//if(e.getDate().after(d))
			e.save();

		}


	}




}
