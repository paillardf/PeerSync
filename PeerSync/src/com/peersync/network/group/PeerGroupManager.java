package com.peersync.network.group;

import java.util.ArrayList;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.PeerSync;


public class PeerGroupManager {

	
	
	private PeerSync peer;
	private PeerGroup netPeerGroup;
	private ArrayList<BasicPeerGroup> peerGroupList;
	
	public PeerGroupManager(PeerSync peerManager, PeerGroup netPeerG) {
		this.peer = peerManager;
		this.netPeerGroup = netPeerG;
		peerGroupList = new ArrayList<BasicPeerGroup>();
		netPeerGroup.getRendezVousService().addListener(new RendezvousListener() {
			
			@Override
			public void rendezvousEvent(RendezvousEvent event) {
				if(event.getType()==RendezvousEvent.RDVCONNECT){
					
					for (BasicPeerGroup peerGroup : peerGroupList) {
						peerGroup.notifyPeerGroupBehaviour(new PeerGroupEvent(PeerGroupEvent.RDV_CONNECTION, netPeerGroup.getPeerGroupID(), event));
					}
				}
				
			}
		});
	}

	public void addPeerGroupToManage(BasicPeerGroup bpg) {
		peerGroupList.add(bpg);
		peer.getScanService().addObserver(bpg);
	}

	public DiscoveryService getNetDiscoveryService() {
		return netPeerGroup.getDiscoveryService();
	}

	public PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}


	
	

}
