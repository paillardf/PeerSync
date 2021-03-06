package com.peersync.network.behaviour;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jxta.content.Content;
import net.jxta.content.ContentID;
import net.jxta.content.ContentService;
import net.jxta.content.ContentShare;
import net.jxta.content.ContentTransfer;
import net.jxta.content.ContentTransferEvent;
import net.jxta.content.ContentTransferListener;
import net.jxta.content.ContentTransferState;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.PSECredential;
import net.jxta.protocol.ContentShareAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.models.PeerGroupEvent;
import com.peersync.models.SharedFolder;
import com.peersync.network.content.SyncContentProvider;
import com.peersync.network.content.model.FolderDocument;
import com.peersync.network.content.transfer.SyncFolderTransfer;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.group.SyncPeerGroup;
import com.peersync.tools.Log;

public class ContentBehaviour extends AbstractBehaviour{

	private long lastShareContentAdvertisment=0;
	private ArrayList<SharedFolder> currentlySharedFiles = new ArrayList<SharedFolder>();
	private Map<String, SyncFolderTransfer> currentTransfer = new HashMap<String, SyncFolderTransfer>();
	private ContentService service;
	private DataBaseManager db;

	private static final long UPDATE_CONTENT_DELAY = 8*60*1000;
	private static final long VALIDITY_CONTENT_ADV = 9*60*1000;


	public ContentBehaviour(BasicPeerGroup peerGroup){
		super(peerGroup);
	}

	@Override
	public void initialize() {
		service = myPeerGroup.getPeerGroup().getContentService();
		db = DataBaseManager.getInstance();
		//myPeerGroup.getPSECredential().tlsKeyBridge(pseCredentialKeyRetriever)
	};


	private void publishShareContentAdvertisement()  {

		ArrayList<SharedFolder> folders = db.getSharedFolders(myPeerGroup.getPeerGroup().getPeerGroupID().toString());
		ArrayList<SharedFolder>  unsharedFolder = (ArrayList<SharedFolder>) currentlySharedFiles.clone();
		unsharedFolder.removeAll(folders);


		for (SharedFolder sharedFolder : unsharedFolder) {
			try{
				service.unshareContent((ContentID)IDFactory.fromURI(new URI(sharedFolder.getUID())));
				Log.i("remove "+sharedFolder.getUID()+"from publish");
			}catch(URISyntaxException e){

			}
		}

		currentlySharedFiles = folders;
		for (SharedFolder sharedFolder : folders) {
			try{
				FolderDocument fileDoc = new FolderDocument(MimeMediaType.AOS);
				Content content = new Content((ContentID)IDFactory.fromURI(new URI(sharedFolder.getUID())), null, fileDoc);
				List<ContentShare> shares =((SyncPeerGroup)myPeerGroup).getContentProvider().shareContent(content);

				DiscoveryService discoService = myPeerGroup.getDiscoveryService();
				for (ContentShare share : shares) {


					ContentShareAdvertisement adv = share.getContentShareAdvertisement();
					adv.sign(myPeerGroup.getPSECredential(), false, true);
					try {
						discoService.publish(adv, VALIDITY_CONTENT_ADV, VALIDITY_CONTENT_ADV);
						discoService.remotePublish(adv, VALIDITY_CONTENT_ADV);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Log.i("add "+sharedFolder.getUID()+" to publish");
			}catch(URISyntaxException e){

			}
		}
		lastShareContentAdvertisment  = System.currentTimeMillis();

	}



	@Override
	protected int action() {

		if(System.currentTimeMillis()-lastShareContentAdvertisment>UPDATE_CONTENT_DELAY){
			publishShareContentAdvertisement();

		}
		checkTransferStatus();

		SyncContentProvider contentProvider = ((SyncPeerGroup)myPeerGroup).getContentProvider();

		for (SharedFolder sf : db.getSharedFolders(myPeerGroup.getPeerGroupID().toString())) {
			if(db.getFilesToDownload(sf.getUID()).size()>0){
				if(!currentTransfer.containsKey(sf.getUID())){
					final String uid = sf.getUID();
					ContentTransfer transfer;
					try {
						transfer = contentProvider.retrieveContent((ContentID)IDFactory.fromURI(new URI(sf.getUID())));
						((SyncFolderTransfer)transfer).startSynchronization();
						transfer.addContentTransferListener(new ContentTransferListener() {

							@Override
							public void contentTransferStateUpdated(ContentTransferEvent event) {
								;
								switch (event.getTransferState()) {
								case CANCELLED :
								case FAILED :
									currentTransfer.remove(event.getContentTransfer());
									Log.i("Transfert "+uid+" failed or cancelled");
								case COMPLETED :
									currentTransfer.remove(event.getContentTransfer());
									Log.i("Transfert "+uid+" terminated");
									break;
								default:
									Log.i("Transfert "+uid+" state changed to "+event.getTransferState());
									break;
								}
							}

							@Override
							public void contentTransferProgress(ContentTransferEvent event) {
							}

							@Override
							public void contentLocationStateUpdated(ContentTransferEvent event) {
							}
						});
						currentTransfer.put(sf.getUID(), ((SyncFolderTransfer)transfer));
						Log.i("synchronize folder "+sf.getUID());
					} catch (UnsupportedOperationException | URISyntaxException e) {
						e.printStackTrace();
					}
				}else{
					currentTransfer.get(sf.getUID()).isRunning();
				}
			}
		}
		return 5000;	

	}


	private void checkTransferStatus() {
		ContentTransferState transferState;
		for (String key  : currentTransfer.keySet()) {
			transferState = currentTransfer.get(key).getTransferState();
			if (transferState.isFinished()) {
				//if (transferState.isSuccessful()) {
				currentTransfer.remove(key);
				//	            } else {
				//	            	currentTransfer.remove(key);
			}
		}



	}

	@Override
	protected void parseAdvertisement(Advertisement advertisement) {

	}

	@Override
	public void notifyPeerGroup(PeerGroupEvent event) {
		if(myPeerGroup.getNetPeerGroup().getPeerGroupID().toString().equals(event.getPeerGroupID().toString())){
			//NETPEERGROUPEVENT
		}else{

			switch (event.getID()) {
			case PeerGroupEvent.RDV_CONNECTION:
				lastShareContentAdvertisment = 0;
				break;
			case PeerGroupEvent.STACK_UPDATE:
				nextExecutionTime = 0;
				break;
			}

		}
	}


}
