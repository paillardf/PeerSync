package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.ID;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import com.peersync.models.ShareFolder;
import com.peersync.models.StackVersion;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.tools.Log;


public class StackSyncBehaviour extends AbstractBehaviour{

	private long lastStackVersionAdvertismentEvent=0;

	public StackSyncBehaviour(MyPeerGroup peerGroup){
		super(peerGroup);


	}

	public void publishStackVersionAdvertisement(){
		ArrayList<ShareFolder> shareFolders = new ArrayList<ShareFolder>();
		ShareFolder shareFolder = new ShareFolder("0320230");
		shareFolder.addStackVersion(new StackVersion("100", System.currentTimeMillis()));
		shareFolder.addStackVersion(new StackVersion("101", System.currentTimeMillis()));
		shareFolders.add(shareFolder);
		StackAdvertisement adv = new StackAdvertisement(shareFolders);

		//peer.myPeerGroup.getPipeService().
		try {
			myPeerGroup.getDiscoveryService().publish(adv);
			myPeerGroup.getDiscoveryService().remotePublish(adv);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("StackBehaviour", adv.toString());
		lastStackVersionAdvertismentEvent = System.currentTimeMillis();
	}


	public void run() {
		try {
			while(true){

				sleep(5000);
				//if(myPeerGroup.getRendezVousService().isConnectedToRendezVous()){
				if(System.currentTimeMillis()-lastStackVersionAdvertismentEvent > 30000){
					publishStackVersionAdvertisement();


				}
				Enumeration<Advertisement> TheAdvEnum;
				TheAdvEnum = myPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, null, null);
				parseAdvertisement(TheAdvEnum);
				myPeerGroup.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV,
						null, null, 1, this);

				//}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		parseAdvertisement(event.getSearchResults());

	}

	protected void parseAdvertisement(Enumeration<Advertisement> TheAdvEnum) {
		while (TheAdvEnum.hasMoreElements()) { 

			Advertisement TheAdv = TheAdvEnum.nextElement();
			Log.d("Communication", "new Advertisement found type:" +TheAdv.getAdvType());

			if (TheAdv.getAdvType().compareTo(StackAdvertisement.class.getName())==0) {

				// We found StackVersion Advertisement
				Log.d("Communication", "new StackAvertisement found");
				StackAdvertisement stackVersionAdvertisement = (StackAdvertisement) TheAdv;
				//TODO

				// Flushing advertisement
				//TheDiscoveryService.flushAdvertisement(TheAdv);
			} 


		}


	}

	@Override
	public void notifyNetPeerGroupRDVConnection(ID id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPeerGroupRDVConnection(ID id) {
	//TODO

	}

}