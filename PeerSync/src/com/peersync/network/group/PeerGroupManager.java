package com.peersync.network.group;

import java.io.IOException;
import java.util.ArrayList;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.exceptions.BasicPeerGroupException;
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


	public void addPeerGroup(BasicPeerGroup peerGroup) {
			peerGroupList.add(peerGroup);
	}

	public void startPeerGroup(PeerGroupID peerGroupID) throws PeerGroupException, ProtocolNotSupportedException, IOException, BasicPeerGroupException {
		BasicPeerGroup peerGroup = getPeerGroup(peerGroupID);
		peerGroup.initialize(netPeerGroup);
		peer.getScanService().addObserver(peerGroup);
		peerGroup.start();
	}
	
	public void stopPeerGroup(PeerGroupID peerGroupID) throws BasicPeerGroupException{
		BasicPeerGroup peerGroup = getPeerGroup(peerGroupID);
		peerGroup.stop();
	}

	public DiscoveryService getNetDiscoveryService() {
		return netPeerGroup.getDiscoveryService();
	}

	public PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}

	public boolean isManage(PeerGroupID id){
		return !(getPeerGroup(id)==null);
	}
	
	public BasicPeerGroup getPeerGroup(PeerGroupID id){
		for (BasicPeerGroup peerGroup : peerGroupList) {
			if(peerGroup.getPeerGroupID().equals(id))
				return peerGroup;
		}
		return null;
	}


}
