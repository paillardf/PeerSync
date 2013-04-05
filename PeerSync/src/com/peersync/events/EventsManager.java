package com.peersync.events;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.peersync.data.DataBaseManager;
import com.peersync.models.EventsStack;
import com.peersync.models.SharedFolder;

public class EventsManager {

	private EventsStack m_EventsStack;
	private Map<String,String> currentStack = new Hashtable<String,String>();

	ArrayList<SharedFolder> m_directories = new ArrayList<SharedFolder>();



	private static EventsManager instance;

	public static EventsManager getEventsManager() 
	{
		if(instance==null)
			instance = new EventsManager();
		return instance;

	}


	private EventsManager() 
	{
		m_EventsStack = new EventsStack();

		loadDirectoriesToScan();


	}

	public void loadDirectoriesToScan()
	{
		m_directories.clear();

		ArrayList<String> sharedDirectories = DataBaseManager.getDataBaseManager().getAllSharedDirectories();
		for(String sd : sharedDirectories)
		{
			Iterator<SharedFolder> it = m_directories.iterator();

			boolean toAdd=true;
			while(toAdd && it.hasNext())
			{

				String tmp = it.next().getAbsFolderRootPath(); //Pour �viter les embrouilles du genre deux dossiers test et test1 cote � cote, on fait +"\\"
				if(DataBaseManager.getDataBaseManager().getSharedFolderRootPath(sd).contains(tmp+"\\"))
					toAdd=false;
				else if( tmp.contains(DataBaseManager.getDataBaseManager().getSharedFolderRootPath(sd)+"\\"))
					it.remove();




			}


			if(toAdd)
			{
				m_directories.add(new SharedFolder(sd));
			}


		}




	}






	public void launch()
	{
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{
				currentStack = DataBaseManager.getDataBaseManager().getLastEvents();
				
					DirectoryReader.getDirectoryReader().scanDifferences(currentStack,m_directories);
					
					//m_EventsStack.createEventsFromScan(dir.getUID(), DirectoryReader.getDirectoryReader().getNewFilesMap(),DirectoryReader.getDirectoryReader().getUpdatedFilesMap(),DirectoryReader.getDirectoryReader().getDeletedFilesSet());
				
				DirectoryReader.getDirectoryReader().getEventsStack().save();
			}
		}, 0, 20000);
	}



}
