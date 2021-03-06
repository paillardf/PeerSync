package com.peersync.network.group;

import java.util.List;

import net.jxta.content.ContentProvider;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.network.behaviour.ContentBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;
import com.peersync.network.content.SyncContentProvider;

public class SyncPeerGroup extends BasicPeerGroup {

	
	
	private SyncContentProvider syncContentProvider;

	public SyncPeerGroup(PeerGroupID psepeergroupid, String peerGroupName,String description)  {
		super( psepeergroupid,  peerGroupName,description);
		behaviourList.add(new DiscoveryBehaviour(this));
		behaviourList.add(new StackSyncBehaviour(this));
		behaviourList.add(new ContentBehaviour(this));
		
		
		
	}

	@Override
	protected void initPeerGroupParameters() throws PeerGroupException {
		super.initPeerGroupParameters();
		List<ContentProvider> contentsProvider = peerGroup.getContentService().getActiveContentProviders();
		for (ContentProvider contentProvider : contentsProvider) {
			peerGroup.getContentService().removeContentProvider(contentProvider);
		}
		syncContentProvider = new SyncContentProvider();
		peerGroup.getContentService().addContentProvider(syncContentProvider); //TODO Utile?
		
	}

	
	public SyncContentProvider getContentProvider(){
		return syncContentProvider;
	}

}
