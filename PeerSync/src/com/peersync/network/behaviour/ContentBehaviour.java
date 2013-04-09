package com.peersync.network.behaviour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.jxta.content.Content;
import net.jxta.content.ContentService;
import net.jxta.content.ContentShare;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.FileDocument;
import net.jxta.document.MimeMediaType;
import net.jxta.protocol.ContentShareAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.models.FileToSync;
import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.MyPeerGroup;


public class ContentBehaviour extends AbstractBehaviour{

	private static final String NAME = "ContentBehaviour";
	private long lastShareContentAdvertisment=0;
	private ArrayList<FileToSync> currentlySharedFiles;
	//	private StackVersionQuery queryHandler;
	//	private static final long VALIDITY_STACKVERSION_ADV = 2*60*1000;
	private static final long PUBLISH_ADVERTISEMENT_DELAY = 10*60*1000;


	public ContentBehaviour(MyPeerGroup peerGroup){
		super(peerGroup);
		//queryHandler = new StackVersionQuery(myPeerGroup);


	}

	@Override
	public void run() {
		ContentService service = myPeerGroup.getPeerGroup().getContentService();
		DataBaseManager db = DataBaseManager.getInstance();
		try {

			while(true){
				sleep(4000);

				if(System.currentTimeMillis()-lastShareContentAdvertisment>PUBLISH_ADVERTISEMENT_DELAY){
					lastShareContentAdvertisment  = System.currentTimeMillis();


					ArrayList<FileToSync> files = db.getFilesWithLocalSource();
					ArrayList<FileToSync>  unsharedFile = (ArrayList<FileToSync>) currentlySharedFiles.clone();
					unsharedFile.removeAll(files);


					for (FileToSync fileToUnshare : unsharedFile) {
						service.unshareContent(fileToUnshare.getContentID());
					}

					for (FileToSync fileToSync : files) {
						FileDocument fileDoc = new FileDocument(new File(fileToSync.getRelFilePath()), MimeMediaType.AOS);
						Content content = new Content(fileToSync.getContentID(), null, fileDoc);
						List<ContentShare> shares = service.shareContent(content);
						DiscoveryService discoService = myPeerGroup.getDiscoveryService();
						for (ContentShare share : shares) {
							//share.addContentShareListener(shareListener);
							ContentShareAdvertisement adv = share.getContentShareAdvertisement();
							try {
								discoService.publish(adv);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}


			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}



	@Override
	protected void parseAdvertisement(
			Enumeration<Advertisement> advertisementsEnum) {
		// TODO Auto-generated method stub

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
				lastShareContentAdvertisment = 0;
				break;
			}

		}
	}





}
