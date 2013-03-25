package com.peersync;

import java.util.ArrayList;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.data.MyPeerGroup;

public class PeerGroupManager {

	
	
	private PeerManager peer;
	private PeerGroup netPeerGroup;
	private ArrayList<MyPeerGroup> peerGroupList;
	
	public PeerGroupManager(PeerManager peerManager, PeerGroup netPeerGroup) {
		this.peer = peerManager;
		this.netPeerGroup = netPeerGroup;
		peerGroupList = new ArrayList<MyPeerGroup>();
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

}
