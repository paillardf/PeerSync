package com.peersync.events;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class EventsManager {

	private EventsStack m_EventsStack;
	private Map<String,String> currentStack = new Hashtable<String,String>();
	
	List<String> m_directories = new LinkedList<String>();
	
	DirectoryReader m_directoryReader = new DirectoryReader();
	
	 
	 public EventsManager()
	 {
		 try {
			m_EventsStack = new EventsStack();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 m_directories.add("C:\\Users\\Nicolas.leleu\\Documents\\testTX");
		
	 }
	 
	 
	public void start()
	{
	 Timer timer = new Timer();
     timer.schedule (new TimerTask() {
         public void run()
         {
        	 currentStack = m_EventsStack.toMap();
        	 m_directoryReader.scanDifferences(currentStack,m_directories);
 			m_EventsStack.createEventsFromScan(m_directoryReader.getNewFilesMap(),m_directoryReader.getUpdatedFilesMap(),m_directoryReader.getDeletedFilesSet());
         }
     }, 0, 20000);
	}
	

}
