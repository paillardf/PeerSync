package com.peersync.network.behaviour;

import java.io.IOException;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.impl.endpoint.EndpointUtils;
import net.jxta.impl.protocol.RdvAdv;
import net.jxta.impl.rendezvous.RendezVousServiceImpl;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.RouteAdvertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.tools.Log;

public class DiscoveryBehaviour extends AbstractBehaviour{

	private static final long UPDATE_RDV_DELAY = 8*60*1000;
	private static final long VALIDITY_RDV_ADV = 9*60*1000;
	private long lastRDVPublishTime = 0;
	private RdvAdv peerRDVAdv;



	public DiscoveryBehaviour(BasicPeerGroup myPeerGroup) {
		super(myPeerGroup);
	}

	@Override
	protected int action() {

		findRDVAdvertisement();
		//findRDVAdvertisement();

		//		if(peerView!=null){
		//			peerView.seed();
		//		}

		Log.d("IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous(), myPeerGroup.getPeerGroupID().toString());
		Log.d( "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous(), myPeerGroup.getPeerGroupID().toString());
		if(System.currentTimeMillis() - lastRDVPublishTime > UPDATE_RDV_DELAY&&myPeerGroup.getPeerGroup().isRendezvous()){
			sendRDVAdvertisement();
		}



		return 10000;




	}

	private void sendRDVAdvertisement(){
		try {
			if(peerRDVAdv==null){
				PeerAdvertisement padv = myPeerGroup.getPeerGroup().getPeerAdvertisement();
				RdvAdv rdvAdv = (RdvAdv) AdvertisementFactory.newAdvertisement(RdvAdv.getAdvertisementType());
				rdvAdv.setServiceName(((RendezVousServiceImpl)myPeerGroup.getRendezVousService()).getAssignedID().toString() + myPeerGroup.getPeerGroupID().getUniqueValue().toString());
				rdvAdv.setGroupID(padv.getPeerGroupID());
				rdvAdv.setName(padv.getName());
				rdvAdv.setPeerID(padv.getPeerID());

				RouteAdvertisement ra = EndpointUtils.extractRouteAdv(padv);

				rdvAdv.setRouteAdv(ra);
				rdvAdv.sign(myPeerGroup.getPSECredential(), true, true);
				peerRDVAdv = rdvAdv;
			}
			myPeerGroup.getNetPeerGroup().getDiscoveryService().publish(peerRDVAdv,VALIDITY_RDV_ADV,VALIDITY_RDV_ADV);
			myPeerGroup.getNetPeerGroup().getDiscoveryService().remotePublish(peerRDVAdv,VALIDITY_RDV_ADV);
			Log.d(myPeerGroup.getPeerGroupName(), "--- SEND RDV ADVERTISEMENT ---");
			lastRDVPublishTime = System.currentTimeMillis();
		} catch (IOException e1) {
			e1.printStackTrace();
		}					
	}

	private void findRDVAdvertisement() {
		Log.d("Trying to find RDV advertisement...", myPeerGroup.getPeerGroup().getPeerGroupName());
		try {
			secureDiscovery(myPeerGroup.getNetPeerGroup().getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, RdvAdv.GroupIDTag, myPeerGroup.getPeerGroupID().toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		myPeerGroup.getNetPeerGroup().getDiscoveryService().getRemoteAdvertisements( null,
				DiscoveryService.ADV, RdvAdv.GroupIDTag, myPeerGroup.getPeerGroupID().toString(),
				5, this );
	}





	protected void parseAdvertisement(Advertisement foundAdv) {

		if(foundAdv.getAdvType().compareTo(RdvAdv.getAdvertisementType())==0){
			RdvAdv rdvAdv = (RdvAdv) foundAdv;
			if(!rdvAdv.getPeerID().equals(myPeerGroup.getPeerGroup().getPeerID())){
				Log.d("Found RDV Advertisement", myPeerGroup.getPeerGroupID().toString());
				try {
					myPeerGroup.getDiscoveryService().publish(rdvAdv);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}



		}

	}








	@Override
	public void notifyPeerGroup(PeerGroupEvent event) {
		if(myPeerGroup.getNetPeerGroup().getPeerGroupID().toString().equals(event.getPeerGroupID().toString())){
			//NETPEERGROUPEVENT
			switch (event.getID()) {
			case PeerGroupEvent.RDV_CONNECTION:
				lastRDVPublishTime = 0;
				break;
			}
		}else{



		}
	}




}
