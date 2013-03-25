package com.peersync;

import java.io.IOException;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;

import com.peersync.advertissement.StackAdvertisement;
import com.peersync.data.MyPeerGroup;


public class CommunicationBehaviour extends Thread{

	private MyPeerGroup peerGroup;

	public CommunicationBehaviour(MyPeerGroup peerGroup){
		this.peerGroup = peerGroup;
		// Registering our stack advertisement instance
		AdvertisementFactory.registerAdvertisementInstance(
				StackAdvertisement.getAdvertisementType(),
				new StackAdvertisement.Instantiator());

	}

	public void sendAdvertissement(){

		//peer.myPeerGroup.getPipeService().
		//peer.myDiscoveryService.remotePublish(adv);
	}

	@Override
	public void run() {

		
		try {
			Enumeration<Advertisement> TheAdvEnum;
			TheAdvEnum = peerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, null, null);


			while (TheAdvEnum.hasMoreElements()) { 

				Advertisement TheAdv = TheAdvEnum.nextElement();

				String advertisementClass = TheAdv.getClass().getSimpleName();

				if (advertisementClass.compareTo(StackAdvertisement.class.getName())==0) {

					// We found StackVersion Advertisement
					StackAdvertisement stackVersionAdvertisement = (StackAdvertisement) TheAdv;

					//TODO

					// Flushing advertisement
					//TheDiscoveryService.flushAdvertisement(TheAdv);
				} 


			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
