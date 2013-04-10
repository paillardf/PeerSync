package com.peersync.events;

import java.util.Timer;
import java.util.TimerTask;

import com.peersync.data.DataBaseManager;

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













	public void launch()
	{
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{
				DataBaseManager.exclusiveAccess.lock();
				DirectoryReader dr = DirectoryReader.getDirectoryReader();
				dr.scan();
				DataBaseManager.exclusiveAccess.unlock();
				

			}
		}, 0, 20000);
	}



}
