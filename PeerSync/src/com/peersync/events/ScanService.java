package com.peersync.events;

import java.io.IOException;
import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.jxta.peer.PeerID;

import com.peersync.data.DataBaseManager;
import com.peersync.exceptions.JxtaNotInitializedException;
import com.peersync.network.PeerSync;

public class ScanService extends Observable {


	private static ScanService instance=null;

	private boolean running;
	private DirectoryReader directoryReader;


	public static ScanService getInstance()
	{
		if(instance==null)
			instance = new ScanService();
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

	private ScanService() 
	{
		running=false;
		directoryReader = new DirectoryReader();
	}







	public void startService() throws IOException, JxtaNotInitializedException
	{
		String pgidStr=null;
		PeerID pgid = PeerSync.getInstance().getConf().getPeerID();
		if(pgid==null)
			throw new JxtaNotInitializedException("Can't start scan service :");
		else
			pgidStr= pgid.toString();
		if(!pgid.equals(directoryReader.getPeerID()))
				directoryReader.setPeerID(pgidStr);
		
		
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
