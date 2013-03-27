package com.peersync.network.behaviour;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;

import com.peersync.network.MyPeerGroup;

public abstract class AbstractBehaviour extends Thread implements DiscoveryListener{


	protected MyPeerGroup myPeerGroup;

	public AbstractBehaviour(MyPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;


	}



	public abstract void run();
	protected abstract void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum);
	
	
	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		parseAdvertisement(event.getSearchResults());

	}
	
	
}
