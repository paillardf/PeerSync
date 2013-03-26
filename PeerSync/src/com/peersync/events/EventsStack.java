package com.peersync.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;



public class EventsStack extends DbliteConnection {

	private Vector<Event> m_Events;

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
		m_Events = new Vector<Event>();
		try
		{
			//			Event e = new Event(Calendar.getInstance().getTime().getTime(),"C:\\Users\\Nicolas.leleu\\Documents\\testTX\\be",1,"Nicolas","UUID1");
			//			addEvent(e);
			//			save();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}


	}





	public void createEventsFromScan(Map<String,String> createdFiles,Map<String,String> updatedFiles,Set<String> deletedFiles)
	{
		m_Events.clear();
		for (String t : deletedFiles)
		{
			try
			{
				Event e = new Event(t,null,ACTION_DELETE,"Nicolas","UUID1");
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
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_CREATE,"Nicolas","UUID1");
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
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_UPDATE,"Nicolas","UUID1");
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

	public Map<String,String> toMap()
	{
		Map<String,String> res = new Hashtable<String,String>();



			try
		{
				Statement statement = getConnection().createStatement();
				statement.setQueryTimeout(30);  // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery("select e1."+FILEPATHFIELD+",e1."+HASHFIELD+", sf."+ROOTPATHFIELD+" "+
						"from "+DBEVENTSTABLE+" e1 left join "+DBSHAREDFOLDERSTABLE+" sf on (e1."+SHAREDFOLDERFIELD+"=sf."+UUIDFIELD+")  where e1."+ACTIONFIELD+" <> 3 and  e1."+DATEFIELD+" = " +
						"(select max(date) from "+DBEVENTSTABLE+" where "+FILEPATHFIELD+" = e1."+FILEPATHFIELD+")");
				
				 
	
				while(rs.next())
				{
					// read the result set
					if(rs.getString(ROOTPATHFIELD)!=null)
					{
						String absolutePath = Event.AbsoluteFromRelativePath(rs.getString(FILEPATHFIELD), rs.getString(ROOTPATHFIELD)) ;
						res.put(absolutePath, rs.getString(HASHFIELD));
						//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
						//		        System.out.println("id = " + rs.getString(HASHFIELD));
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

	//	public Date lastEventInDir(String dirPath) throws Exception
	//	{
	//		 
	//		try
	//		{
	//			Statement statement = getConnection().createStatement();
	//			statement.setQueryTimeout(30);  // set timeout to 30 sec.
	//			statement.executeQuery("select where datetime(date) = select max(datetime(date)) from events where filepath like +");
	//
	//		}
	//		catch(SQLException e)
	//		{
	//			// if the error message is "out of memory", 
	//			// it probably means no database file is found
	//			System.err.println(e.getMessage());
	//		}
	//		
	//	}

	public void save()
	{
		save(Calendar.getInstance().getTime().getTime());

	}

	public void save(long d) {


		for (Event e : m_Events)
		{
			//if(e.getDate().after(d))
			e.save();

		}


	}




}
