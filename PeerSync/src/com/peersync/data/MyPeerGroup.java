package com.peersync.data;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.rendezvous.RendezVousService;

import com.peersync.DiscoveryBehaviour;
import com.peersync.PeerGroupManager;

public class MyPeerGroup {

	
	
	private PeerGroupID id;
	private DiscoveryBehaviour dicoveryManager;
	private PeerGroupManager groupManager;
	private PeerGroup peerGroup;
	public String peerGroupName;

	public MyPeerGroup(PeerGroupManager peerGroupManager, PeerGroupID psepeergroupid, String peerGroupName)  {
		this.id = psepeergroupid;
		this.peerGroupName = peerGroupName;
		this.groupManager = peerGroupManager;
		this.dicoveryManager = new DiscoveryBehaviour(this);
		dicoveryManager.start();
	}

	public DiscoveryService getNetPeerGroupDiscoveryService() {
		return groupManager.getNetDiscoveryService();
	}

	public PeerGroup getNetPeerGroup() {
		return groupManager.getNetPeerGroup();
	}
	
	public DiscoveryService getPeerGroupDiscoveryService() {
		return peerGroup.getDiscoveryService();
	}

	public PeerGroup getPeerGroup() {
		return peerGroup;
	}
	public void setPeerGroup(PeerGroup mPeerGroup) {
		this.peerGroup = mPeerGroup;
		if(peerGroup!=null)
			peerGroup.getRendezVousService().setAutoStart(true, 100000);
	}

	public RendezVousService getRendezVousService() {
		return peerGroup.getRendezVousService();
	}
	
	public DiscoveryService getDiscoveryService() {
		return peerGroup.getDiscoveryService();
	}
	

}
