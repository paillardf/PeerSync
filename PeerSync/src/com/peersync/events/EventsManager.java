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












	public void launch()
	{
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{
				DirectoryReader dr = DirectoryReader.getDirectoryReader();
				dr.scan();	

			}
		}, 0, 20000);
	}



}
