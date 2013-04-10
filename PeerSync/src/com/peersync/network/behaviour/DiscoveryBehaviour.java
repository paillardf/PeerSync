package com.peersync.network.behaviour;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.Module;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.PeerManager;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;
import com.peersync.tools.Outils;

public class DiscoveryBehaviour extends AbstractBehaviour{

	private static final long UPDATE_RDV_DELAY = 8*60*1000;
	private static final long VALIDITY_RDV_ADV = 10*60*1000;
	private long lastRDVPublishTime = 0;
	private RendezVousAdvertisement peerRDVAdv;




	public DiscoveryBehaviour(MyPeerGroup myPeerGroup) {
		super(myPeerGroup);
	}


	public void run() {

		try {


			int essai=0;
			while(myPeerGroup.getPeerGroup()==null&&essai<5){
				essai++;
				findGroupAdvertisement();
				sleep(6000);


				if(!myPeerGroup.getNetPeerGroup().getRendezVousService().isConnectedToRendezVous())
					sleep(2000);
			}

			if(myPeerGroup.getPeerGroup()==null){
				Log.d(myPeerGroup.peerGroupName, "GroupAdv Not Found");
				joinGroup(createNewPeerGroup(myPeerGroup.getPeerGroupID(),myPeerGroup.getPeerGroupName(), "Groupe securisé"));

			}
			if(myPeerGroup.getPeerGroup()!=null){

				while(true){
					//if(!myPeerGroup.getRendezVousService().isConnectedToRendezVous())
					findRDVAdvertisement();
					sleep(10000);

					Log.d(myPeerGroup.peerGroupName, "IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous());
					Log.d(myPeerGroup.peerGroupName, "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous());

					if(System.currentTimeMillis() - lastRDVPublishTime > UPDATE_RDV_DELAY&&myPeerGroup.getPeerGroup().isRendezvous()){
						sendRDVAdvertisement();
					}

				}
			}


		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void sendRDVAdvertisement(){
		try {
			if(peerRDVAdv==null){
				peerRDVAdv = (RendezVousAdvertisement) AdvertisementFactory.newAdvertisement(RendezVousAdvertisement.getAdvertisementType());
				peerRDVAdv.setPeerId(myPeerGroup.getPeerGroup().getPeerID());
				peerRDVAdv.setPeerGroupId(myPeerGroup.getPeerGroup().getPeerGroupID());
				peerRDVAdv.setStartDate(System.currentTimeMillis());
			}


			myPeerGroup.getNetPeerGroupDiscoveryService().publish(peerRDVAdv,VALIDITY_RDV_ADV,VALIDITY_RDV_ADV);
			myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(peerRDVAdv,VALIDITY_RDV_ADV);
			Log.d(myPeerGroup.peerGroupName, "--- SEND RDV ADVERTISEMENT ---");
			lastRDVPublishTime = System.currentTimeMillis();
		} catch (IOException e1) {
			e1.printStackTrace();
		}					
	}

	private void findRDVAdvertisement() {
		Log.d(myPeerGroup.peerGroupName,"Trying to find RDV advertisement...");


		myPeerGroup.getNetPeerGroupDiscoveryService().getRemoteAdvertisements( null,
				DiscoveryService.ADV, 
				RendezVousAdvertisement.PeerGroupIdTAG, 
				myPeerGroup.getPeerGroup().getPeerGroupID().toString(),
				100, this );
	}

	private void findGroupAdvertisement() {
		String searchKey = "Name";
		Log.d(myPeerGroup.peerGroupName,"Trying to find Group avertisement...");

		try {
			parseAdvertisement(myPeerGroup.getNetPeerGroupDiscoveryService().getLocalAdvertisements
					(DiscoveryService.GROUP, searchKey, myPeerGroup.peerGroupName));
			if(myPeerGroup.getPeerGroup()==null){

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
			}
			MyCredential = myMembershipService.join(MyStringAuthenticator);
			Log.d(myPeerGroup.peerGroupName,"Group has been joined");
			myPeerGroup.setPeerGroup(myLocalGroup);
		} catch (Exception e) {
			Log.d(myPeerGroup.peerGroupName,"Authentication failed - group not joined");
			e.printStackTrace();
		}
	}




	protected void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum) {

		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

			while (advertisementsEnum.hasMoreElements()) {
				Advertisement foundAdv = advertisementsEnum.nextElement();
				

				if (myPeerGroup.getPeerGroup()==null&&foundAdv.getAdvType().compareTo(PeerGroupAdvertisement.getAdvertisementType())==0) {
					Log.d(myPeerGroup.peerGroupName,"Found GROUP Advertisement");
					try {
						joinGroup(myPeerGroup.getNetPeerGroup().newGroup(foundAdv));
					} catch (PeerGroupException e) {
						e.printStackTrace();
					}
				}else if(foundAdv.getAdvType().compareTo(RendezVousAdvertisement.getAdvertisementType())==0){


					Log.d(myPeerGroup.peerGroupName,"Found RDV Advertisement");
					RendezVousAdvertisement rdvAdv = (RendezVousAdvertisement) foundAdv;
					if(!rdvAdv.getPeerId().equals(myPeerGroup.getPeerGroup().getPeerID())){

						if(myPeerGroup.getPeerGroup().getRendezVousService().isRendezVous()){


							List<PeerID> listRDV = myPeerGroup.getPeerGroup().getRendezVousService().getLocalRendezVousView();
							for (PeerID peerID : listRDV) {
								if(peerID.equals(rdvAdv.getPeerId())){
									if(rdvAdv.getStartDate()<peerRDVAdv.getStartDate()){
										myPeerGroup.getRendezVousService().stopRendezVous();
										peerRDVAdv = null;
										break;

									}else{
										myPeerGroup.getRendezVousService().disconnectFromRendezVous(rdvAdv.getPeerId());
									}
								}
							}

						}else{

							try {
								myPeerGroup.getRendezVousService().connectToRendezVous(rdvAdv.getRendezVousAddress());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

		}

	}


//	@Override
//	public void notifyNetPeerGroup(PeerInfoEvent event) {
//		lastRDVPublishTime = 0;
//
//	}


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


	



	//	if(myPeerGroup.getRendezVousService().isRendezVous()){
	//		try {
	//			Enumeration<Advertisement> enumAdv = myPeerGroup.getPeerGroupDiscoveryService().getLocalAdvertisements
	//					(DiscoveryService.ADV, RendezVousAdvertisement.PeerGroupIdTAG, myPeerGroup.getPeerGroup().getPeerGroupID().toString());
	//
	//
	//			while (enumAdv.hasMoreElements()) {
	//				Advertisement foundAdv = enumAdv.nextElement();
	//				if(foundAdv.getAdvType().compareTo(RendezVousAdvertisement.getAdvertisementType())==0){
	//					
	//					
	//					RendezVousAdvertisement rdvAdv = (RendezVousAdvertisement) foundAdv;
	//					System.out.println(rdvAdv.getPeerId().toURI()+ "  "+id.toURI());
	//					
	//					if(rdvAdv.getPeerId().toURI().compareTo(id.toURI())==0&&rdvAdv.getStartDate()<peerRDVAdv.getStartDate()){
	//						System.out.println("STOP SERVICE");
	//						
	//						myPeerGroup.getRendezVousService().stopRendezVous();
	//						peerRDVAdv = null;
	//						break;
	//					}
	//				}
	//			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}
}
