package com.peersync.network.behaviour;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.impl.endpoint.IPUtils;
import net.jxta.impl.endpoint.TransportUtils;
import net.jxta.impl.id.UUID.IDFormat;
import net.jxta.peer.PeerInfoEvent;

import com.peersync.network.group.MyPeerGroup;

public abstract class AbstractBehaviour extends Thread implements DiscoveryListener{


	protected MyPeerGroup myPeerGroup;

	public AbstractBehaviour(MyPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;


	}



	public abstract void run();
	protected abstract void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum);
	public abstract void notifyNetPeerGroupRDVConnection(ID id);
	public abstract void notifyPeerGroupRDVConnection(ID id);
	
	

	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		// String found_peer_id = "urn:jxta:" + event.getSource().toString().substring(7);   
		parseAdvertisement(event.getSearchResults());

	}
	
	
}
