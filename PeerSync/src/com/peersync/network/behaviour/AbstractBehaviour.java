package com.peersync.network.behaviour;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.MyPeerGroup;

public abstract class AbstractBehaviour extends Thread implements DiscoveryListener{


	protected MyPeerGroup myPeerGroup;

	public AbstractBehaviour(MyPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;


	}



	public abstract void run();
	protected abstract void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum);
	//public abstract void notifyNetPeerGroup(PeerInfoEvent event);
	public abstract void notifyPeerGroup(PeerGroupEvent event);
	
	

	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		// String found_peer_id = "urn:jxta:" + event.getSource().toString().substring(7);   
		parseAdvertisement(event.getSearchResults());

	}
	
	
}
