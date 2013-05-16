package com.peersync.events;

import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.peersync.data.DataBaseManager;

public class ScanService extends Observable {




	private boolean running;
	private DirectoryReader directoryReader;

	

	private boolean getRunning()
	{
		return running;
	}

	private void setRunning(boolean r)
	{
		running=r;
	}

	public ScanService(String peerID) 
	{
		running=false;
		directoryReader = new DirectoryReader(peerID);
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
					Set<String> peerGroupWithNewEvents = directoryReader.scan();
					for (String peerGroupId : peerGroupWithNewEvents)
					{
						notifyObservers(peerGroupId);
					}
					setChanged();
					DataBaseManager.exclusiveAccess.unlock();
				}



			}
		}, 0, 10000);
	}




}
