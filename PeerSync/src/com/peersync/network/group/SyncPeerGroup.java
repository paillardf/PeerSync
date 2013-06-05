package com.peersync.network.group;

import java.util.List;

import net.jxta.content.ContentProvider;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.network.behaviour.ContentBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;
import com.peersync.network.content.SyncContentProvider;

public class SyncPeerGroup extends BasicPeerGroup {

	
	
	private SyncContentProvider syncContentProvider;

	public SyncPeerGroup(PeerGroupID psepeergroupid, String peerGroupName)  {
		super( psepeergroupid,  peerGroupName);
		behaviourList.add(new DiscoveryBehaviour(this));
		behaviourList.add(new StackSyncBehaviour(this));
		behaviourList.add(new ContentBehaviour(this));
		
		
		
	}

	@Override
	protected void initPeerGroupParameters() {
		super.initPeerGroupParameters();
		List<ContentProvider> contentsProvider = peerGroup.getContentService().getActiveContentProviders();
		for (ContentProvider contentProvider : contentsProvider) {
			peerGroup.getContentService().removeContentProvider(contentProvider);
		}
		syncContentProvider = new SyncContentProvider();
		peerGroup.getContentService().addContentProvider(syncContentProvider);
		
	}

	
	public SyncContentProvider getContentProvider(){
		return syncContentProvider;
	}

}
