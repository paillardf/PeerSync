package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.Enumeration;

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

		Log.d(myPeerGroup.getPeerGroupName(), "IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous());
		Log.d(myPeerGroup.getPeerGroupName(), "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous());
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
				peerRDVAdv = rdvAdv;
				//				peerRDVAdv = (RendezVousAdvertisement) AdvertisementFactory.newAdvertisement(RendezVousAdvertisement.getAdvertisementType());
				//				peerRDVAdv.setPeerID(myPeerGroup.getPeerGroup().getPeerID());
				//				peerRDVAdv.setPeerGroupId(myPeerGroup.getPeerGroup().getPeerGroupID());
				//				peerRDVAdv.setStartDate(System.currentTimeMillis());
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
		try {
			parseAdvertisement(myPeerGroup.getNetPeerGroup().getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, RdvAdv.GroupIDTag, myPeerGroup.getPeerGroupID().toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// advs = discovery.getLocalAdvertisements(DiscoveryService.ADV, RdvAdvertisement.ServiceNameTag, serviceName);
		myPeerGroup.getNetPeerGroup().getDiscoveryService().getRemoteAdvertisements( null,
				DiscoveryService.ADV, RdvAdv.GroupIDTag, myPeerGroup.getPeerGroupID().toString(),
				//RendezVousAdvertisement.PeerGroupIdTAG, 
				//myPeerGroup.getPeerGroup().getPeerGroupID().toString(),
				5, this );
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
					RdvAdv rdvAdv = (RdvAdv) foundAdv;
					if(!rdvAdv.getPeerID().equals(myPeerGroup.getPeerGroup().getPeerID())){
						Log.d(myPeerGroup.getPeerGroupName(),"Found RDV Advertisement");
						//if(myPeerGroup.getPeerGroup().getRendezVousService().isRendezVous()){


						//							List<PeerID> listRDV = myPeerGroup.getPeerGroup().getRendezVousService().getLocalRendezVousView();
						//							for (PeerID peerID : listRDV) {
						//								if(peerID.equals(rdvAdv.getPeerID())){
						//									if(peerRDVAdv==null||rdvAdv.getStartDate()<peerRDVAdv.getStartDate()){
						//										myPeerGroup.getRendezVousService().stopRendezVous();
						//										peerRDVAdv = null;
						//										break;
						//
						//									}else{
						//										//((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView(). probeAddress(rdvAdv.getRendezVousAddress(), null);
						//										//URI t = rdvAdv.getRendezVousAddress().toURI();
						//										//myPeerGroup.getConf().addRdvSeedingURI(t);
						//										//myPeerGroup.getPeerGroup().getEndpointService().getEndpointRouter().suggestRoute(route);
						//										//myPeerGroup.getRendezVousService().disconnectFromRendezVous(rdvAdv.getPeerId());
						//									}
						//								}
						//							}
						//						}else{
//						PeerView peerView = ((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView();
//						if(peerView !=null){
//							List<EndpointAddress> listAddress = ((RouteAdv)rdvAdv.getRouteAdv()).getDestEndpointAddresses();
//							for (EndpointAddress endpointAddress : listAddress) {
//								peerView.addSeed(endpointAddress.toURI());
//								//((RendezVousServiceImpl) myPeerGroup.getRendezVousService()).getPeerView().addSeed(endpointAddress.toURI());
//							}
//						}
						try {
							myPeerGroup.getDiscoveryService().publish(rdvAdv);
						} catch (IOException e) {
							e.printStackTrace();
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
