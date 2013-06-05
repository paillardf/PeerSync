package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.data.SyncUtils;
import com.peersync.models.PeerGroupEvent;
import com.peersync.models.SharedFolder;
import com.peersync.models.SharedFolderVersion;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.query.StackVersionQuery;
import com.peersync.tools.Log;


public class StackSyncBehaviour extends AbstractBehaviour{

	private static final String NAME = "StackSyncBehaviour";
	private long lastStackVersionAdvertismentEvent=0;
	private StackVersionQuery queryHandler;
	private static final long VALIDITY_STACKVERSION_ADV = 2*60*1000;
	private static final long PUBLISH_ADVERTISEMENT_DELAY = VALIDITY_STACKVERSION_ADV-10*1000;


	public StackSyncBehaviour(BasicPeerGroup peerGroup){
		super(peerGroup);
		queryHandler = new StackVersionQuery(myPeerGroup);
	}
	@Override
	public void initialize() {
		myPeerGroup.getPeerGroup().getResolverService().registerHandler(StackVersionQuery.NAME, queryHandler);
	}

	public void publishStackVersionAdvertisement(){
		DataBaseManager db = DataBaseManager.getInstance();

		ArrayList<SharedFolder> shareFolderUIDs = db.getSharedFolders(myPeerGroup.getPeerGroupID().toString());
		ArrayList<SharedFolderVersion> shareFolders = new ArrayList<SharedFolderVersion>();
		for (SharedFolder shareFolder : shareFolderUIDs) {
			shareFolders.add(db.getSharedFolderVersion(shareFolder.getUID()));
		}
		StackAdvertisement adv = 
				new StackAdvertisement(shareFolders, myPeerGroup.getPeerGroup().getPeerGroupID(), myPeerGroup.getPeerGroup().getPeerID());

		//peer.myPeerGroup.getPipeService().
		try {
			myPeerGroup.getDiscoveryService().publish(adv, VALIDITY_STACKVERSION_ADV, VALIDITY_STACKVERSION_ADV);
			myPeerGroup.getDiscoveryService().remotePublish(adv, VALIDITY_STACKVERSION_ADV);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(NAME, "Advertisement Send" +adv);
		lastStackVersionAdvertismentEvent = System.currentTimeMillis();
	}

	@Override
	protected int action() {
		if(System.currentTimeMillis()-lastStackVersionAdvertismentEvent > PUBLISH_ADVERTISEMENT_DELAY){
			publishStackVersionAdvertisement();


		}
		Enumeration<Advertisement> TheAdvEnum;
		try {
			TheAdvEnum = myPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, StackAdvertisement.ShareFolderTAG, null);

			parseAdvertisement(TheAdvEnum);
			myPeerGroup.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV,
					StackAdvertisement.ShareFolderTAG, null,  100, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 20000;
	}








	protected void parseAdvertisement(Enumeration<Advertisement> TheAdvEnum) {
		while (TheAdvEnum.hasMoreElements()) { 

			Advertisement TheAdv = TheAdvEnum.nextElement();

			if (TheAdv.getAdvType().compareTo(StackAdvertisement.AdvertisementType)==0) {

				// We found StackVersion Advertisement
				StackAdvertisement stackVersionAdvertisement = (StackAdvertisement) TheAdv;
				if(stackVersionAdvertisement.getPeerId().compareTo(myPeerGroup.getPeerGroup().getPeerID().toString())!=0){

					Log.d(NAME, "Adv recu" + stackVersionAdvertisement);

					ArrayList<SharedFolderVersion> shareFolderVersion = 
							SyncUtils.compareShareFolderVersion(stackVersionAdvertisement.getShareFolderList());
					if(shareFolderVersion.size()>0){
						//ENVOYER UNE REQUETE
						queryHandler.sendQuery(shareFolderVersion, stackVersionAdvertisement.getPeerId());
					}
				}
				//if(stackVersionAdvertisement.getPeerId().compareTo(myPeerGroup.getPeerGroup().getPeerID().toString())!=0){
				//queryHandler.sendQuery(stackVersionAdvertisement.getShareFolderList(), stackVersionAdvertisement.getPeerId());
				//System.out.println(stackVersionAdvertisement.toString());
				// Flushing advertisement
				//TheDiscoveryService.flushAdvertisement(TheAdv);
				//}
			} 


		}


	}


	@Override
	public void notifyPeerGroup(PeerGroupEvent event) {
		if(myPeerGroup.getNetPeerGroup().getPeerGroupID().toString().equals(event.getPeerGroupID().toString())){
			//NETPEERGROUPEVENT
		}else{
			//PEERGROUPEVENT
			nextExecutionTime=0;
			switch (event.getID()) {
			case PeerGroupEvent.RDV_CONNECTION:
				lastStackVersionAdvertismentEvent = 0;
				
				break;
			case PeerGroupEvent.STACK_UPDATE:
				lastStackVersionAdvertismentEvent = 0;
				break;
			}

		}
	}



}
