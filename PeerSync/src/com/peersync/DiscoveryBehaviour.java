package com.peersync;

import java.util.Enumeration;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.Module;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.data.MyPeerGroup;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;
import com.peersync.tools.Outils;

public class DiscoveryBehaviour extends Thread implements DiscoveryListener{


	//public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + "config"+System.getProperty("file.separator")+"jxta.conf");

	private MyPeerGroup myPeerGroup;

	public DiscoveryBehaviour(MyPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;


	}



	public void run() {

		try {


			while(myPeerGroup.getPeerGroup()==null){
				findGroupAdvertisement();
				sleep(6000);
				if(myPeerGroup.getPeerGroup()==null){
					Log.d(myPeerGroup.peerGroupName, "Group Not Found");
					myPeerGroup.setPeerGroup(createNewPeerGroup(PeerManager.PsePeerGroupID, PeerManager.PsePeerGroupName, "Groupe securisé"));
					joinGroup(myPeerGroup.getPeerGroup());
				}
			}

			while(true){
				sleep(10000);

				Log.d(myPeerGroup.peerGroupName, "IS RENDEZ VOUS "+ myPeerGroup.getPeerGroup().isRendezvous());
				Log.d(myPeerGroup.peerGroupName, "IS CONNECT TO RENDEZ VOUS "+ myPeerGroup.getPeerGroup().getRendezVousService().isConnectedToRendezVous());
				
				if(!myPeerGroup.getRendezVousService().isConnectedToRendezVous()){
					findRDVAdvertisement();
				}
				
			}

			

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	private void findRDVAdvertisement() {
		String searchKey = "Name";
		Log.d(myPeerGroup.peerGroupName,"Trying to find RDV advertisement...");

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
			else
				Log.d(myPeerGroup.peerGroupName,"Start custom peergroup");
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



	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		parseAdvertisement(event.getSearchResults());

	}
	
	private void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum) {

		// PEER GROUP ADVENTISEMENT
		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {
			
			PeerGroupAdvertisement myFoundPGA = null;
			while (advertisementsEnum.hasMoreElements()) {
				myFoundPGA = (PeerGroupAdvertisement) advertisementsEnum.nextElement();
				if (myPeerGroup.getPeerGroup()==null&&myFoundPGA.getName().equals(myPeerGroup.peerGroupName)) {
					Log.d(myPeerGroup.peerGroupName,"Found GROUP Advertisement");
					Log.d(myPeerGroup.peerGroupName,"Creating new group variable...");
					try {
						myPeerGroup.setPeerGroup(myPeerGroup.getNetPeerGroup().newGroup(myFoundPGA));
						joinGroup(myPeerGroup.getPeerGroup());
					} catch (PeerGroupException e) {
						e.printStackTrace();
					}
				}
			}

		}

	}
}
