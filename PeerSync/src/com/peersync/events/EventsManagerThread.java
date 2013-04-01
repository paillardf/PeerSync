package com.peersync.events;

public class EventsManagerThread extends Thread{
	
	private static EventsManagerThread instance;
	
	public static EventsManagerThread getEventsManagerThread()
	{
		if(instance==null)
			instance = new EventsManagerThread();
		return instance;
	}
	
	private EventsManagerThread()
	{
		
	}
	
	public void run()
	{
		EventsManager.getEventsManager().launch();
		
	}

}
