package com.peersync.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.peersync.models.ShareFolder;
import com.peersync.models.StackVersion;

public class EventsManager extends DbliteConnection{

	private EventsStack m_EventsStack;
	private Map<String,String> currentStack = new Hashtable<String,String>();

	List<ShareFolder> m_directories = new LinkedList<ShareFolder>();



	private static EventsManager instance;

	public static EventsManager getEventsManager() throws ClassNotFoundException, SQLException
	{
		if(instance==null)
			instance = new EventsManager();
		return instance;

	}

	private static final String DBEVENTSPATH = "./dblite.db";
	private static final String DBEVENTSTABLE = "events";
	private static final String DATEFIELD = "date";
	private static final String FILEPATHFIELD = "filepath";
	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String SHAREDFOLDERFIELD = "sharedFolder";
	private static final String UUIDFIELD = "uuid";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";
	private static final String OWNERFIELD = "owner";

	private EventsManager() throws ClassNotFoundException, SQLException
	{
		super(DBEVENTSPATH);
		try {
			m_EventsStack = new EventsStack();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadDirectoriesToScan();


	}

	public void loadDirectoriesToScan()
	{
		m_directories.clear();
		Statement statement;
		try {
			statement = getConnection().createStatement();

			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select sf."+UUIDFIELD+",sf."+ROOTPATHFIELD+
					" from "+DBSHAREDFOLDERSTABLE+" sf");


			while(rs.next())
			{
				addDirectory(rs.getString(UUIDFIELD),rs.getString(ROOTPATHFIELD));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addDirectory(String UUID,String path)
	{


		for(ShareFolder dir : m_directories)
		{
			if(dir.getAbsFolderRootPath().contains(path))
				return;
		}
		try {
			m_directories.add(new ShareFolder(UUID, ""));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}




	public void launch()
	{
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{

				for(ShareFolder dir : m_directories)
				{
					currentStack = m_EventsStack.toMap(dir.getUID());
					DirectoryReader.getDirectoryReader().scanDifferences(currentStack,dir);
					m_EventsStack.createEventsFromScan(dir.getUID(), DirectoryReader.getDirectoryReader().getNewFilesMap(),DirectoryReader.getDirectoryReader().getUpdatedFilesMap(),DirectoryReader.getDirectoryReader().getDeletedFilesSet());
				}
			}
		}, 0, 20000);
	}

	public ArrayList<StackVersion> getStackVersionList(String UID){
		ArrayList<StackVersion> res = new ArrayList<StackVersion>();
		Statement statement;

		try {
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

}
