package com.peersync.network.behaviour;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.BasicPeerGroup;

public abstract class AbstractBehaviour  implements DiscoveryListener, Runnable{


	private Thread.State statue = Thread.State.RUNNABLE;
	protected BasicPeerGroup myPeerGroup;
	private long nextExecutionTime = 0;
	
	public AbstractBehaviour(BasicPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;
	}

	
	public void terminated() {
		statue  = Thread.State.TERMINATED;
	};

	
	@Override
	public void run() {
		nextExecutionTime = action()+System.currentTimeMillis();
	}
	
	public boolean hasToRun(){
		if(System.currentTimeMillis()>=nextExecutionTime&&statue==Thread.State.RUNNABLE){
			return true;
		}
		return false;
	}
	
	protected abstract int action();
	public void initialize(){};
	protected abstract void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum);
	public abstract void notifyPeerGroup(PeerGroupEvent event);
	
	

	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		parseAdvertisement(event.getSearchResults());
	}


	public long getNextExecutionTime() {
		return nextExecutionTime;
	}
	
	
}
