package com.peersync.events;

import java.util.Timer;
import java.util.TimerTask;

public class EventsManager {





	private static EventsManager instance;

	public static EventsManager getEventsManager() 
	{
		if(instance==null)
			instance = new EventsManager();
		return instance;

	}


	private EventsManager() 
	{

	}

<<<<<<< HEAD
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


=======
>>>>>>> 66987430f29bdb1c31748b8ca4901cc957596d12











	public void launch()
	{
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{
<<<<<<< HEAD
				DataBaseManager db = DataBaseManager.getInstance();
				
				DirectoryReader dr = DirectoryReader.getDirectoryReader(m_directories);
				
				for (SharedFolder shareFolder : m_directories) {
					Map<String,String> currentStack = DataBaseManager.getInstance().getLastEvents(shareFolder.getUID());
					dr.scanDifferences(currentStack,shareFolder);
					dr.getEventsStack().save();
				}
					
=======
				DirectoryReader dr = DirectoryReader.getDirectoryReader();
				dr.scan();	

>>>>>>> 66987430f29bdb1c31748b8ca4901cc957596d12
			}
		}, 0, 20000);
	}



}
