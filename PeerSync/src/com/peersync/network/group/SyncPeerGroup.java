package com.peersync.network.group;

import java.io.File;
import java.util.List;

import net.jxta.content.Content;
import net.jxta.content.ContentProvider;
import net.jxta.document.FileDocument;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;

import com.peersync.network.behaviour.ContentBehaviour;
import com.peersync.network.behaviour.DiscoveryBehaviour;
import com.peersync.network.behaviour.StackSyncBehaviour;
import com.peersync.network.content.SyncContentProvider;

public class SyncPeerGroup extends BasicPeerGroup {

	
	
	private SyncContentProvider syncContentProvider;

	public SyncPeerGroup(PeerGroup netPeerGroup, NetworkConfigurator conf, PeerGroupID psepeergroupid, String peerGroupName)  {
		super(netPeerGroup, conf,  psepeergroupid,  peerGroupName);
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

	public void shareContent(Content content) {
		peerGroup.getContentService().shareContent(content);
		
	}
	
	public SyncContentProvider getContentProvider(){
		return syncContentProvider;
	}

}
