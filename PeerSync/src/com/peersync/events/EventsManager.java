package com.peersync.events;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.peersync.models.ShareFolder;

public class EventsManager {

	private EventsStack m_EventsStack;
	private Map<String,String> currentStack = new Hashtable<String,String>();

	List<ShareFolder> m_directories = new LinkedList<ShareFolder>();



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
			Iterator<ShareFolder> it = m_directories.iterator();

			boolean toAdd=true;
			while(toAdd && it.hasNext())
			{

				String tmp = it.next().getAbsFolderRootPath(); //Pour éviter les embrouilles du genre deux dossiers test et test1 cote à cote, on fait +"\\"
				if(DataBaseManager.getDataBaseManager().getSharedFolderRootPath(sd).contains(tmp+"\\"))
					toAdd=false;
				else if( tmp.contains(DataBaseManager.getDataBaseManager().getSharedFolderRootPath(sd)+"\\"))
					it.remove();



			}


			if(toAdd)
				m_directories.add(new ShareFolder(sd));


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
					currentStack = DataBaseManager.getDataBaseManager().getLastEvents(dir.getUID());
					DirectoryReader.getDirectoryReader().scanDifferences(currentStack,dir);
					m_EventsStack.createEventsFromScan(dir.getUID(), DirectoryReader.getDirectoryReader().getNewFilesMap(),DirectoryReader.getDirectoryReader().getUpdatedFilesMap(),DirectoryReader.getDirectoryReader().getDeletedFilesSet());
				}
			}
		}, 0, 20000);
	}



}
