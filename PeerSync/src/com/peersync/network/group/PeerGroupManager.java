package com.peersync.network.group;

import java.util.ArrayList;

import com.peersync.network.PeerManager;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;


public class PeerGroupManager {

	
	
	private PeerManager peer;
	private PeerGroup netPeerGroup;
	private ArrayList<MyPeerGroup> peerGroupList;
	
	public PeerGroupManager(PeerManager peerManager, PeerGroup netPeerGroup) {
		this.peer = peerManager;
		this.netPeerGroup = netPeerGroup;
		peerGroupList = new ArrayList<MyPeerGroup>();
		netPeerGroup.getRendezVousService().addListener(new RendezvousListener() {
			
			@Override
			public void rendezvousEvent(RendezvousEvent event) {
				if(event.getType()==RendezvousEvent.RDVCONNECT){
					
					for (MyPeerGroup peerGroup : peerGroupList) {
						peerGroup.notifyNetPeerGroupRDVConnection(event.getPeerID());
					}
				}
				
			}
		});
	}

	public void addPeerGroupToManage(PeerGroupID psepeergroupid, String peerGroupName) {
		MyPeerGroup pg = new MyPeerGroup(this, psepeergroupid, peerGroupName);
		peerGroupList.add(pg);
	}

	public DiscoveryService getNetDiscoveryService() {
		return netPeerGroup.getDiscoveryService();
	}

	public PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}

	public NetworkConfigurator getConfig() {
		return peer.conf;
		
	}

}
