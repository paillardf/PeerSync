package com.peersync.network.group;

import java.util.ArrayList;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.behaviour.AbstractBehaviour;
import com.peersync.network.behaviour.ContentBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;

public class MyPeerGroup {

	
	
	private PeerGroupManager groupManager;
	private PeerGroup peerGroup;
	public String peerGroupName;
	private ArrayList<AbstractBehaviour> behaviourList = new ArrayList<AbstractBehaviour>();
	private PeerGroupID peerGroupId;
	
	public MyPeerGroup(PeerGroupManager peerGroupManager, PeerGroupID psepeergroupid, String peerGroupName)  {
		this.peerGroupName = peerGroupName;
		this.peerGroupId=psepeergroupid;
		this.groupManager=peerGroupManager;
		
		DiscoveryBehaviour db = new DiscoveryBehaviour(this);
		behaviourList.add(db);
		behaviourList.add(new StackSyncBehaviour(this));
		behaviourList.add(new ContentBehaviour(this));
		//DiscoveryBehaviour had to be start before the others in order to initialize the peerGroup.
		db.start();
	}

	public DiscoveryService getNetPeerGroupDiscoveryService() {
		return getGroupManager().getNetDiscoveryService();
	}

	public PeerGroupID getPeerGroupID(){
		return peerGroupId;
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
						notifyPeerGroup(new PeerGroupEvent(PeerGroupEvent.RDV_CONNECTION, peerGroup.getPeerGroupID(), event));
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

	public void notifyPeerGroup(PeerGroupEvent e) {
		for (AbstractBehaviour behaviour : behaviourList) {
				behaviour.notifyPeerGroup(e);
		}
		
	}
	

	public void removeBehaviour(AbstractBehaviour abstractBehaviour) {
		behaviourList.remove(abstractBehaviour);
		if(abstractBehaviour.isAlive())
			abstractBehaviour.interrupt();
	}

	public String getPeerGroupName() {
		return peerGroupName;
	}
	

}
