package com.peersync.network.group;

import java.util.ArrayList;

import net.jxta.discovery.DiscoveryService;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.network.behaviour.AbstractBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;

public class MyPeerGroup {

	
	
	private PeerGroupManager groupManager;
	private PeerGroup peerGroup;
	public String peerGroupName;
	private ArrayList<AbstractBehaviour> behaviourList = new ArrayList<AbstractBehaviour>();
	
	public MyPeerGroup(PeerGroupManager peerGroupManager, PeerGroupID psepeergroupid, String peerGroupName)  {
		this.peerGroupName = peerGroupName;
		this.groupManager=peerGroupManager;
		
		DiscoveryBehaviour db = new DiscoveryBehaviour(this);
		behaviourList.add(db);
		behaviourList.add(new StackSyncBehaviour(this));
		
		//DiscoveryBehaviour had to be start before the others in order to initialize the peerGroup.
		db.start();
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
			getRendezVousService().setAutoStart(true,30000);
			startBehaviour();
			peerGroup.getRendezVousService().addListener(new RendezvousListener() {
				
				@Override
				public void rendezvousEvent(RendezvousEvent event) {
					if(event.getType()==RendezvousEvent.RDVCONNECT){
						notifyPeerGroupRDVConnection(event.getPeerID());
					}
					
					
				}
			});
		}
	}

	private void startBehaviour() {
		for (AbstractBehaviour behaviour : behaviourList) {
			if(!behaviour.isAlive())
				behaviour.start();
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

	public void notifyNetPeerGroupRDVConnection(ID id) {
		for (AbstractBehaviour behaviour : behaviourList) {
				behaviour.notifyNetPeerGroupRDVConnection(id);
		}
		
	}
	private void notifyPeerGroupRDVConnection(ID id) {
		for (AbstractBehaviour behaviour : behaviourList) {
				behaviour.notifyPeerGroupRDVConnection(id);
		}
		
	}

	public void removeBehaviour(AbstractBehaviour abstractBehaviour) {
		behaviourList.remove(abstractBehaviour);
	}
	

}
