package com.peersync.network.group;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;

import com.peersync.network.behaviour.ContentBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;

public class SyncPeerGroup extends BasicPeerGroup {

	
	
	public SyncPeerGroup(PeerGroup netPeerGroup, NetworkConfigurator conf, PeerGroupID psepeergroupid, String peerGroupName)  {
		super(netPeerGroup, conf,  psepeergroupid,  peerGroupName);
		//behaviourList.add(new DiscoveryBehaviour(this));
		behaviourList.add(new StackSyncBehaviour(this));
		//behaviourList.add(new ContentBehaviour(this));
		
	}


}
