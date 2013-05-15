package com.peersync.events;

import java.util.Timer;
import java.util.TimerTask;

import com.peersync.data.DataBaseManager;

public class EventsManager {




	private boolean running;
	private static EventsManager instance;

	public static EventsManager getEventsManager() 
	{
		if(instance==null)
			instance = new EventsManager();
		return instance;

	}

	
	private boolean getRunning()
	{
		return running;
	}
	
	private void setRunning(boolean r)
	{
		running=r;
	}

	private EventsManager() 
	{
		running=false;
	}







	public void startService()
	{
		if(!running)
		{
			launch();
			setRunning(true);
		}
		
		
	}
	
	public void stopService()
	{
		setRunning(false);
		
		
	}
	





	private void launch()
	{
		
		Timer timer = new Timer();
		timer.schedule (new TimerTask() {
			public void run()
			{
				if(!getRunning()) //Point observable
				{
					this.cancel();
				}
				else
				{
					DataBaseManager.exclusiveAccess.lock();
					DirectoryReader dr = DirectoryReader.getDirectoryReader();
					dr.scan();
					DataBaseManager.exclusiveAccess.unlock();
				}
			
				

			}
		}, 0, 10000);
	}




}
