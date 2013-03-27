package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.Enumeration;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.Module;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.RdvAdvertisement;

import com.peersync.network.MyPeerGroup;
import com.peersync.network.PeerManager;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;
import com.peersync.tools.Outils;

public class DiscoveryBehaviour extends AbstractBehaviour{






	private static final long UPDATE_RDV_DELAY = 1*60*1000;
	private long lastRDVPublishTime = 0;




	public DiscoveryBehaviour(MyPeerGroup myPeerGroup) {
		super(myPeerGroup);
	}


	public void run() {

		try {


			while(myPeerGroup.getPeerGroup()==null){
				if(!myPeerGroup.getNetPeerGroup().getRendezVousService().isConnectedToRendezVous())
					sleep(2000);
				findGroupAdvertisement();
				sleep(6000);
				if(myPeerGroup.getPeerGroup()==null){
					Log.d(myPeerGroup.peerGroupName, "Group Not Found");
					myPeerGroup.setPeerGroup(createNewPeerGroup(PeerManager.PsePeerGroupID, PeerManager.PsePeerGroupName, "Groupe securisé"));
					joinGroup(myPeerGroup.getPeerGroup());
				}
			}

			while(true){
				if(!myPeerGroup.getRendezVousService().isConnectedToRendezVous())
					findRDVAdvertisement();
				sleep(10000);

				Log.d(myPeerGroup.peerGroupName, "IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous());
				Log.d(myPeerGroup.peerGroupName, "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous());

				if(System.currentTimeMillis() - lastRDVPublishTime > UPDATE_RDV_DELAY&&myPeerGroup.getPeerGroup().isRendezvous()){

					//					Log.d("ADV", "Send RDV Advertisment");
					//					RdvAdvertisement rdvAdv = (RdvAdvertisement) AdvertisementFactory.newAdvertisement(RdvAdvertisement.getAdvertisementType());
					//					PeerAdvertisement padv = myPeerGroup.getPeerGroup().getPeerAdvertisement();
					//					rdvAdv.setPeerID(padv.getPeerID());
					//					rdvAdv.setGroupID(myPeerGroup.getPeerGroup().getPeerGroupID());
					//					rdvAdv.setServiceName(myPeerGroup.getRendezVousService().getImplAdvertisement().getAdvType());
					//					rdvAdv.setName(padv.getName());
					//					RouteAdvertisement ra = EndpointUtils.extractRouteAdv(padv);
					//					rdvAdv.setRouteAdv(ra);
					//					rdvAdv.set;
					//					try {
					//						myPeerGroup.getNetPeerGroupDiscoveryService().publish(rdvAdv, UPDATE_RDV_DELAY, UPDATE_RDV_DELAY );
					//					} catch (IOException e) {
					//						// TODO Auto-generated catch block
					//						e.printStackTrace();
					//					}



					try {
					//	PeerAdvertisement peerAdv = (PeerAdvertisement )myPeerGroup.getPeerGroup().getPeerAdvertisement();
						RendezVousAdvertisement peerAdv = (RendezVousAdvertisement) AdvertisementFactory.newAdvertisement(RendezVousAdvertisement.getAdvertisementType());
						peerAdv.setPeerId(myPeerGroup.getPeerGroup().getPeerID());
						peerAdv.setPeerGroupId(myPeerGroup.getPeerGroup().getPeerGroupID());
						peerAdv.setStartDate(System.currentTimeMillis());
						//peerAdv.SetID(myPeerGroup.getPeerGroup().getPeerID());
						myPeerGroup.getNetPeerGroupDiscoveryService().publish(peerAdv);
						myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(peerAdv);
						Log.d("ADV", peerAdv.toString());
						lastRDVPublishTime = System.currentTimeMillis();
						Enumeration<Advertisement> advertisementsEnum;
						advertisementsEnum = myPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.PEER, "GID", myPeerGroup.getPeerGroup().getPeerGroupID().toString());


						if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

							while (advertisementsEnum.hasMoreElements()) {
								Advertisement adv = advertisementsEnum.nextElement();
								if(adv.getAdvType().compareTo(RdvAdvertisement.getAdvertisementType())==0){


									//myPeerGroup.getNetPeerGroupDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV, attribute, value, threshold)
									lastRDVPublishTime = System.currentTimeMillis();

									break;
								}
							}
						}

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//					myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(rdvAdv, UPDATE_RDV_DELAY );
					//					Log.d("ADV",rdvAdv.toString());
				}

			}



		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	private void findRDVAdvertisement() {
		Log.d(myPeerGroup.peerGroupName,"Trying to find RDV advertisement...");
		myPeerGroup.getNetPeerGroupDiscoveryService().getRemoteAdvertisements( null,
				DiscoveryService.ADV, 
				RendezVousAdvertisement.PeerGroupIdTAG, 
				myPeerGroup.getPeerGroup().getPeerGroupID().toString(),
				100, this );
		//		try {
		//			parseAdvertisement(myPeerGroup.getDiscoveryService().getLocalAdvertisements
		//					(DiscoveryService.ADV, null, null));
		//			if(myPeerGroup.getRendezVousService().isConnectedToRendezVous()){
		//				
		//				Log.d(myPeerGroup.peerGroupName,"Launching Remote Discovery Service for RDV...");
		//				myPeerGroup.getNetPeerGroupDiscoveryService().getRemoteAdvertisements(null,
		//						DiscoveryService.ADV, null, null, 1, this);
		//			}
		//		} catch (Exception e) {
		//			Log.e(myPeerGroup.peerGroupName, "Error during advertisement search");
		//		}
	}





	private void findGroupAdvertisement() {
		String searchKey = "Name";
		Log.d(myPeerGroup.peerGroupName,"Trying to find advertisement...");

		try {
			parseAdvertisement(myPeerGroup.getNetPeerGroupDiscoveryService().getLocalAdvertisements
					(DiscoveryService.GROUP, searchKey, myPeerGroup.peerGroupName));
			if(myPeerGroup.getPeerGroup()==null){

				Log.d(myPeerGroup.peerGroupName,"Launching Remote Discovery Service...");
				myPeerGroup.getNetPeerGroupDiscoveryService().getRemoteAdvertisements(null,
						DiscoveryService.GROUP, searchKey, myPeerGroup.peerGroupName, 1, this);
			}
		} catch (Exception e) {
			Log.e(myPeerGroup.peerGroupName, "Error during advertisement search");
		}
	}




	private PeerGroup createNewPeerGroup(PeerGroupID myPeerGroupID, String myPeerGroupName, String myPeerGroupDescription) {
		PeerGroup tempPeerGroup = null;

		// Creating a child group with PSE

		try {
			tempPeerGroup = myPeerGroup.getNetPeerGroup().newGroup(
					myPeerGroupID,
					Outils.createAllPurposePeerGroupWithPSEModuleImplAdv(),
					myPeerGroupName,
					myPeerGroupDescription
					);
			if (Module.START_OK != tempPeerGroup.startApp(new String[0]))
				Log.d(myPeerGroup.peerGroupName,"Cannot start custom peergroup");
			else{
				myPeerGroup.getNetPeerGroupDiscoveryService().publish(tempPeerGroup.getPeerGroupAdvertisement());
				myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(tempPeerGroup.getPeerGroupAdvertisement());

				Log.d(myPeerGroup.peerGroupName,"Start custom peergroup");
			}

		} catch (PeerGroupException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}




		return tempPeerGroup;
	}

	void joinGroup(PeerGroup myLocalGroup) {
		// Joining the peer group
		try {

			MembershipService myMembershipService =	myLocalGroup.getMembershipService();

			AuthenticationCredential myAuthenticationCredential =
					new AuthenticationCredential(myPeerGroup.getNetPeerGroup(), "StringAuthentication", null);

			StringAuthenticator MyStringAuthenticator = (StringAuthenticator) myMembershipService.apply(myAuthenticationCredential);

			MyStringAuthenticator.setAuth1_KeyStorePassword(KeyStoreManager.MyKeyStorePassword);
			MyStringAuthenticator.setAuth2Identity(PeerManager.PID_EDGE.toString());
			MyStringAuthenticator.setAuth3_IdentityPassword(KeyStoreManager.MyPrivateKeyPassword);


			Credential MyCredential = null;

			if (!MyStringAuthenticator.isReadyForJoin()) {
				Log.d(myPeerGroup.peerGroupName,"Authenticator is not complete");
				return;
			}
			MyCredential = myMembershipService.join(MyStringAuthenticator);
			Log.d(myPeerGroup.peerGroupName,"Group has been joined");
		} catch (Exception e) {
			Log.d(myPeerGroup.peerGroupName,"Authentication failed - group not joined");
			e.printStackTrace();
			System.exit(-1);
		}
	}




	protected void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum) {

		// PEER GROUP ADVENTISEMENT
		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

			while (advertisementsEnum.hasMoreElements()) {
				Advertisement foundAdv = advertisementsEnum.nextElement();
				Log.d("ADV", "Nouvelle advertisment "+ foundAdv.getAdvType() +"   "+RendezVousAdvertisement.getAdvertisementType());


				if (myPeerGroup.getPeerGroup()==null&&foundAdv.getAdvType().compareTo(PeerGroupAdvertisement.getAdvertisementType())==0) {
					Log.d(myPeerGroup.peerGroupName,"Found GROUP Advertisement");
					try {
						myPeerGroup.setPeerGroup(myPeerGroup.getNetPeerGroup().newGroup(foundAdv));
						joinGroup(myPeerGroup.getPeerGroup());
					} catch (PeerGroupException e) {
						e.printStackTrace();
					}
				}else if(foundAdv.getAdvType().compareTo(RendezVousAdvertisement.getAdvertisementType())==0){
					//EndpointAddress addr = new EndpointAddress("jxta", adv.getPeerID().getUniqueValue().toString(), null, null);
					Log.d(myPeerGroup.peerGroupName,"Found RDV Advertisement");
					RendezVousAdvertisement rdvAdv = (RendezVousAdvertisement) foundAdv;
					try {
						myPeerGroup.getRendezVousService().connectToRendezVous(rdvAdv.getRendezVousAddress());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//myPeerGroup.getGroupManager().getConfig().addRelaySeedingURI(rdvAdv.getRouteAdv().getDestPeerID().toURI());
				}
			}

		}

	}
}
