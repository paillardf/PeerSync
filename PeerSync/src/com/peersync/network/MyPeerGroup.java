package com.peersync.network;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.network.behaviour.CommunicationBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;

public class MyPeerGroup {

	
	
	private PeerGroupID id;
	private DiscoveryBehaviour dicoveryManager;
	private PeerGroupManager groupManager;
	private PeerGroup peerGroup;
	public String peerGroupName;
	private CommunicationBehaviour communicationBehaviour;

	public MyPeerGroup(PeerGroupManager peerGroupManager, PeerGroupID psepeergroupid, String peerGroupName)  {
		this.id = psepeergroupid;
		this.peerGroupName = peerGroupName;
		this.setGroupManager(peerGroupManager);
		this.dicoveryManager = new DiscoveryBehaviour(this);
		dicoveryManager.start();
		this.communicationBehaviour = new CommunicationBehaviour(this);
		
	}

	public DiscoveryService getNetPeerGroupDiscoveryService() {
		return getGroupManager().getNetDiscoveryService();
	}

	public PeerGroup getNetPeerGroup() {
		return getGroupManager().getNetPeerGroup();
	}
	
	public DiscoveryService getPeerGroupDiscoveryService() {
		return peerGroup.getDiscoveryService();
	}

	public PeerGroup getPeerGroup() {
		return peerGroup;
	}
	public void setPeerGroup(PeerGroup mPeerGroup) {
		this.peerGroup = mPeerGroup;
		if(peerGroup!=null){
			getRendezVousService().setAutoStart(true,10000);
			
			//communicationBehaviour.start();
		}
	}

	public RendezVousService getRendezVousService() {
		return peerGroup.getRendezVousService();
	}
	
	public DiscoveryService getDiscoveryService() {
		return peerGroup.getDiscoveryService();
	}

	public PeerGroupManager getGroupManager() {
		return groupManager;
	}

	public void setGroupManager(PeerGroupManager groupManager) {
		this.groupManager = groupManager;
	}
	

}
