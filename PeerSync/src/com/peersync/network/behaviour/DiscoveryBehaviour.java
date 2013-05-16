package com.peersync.network.behaviour;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.impl.protocol.RdvAdv;
import net.jxta.impl.protocol.RouteAdv;
import net.jxta.impl.rendezvous.RendezVousServiceImpl;
import net.jxta.impl.rendezvous.rpv.PeerView;
import net.jxta.peer.PeerID;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.protocol.RouteAdvertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.tools.Log;

public class DiscoveryBehaviour extends AbstractBehaviour{

	private static final long UPDATE_RDV_DELAY = 8*60*1000;
	private static final long VALIDITY_RDV_ADV = 10*60*1000;
	private long lastRDVPublishTime = 0;
	private RendezVousAdvertisement peerRDVAdv;



	public DiscoveryBehaviour(BasicPeerGroup myPeerGroup) {
		super(myPeerGroup);
	}

	@Override
	protected int action() {

		//findRDVAdvertisement();
		PeerView peerView = ((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView();
//		if(peerView!=null){
//			peerView.seed();
//		}
		
		Log.d(myPeerGroup.getPeerGroupName(), "IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous());
		Log.d(myPeerGroup.getPeerGroupName(), "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous());
		if(System.currentTimeMillis() - lastRDVPublishTime > UPDATE_RDV_DELAY&&myPeerGroup.getPeerGroup().isRendezvous()){
		//	sendRDVAdvertisement();
		}
		return 10000;
		

	}

	private void sendRDVAdvertisement(){
		try {
			if(peerRDVAdv==null){
				peerRDVAdv = (RendezVousAdvertisement) AdvertisementFactory.newAdvertisement(RendezVousAdvertisement.getAdvertisementType());
				peerRDVAdv.setPeerID(myPeerGroup.getPeerGroup().getPeerID());
				peerRDVAdv.setPeerGroupId(myPeerGroup.getPeerGroup().getPeerGroupID());
				peerRDVAdv.setStartDate(System.currentTimeMillis());
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
		Log.d(myPeerGroup.getPeerGroup().getPeerGroupName(),"Trying to find RDV advertisement...");

//		try {
//			parseAdvertisement(myPeerGroup.getPeerGroup().getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, null, null));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// advs = discovery.getLocalAdvertisements(DiscoveryService.ADV, RdvAdvertisement.ServiceNameTag, serviceName);
		myPeerGroup.getPeerGroup().getDiscoveryService().getRemoteAdvertisements( null,
				DiscoveryService.ADV, null, null,
				//RendezVousAdvertisement.PeerGroupIdTAG, 
				//myPeerGroup.getPeerGroup().getPeerGroupID().toString(),
				100, this );
	}




	protected void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum) {

		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

			while (advertisementsEnum.hasMoreElements()) {
				Advertisement foundAdv = advertisementsEnum.nextElement();
//				if(foundAdv instanceof RdvAdvertisement){
//					RdvAdvertisement a =(RdvAdvertisement) foundAdv;
//					try {
//					NetworkConfigurator conf = PeerSync.getInstance().getConf();
//					String s = a.getRouteAdv().getDest().getEndpointAddresses().nextElement().toString();
//					
//					conf.addRdvSeedingURI(s);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				
//				if(foundAdv.getAdvType().compareTo(RendezVousAdvertisement.getAdvertisementType())==0){
//
//
//					Log.d(myPeerGroup.getPeerGroupName(),"Found RDV Advertisement");
//					RendezVousAdvertisement rdvAdv = (RendezVousAdvertisement) foundAdv;
//					
//					if(!rdvAdv.getPeerID().equals(myPeerGroup.getPeerGroup().getPeerID())){
//
//						if(myPeerGroup.getPeerGroup().getRendezVousService().isRendezVous()){
//
//
//							List<PeerID> listRDV = myPeerGroup.getPeerGroup().getRendezVousService().getLocalRendezVousView();
//							for (PeerID peerID : listRDV) {
//								if(peerID.equals(rdvAdv.getPeerID())){
//
//								}
//							}
//						}else{
//							
//							//List<EndpointAddress> listAddress = ((RouteAdv)rdvAdv.getRouteAdv()).getDestEndpointAddresses();
//							//for (EndpointAddress endpointAddress : listAddress) {
//								myPeerGroup.getConf().addRdvSeedingURI(rdvAdv.getPeerID().toURI());
//								myPeerGroup.getConf().addRdvSeedingURI(rdvAdv.getRendezVousAddress().toURI());
//								//((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView().addSeed(endpointAddress.toURI());
//							//}
//							
//
//						}
//					}	
						
				
				if(foundAdv.getAdvType().compareTo(RdvAdv.getAdvertisementType())==0){


					Log.d(myPeerGroup.getPeerGroupName(),"Found RDV Advertisement");
					RdvAdv rdvAdv = (RdvAdv) foundAdv;
					
					if(!rdvAdv.getPeerID().equals(myPeerGroup.getPeerGroup().getPeerID())){

						if(myPeerGroup.getPeerGroup().getRendezVousService().isRendezVous()){


							List<PeerID> listRDV = myPeerGroup.getPeerGroup().getRendezVousService().getLocalRendezVousView();
							for (PeerID peerID : listRDV) {
								if(peerID.equals(rdvAdv.getPeerID())){
//									if(peerRDVAdv==null||rdvAdv.getStartDate()<peerRDVAdv.getStartDate()){
//										myPeerGroup.getRendezVousService().stopRendezVous();
//										peerRDVAdv = null;
//										break;
//
//									}else{
//										//((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView(). probeAddress(rdvAdv.getRendezVousAddress(), null);
//										//URI t = rdvAdv.getRendezVousAddress().toURI();
//										//myPeerGroup.getConf().addRdvSeedingURI(t);
//										//TODO
//										//myPeerGroup.getPeerGroup().getEndpointService().getEndpointRouter().suggestRoute(route);
//										//myPeerGroup.getRendezVousService().disconnectFromRendezVous(rdvAdv.getPeerId());
//									}
								}
							}
						}else{
							
							List<EndpointAddress> listAddress = ((RouteAdv)rdvAdv.getRouteAdv()).getDestEndpointAddresses();
							for (EndpointAddress endpointAddress : listAddress) {
								myPeerGroup.getConf().addRdvSeedingURI(endpointAddress.toURI());
								//((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView().addSeed(endpointAddress.toURI());
							}
							
							
//							RouteAdvertisement r = (RouteAdvertisement) AdvertisementFactory.newAdvertisement(RouteAdvertisement.getAdvertisementType());
//							r.addDestEndpointAddress(rdvAdv.getRendezVousAddress());
//							URI r = rdvAdv.getRendezVousAddress().toURI();
//							((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView().addSeed(r);

//							String t = rdvAdv.getRouteAdv().getFirstHop().toString();//getRendezVousAddress().toURI();
//							myPeerGroup.getConf().addRdvSeedingURI(t);
//							
//							RouteAdvertisement r = (RouteAdvertisement) AdvertisementFactory.newAdvertisement(RouteAdvertisement.getAdvertisementType());
//							r.addDestEndpointAddress(rdvAdv.getRendezVousAddress());
//							myPeerGroup.getPeerGroup().getEndpointService().getEndpointRouter().suggestRoute(r);
							//myPeerGroup.getPeerGroup().getMembershipService()..EndpointService().
//							try {
//								
	//					myPeerGroup.getRendezVousService().getClass()connectToRendezVous(rdvAdv.getRendezVousAddress());
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
						}
					}
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
