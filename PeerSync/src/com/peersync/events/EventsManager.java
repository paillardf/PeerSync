package com.peersync.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.peersync.data.DataBaseManager;
import com.peersync.models.EventsStack;
import com.peersync.models.SharedFolder;

public class EventsManager {

	private EventsStack m_EventsStack;
	

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

		ArrayList<String> sharedDirectories = DataBaseManager.getInstance().getAllSharedDirectories();
		for(String sd : sharedDirectories)
		{
			Iterator<SharedFolder> it = m_directories.iterator();

			boolean toAdd=true;
			while(toAdd && it.hasNext())
			{

				String tmp = it.next().getAbsFolderRootPath(); //Pour éviter les embrouilles du genre deux dossiers test et test1 cote à cote, on fait +"\\"
				if(DataBaseManager.getInstance().getSharedFolderRootPath(sd).contains(tmp+"\\"))
					toAdd=false;
				else if( tmp.contains(DataBaseManager.getInstance().getSharedFolderRootPath(sd)+"\\"))
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
				DataBaseManager db = DataBaseManager.getInstance();
				
				DirectoryReader dr = DirectoryReader.getDirectoryReader(m_directories);
				
				for (SharedFolder shareFolder : m_directories) {
					Map<String,String> currentStack = DataBaseManager.getInstance().getLastEvents(shareFolder.getUID());
					dr.scanDifferences(currentStack,shareFolder);
					dr.getEventsStack().save();
				}
					
			}
		}, 0, 20000);
	}



}
