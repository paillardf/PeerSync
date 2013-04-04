package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.id.ID;

import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.network.query.StackVersionQuery;
import com.peersync.tools.Log;


public class StackSyncBehaviour extends AbstractBehaviour{

	private static final String NAME = "StackSyncBehaviour";
	private long lastStackVersionAdvertismentEvent=0;
	private StackVersionQuery queryHandler;
	private static final long UPDATE_RDV_DELAY = 8*60*1000;
	private static final long VALIDITY_RDV_ADV = 10*60*1000;
	
	public StackSyncBehaviour(MyPeerGroup peerGroup){
		super(peerGroup);
		queryHandler = new StackVersionQuery(myPeerGroup);

	}

	public void publishStackVersionAdvertisement(){
		ArrayList<SharedFolderVersion> shareFolders = new ArrayList<SharedFolderVersion>();
		SharedFolderVersion shareFolder = new SharedFolderVersion("0320230");
		shareFolder.addStackVersion(new StackVersion(""+System.currentTimeMillis(), System.currentTimeMillis()));
		shareFolder.addStackVersion(new StackVersion("101", System.currentTimeMillis()));
		shareFolders.add(shareFolder);
		StackAdvertisement adv = new StackAdvertisement(shareFolders, myPeerGroup.getPeerGroup().getPeerGroupID());

		//peer.myPeerGroup.getPipeService().
		try {
			myPeerGroup.getDiscoveryService().publish(adv, 30000, 30000);
			myPeerGroup.getDiscoveryService().remotePublish(adv, 30000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(NAME, "Advertisement Send" +adv);
		lastStackVersionAdvertismentEvent = System.currentTimeMillis();
	}


	public void run() {
		try {
			while(true){
				sleep(20000);
				//if(myPeerGroup.getRendezVousService().isConnectedToRendezVous()){
				if(System.currentTimeMillis()-lastStackVersionAdvertismentEvent > 10000){
					publishStackVersionAdvertisement();


				}
				Enumeration<Advertisement> TheAdvEnum;
				TheAdvEnum = myPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, StackAdvertisement.ShareFolderTAG, null);
				parseAdvertisement(TheAdvEnum);
				myPeerGroup.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV,
						null, null,  100, this);

				//}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	protected void parseAdvertisement(Enumeration<Advertisement> TheAdvEnum) {
		while (TheAdvEnum.hasMoreElements()) { 
			
			Advertisement TheAdv = TheAdvEnum.nextElement();
			
			if (TheAdv.getAdvType().compareTo(StackAdvertisement.AdvertisementType)==0) {
				Log.d(NAME, "Adv recu");
				// We found StackVersion Advertisement
				StackAdvertisement stackVersionAdvertisement = (StackAdvertisement) TheAdv;
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
	public void notifyNetPeerGroupRDVConnection(ID id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPeerGroupRDVConnection(ID id) {
		lastStackVersionAdvertismentEvent = 0;
	}

}
