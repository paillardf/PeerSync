package com.peersync.network.behaviour;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.group.GroupUtils;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;

public class DiscoveryBehaviour extends AbstractBehaviour{

	private static final long UPDATE_RDV_DELAY = 8*60*1000;
	private static final long VALIDITY_RDV_ADV = 10*60*1000;
	private long lastRDVPublishTime = 0;
	private RendezVousAdvertisement peerRDVAdv;
	private static final int NB_ESSAIS_GROUP = 2;



	public DiscoveryBehaviour(MyPeerGroup myPeerGroup) {
		super(myPeerGroup);
	}


	public void run() {

		try {


			int essai=0;
			while(myPeerGroup.getPeerGroup()==null&&essai<NB_ESSAIS_GROUP){
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
		try{
			// Build the Module Impl Advertisemet we will use for our group.
			ModuleImplAdvertisement pseImpl =  GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv();// build_psegroup_impl_adv(npg);

			// Publish the Module Impl Advertisement to the group where the
			// peergroup will be advertised. This should be done in every peer
			// group in which the Peer Group is also advertised.
			// We use the same expiration and lifetime that the Peer Group Adv
			// will use (the default).
			DiscoveryService disco = myPeerGroup.getNetPeerGroup().getDiscoveryService();
			disco.publish(pseImpl, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);

			PeerGroupAdvertisement pse_pga = null;
			KeyStoreManager ksm = KeyStoreManager.getInstance();
			EncryptedPrivateKeyInfo encyptedKey = ksm.getEncryptedPrivateKey(myPeerGroupID.toString(), Constants.PeerGroupKey.toCharArray());


			X509Certificate[] invitationCertChain = {ksm.getX509Certificate(myPeerGroupID.toString())};

			//			PrivateKey newPriv = ksm.KEY.issuerPkey;
			//			EncryptedPrivateKeyInfo encypted = null;
			//			encypted = PSEUtils.pkcs5_Encrypt_pbePrivateKey(Constants.PeerGroupKey.toCharArray(), newPriv, 500);



			pse_pga = GroupUtils.build_psegroup_adv(pseImpl, myPeerGroup.peerGroupName, myPeerGroupID , invitationCertChain, encyptedKey);
			disco.publish(pse_pga, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);
			disco.remotePublish(pse_pga);


			tempPeerGroup  = initPeerGroup(pse_pga);


			//        // Creating a child group with PSE
			//
			//		try {
			////			NetworkManager networkManager = PeerManager.getInstance().networkManager;
			////			NetworkConfigurator MyConfigParams = networkManager.getConfigurator();
			////			// Retrieving the PSE configuration advertisement 
			////			PSEConfigAdv MyPSEConfigAdv = (PSEConfigAdv) 
			////
			////			MyConfigParams.getSvcConfigAdvertisement(PeerGroup.membershipClassID); 
			////
			////			// Retrieving the X.509 certificate 
			////			MyX509Certificate = MyPSEConfigAdv.getCertificate(); 
			////
			////			System.out.println(MyX509Certificate.toString()); 
			////			
			////			
			//			KeyStoreManager ksm = KeyStoreManager.getInstance();
			//			
			//			//PSEMembershipService pseM = (PSEMembershipService) myPeerGroup.getNetPeerGroup().getMembershipService();
			//			//pseM.getPSEConfig().
			//			
			//			X509Certificate[] ksmList = {ksm.getX509Certificate(myPeerGroupID.toString())};
			////			
			//			PeerGroupAdvertisement groupAdv = Outils.build_psegroup_adv(myPeerGroupID,myPeerGroup.peerGroupName, ksmList, ksm.getPrivateKey(), KeyStoreManager.MyPrivateKeyPassword);
			//			myPeerGroup.getNetPeerGroupDiscoveryService().publish(groupAdv);
			//			myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(groupAdv);
			//			
			//			tempPeerGroup = myPeerGroup.getNetPeerGroup().newGroup(groupAdv);
			//			//PeerGroup
			////					myPeerGroupID,
			////					Outils.createAllPurposePeerGroupWithPSEModuleImplAdv(myPeerGroup.getNetPeerGroup()),
			////					myPeerGroupName,
			////					myPeerGroupDescriptio
			////					,true);
			////			if (Module.START_OK != tempPeerGroup.startApp(new String[0]))
			////				Log.d(myPeerGroup.peerGroupName,"Cannot start custom peergroup");
			////			else{
			////				myPeerGroup.getNetPeerGroupDiscoveryService().publish(tempPeerGroup.getPeerGroupAdvertisement());
			////				myPeerGroup.getNetPeerGroupDiscoveryService().remotePublish(tempPeerGroup.getPeerGroupAdvertisement());
			////
			////				Log.d(myPeerGroup.peerGroupName,"Start custom peergroup");
			////			}
			//
			//		} catch (Exception e) {
			//			e.printStackTrace();
			//		}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return tempPeerGroup;
	}
	private PeerGroup initPeerGroup(PeerGroupAdvertisement adv) {



		//		Element pi = adv.getServiceParam(PeerGroup.membershipClassID);
		//		Advertisement pa = null;
		//		pa = AdvertisementFactory.newAdvertisement((XMLElement) pi);
		//
		//
		//		PSEConfigAdv pse = (PSEConfigAdv) pa;
		//		X509Certificate cert = pse.getCertificate();
		//		EncryptedPrivateKeyInfo key = pse.getEncryptedPrivateKey();
		//		PrivateKey privateKey = PSEUtils.pkcs5_Decrypt_pbePrivateKey(myPeerGroup.getPeerGroupKey(), pse.getEncryptedPrivateKeyAlgo(), key);
		//
		//		// Merge service params with those specified by the group (if any). The only
		//		// policy, right now, is to give peer params the precedence over group params.
		//		StructuredDocument membershipParam = adv.getServiceParam(PeerGroup.membershipClassID);
		//        Enumeration keys = grpParams.keys();
		//
		//        while (keys.hasMoreElements()) {
		//            ID key = (ID) keys.nextElement();
		//            Element e = (Element) grpParams.get(key);
		//
		//            if (configAdvertisement.getServiceParam(key) == null) {
		//                configAdvertisement.putServiceParam(key, e);
		//            }
		//        }



		PeerGroup peerGroup = null;
		try {
			peerGroup = myPeerGroup.getNetPeerGroup().newGroup(adv);
			PSEMembershipService memberShip = (PSEMembershipService) peerGroup.getMembershipService();
			memberShip.init(peerGroup, memberShip.getAssignedID(), memberShip.getImplAdvertisement());

		} catch (PeerGroupException e) {
			e.printStackTrace();
			return null;
		}

		//		ConfigParams configAdv = myPeerGroup.getNetPeerGroup().getConfigAdvertisement();
		//		configAdv = configAdv.clone();
		//		// Get our peer-defined parameters in the configAdv
		//		configAdv.removeServiceParam(PeerGroup.membershipClassID);
		//
		//
		//		configAdv.putServiceParam(PeerGroup.membershipClassID, membershipParam);
		//

		//		PSEMembershipService membership = (PSEMembershipService) peerGroup.getMembershipService();
		//		membership.getPSEConfig().setKeyStorePassword(KeyStoreManager.MyKeyStorePassword.toCharArray());
		//		
		//		
		//		PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement((XMLElement) adv.getServiceParam(PeerGroup.membershipClassID));
		//		
		//		pseConf.getEncryptedPrivateKey();
		//		
		//		PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());
		//
		//		pseConf.setCertificateChain(invitationCertChain);
		//		pseConf.setEncryptedPrivateKey(invitationPrivateKey, invitationCertChain[0].getPublicKey().getAlgorithm());
		//
		//		XMLDocument pseDoc = (XMLDocument) pseConf.getDocument(MimeMediaType.XMLUTF8);
		//
		//		newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);
		//		
		//		membership.getPSEConfig().setKey(adv., certchain, key, key_password)
		//		adv.ge
		//GenericPeerGroup.setGroupConfigAdvertisement(adv.getPeerGroupID(), configAdv);


		return peerGroup;
	}

	private boolean joinGroup(PeerGroup myLocalGroup) {
		// Joining the peer group
		//	try {



		PSEMembershipService membership =	(PSEMembershipService)myLocalGroup.getMembershipService();
		membership.getPSEConfig().setKeyStorePassword(KeyStoreManager.MyKeyStorePassword.toCharArray());
		StringAuthenticator memberAuthenticator;

		try {
			AuthenticationCredential application = new AuthenticationCredential(myLocalGroup, "StringAuthentication", null);

			memberAuthenticator = (StringAuthenticator) membership.apply(application);
			memberAuthenticator.setAuth1_KeyStorePassword(KeyStoreManager.MyKeyStorePassword);
			memberAuthenticator.setAuth2Identity(myLocalGroup.getPeerGroupID());
			memberAuthenticator.setAuth3_IdentityPassword(myPeerGroup.getPeerGroupKey());
			if (!memberAuthenticator.isReadyForJoin()) {
				Log.d(myPeerGroup.peerGroupName,"Authenticator is not complete");
			}
			Credential MyCredential = membership.join(memberAuthenticator);
			Log.d(myPeerGroup.peerGroupName,"Group has been joined");
			myPeerGroup.setPeerGroup(myLocalGroup);

		} catch (ProtocolNotSupportedException | PeerGroupException noAuthenticator) {
			throw new UndeclaredThrowableException(noAuthenticator, "String authenticator not available!");
		}

		/*X509Certificate invitationCert = invitationAuthenticator.getCertificate(new char[0], myLocalGroup.getPeerID());

					String subjectName = PSEUtils.getCertSubjectCName(invitationCert);

		            String issuerName = PSEUtils.getCertIssuerCName(invitationCert);
		 */

		//		if(memberAuthenticator.isReadyForJoin()){
		//			System.out.println("I m the winner");
		//			try {
		//				membership.join(memberAuthenticator);
		//			} catch (PeerGroupException e) {
		//				e.printStackTrace();
		//			}
		//		}
		//invitationConfirmButton.setEnabled(invitationAuthenticator.isReadyForJoin());


		//			}
		return false;







		//
		//
		//			KeyStoreManager ksm = KeyStoreManager.getInstance();
		//			Certificate[] certs = {ksm.getX509Certificate()};
		//
		//			//	myMembershipService.getPSEConfig().setKeyStorePassword(ksm.MyKeyStorePassword.toCharArray());
		//			//	myMembershipService.getPSEConfig().setKey(PeerManager.PID_EDGE, certs , ksm.getPrivateKey(), ksm.MyPrivateKeyPassword.toCharArray());
		//
		//			//myMembershipService.getPSEConfig().validPasswd(PeerManager.PID_EDGE, ksm.MyKeyStorePassword.toCharArray(), ksm.MyPrivateKeyPassword.toCharArray());
		//
		//			AuthenticationCredential myAuthenticationCredential =
		//					new AuthenticationCredential(myLocalGroup, "StringAuthentication", null);
		//
		//			StringAuthenticator MyStringAuthenticator = (StringAuthenticator) myMembershipService.apply(myAuthenticationCredential);
		//
		//
		//			MyStringAuthenticator.setAuth1_KeyStorePassword(KeyStoreManager.MyPrivateKeyPassword);
		//			MyStringAuthenticator.setAuth2Identity(PeerManager.PID_EDGE.toString());
		//			MyStringAuthenticator.setAuth3_IdentityPassword(KeyStoreManager.MyPrivateKeyPassword);
		//
		//
		//			PSECredential MyCredential = null;
		//
		//			if (!MyStringAuthenticator.isReadyForJoin()) {
		//				Log.d(myPeerGroup.peerGroupName,"Authenticator is not complete");
		//			}
		//			MyCredential = membership.join(MyStringAuthenticator);
		//			Log.d(myPeerGroup.peerGroupName,"Group has been joined");
		//			myPeerGroup.setPeerGroup(myLocalGroup);
		//		} catch (Exception e) {
		//			Log.d(myPeerGroup.peerGroupName,"Authentication failed - group not joined");
		//			e.printStackTrace();
		//		}
	}




	protected void parseAdvertisement(Enumeration<Advertisement> advertisementsEnum) {

		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

			while (advertisementsEnum.hasMoreElements()) {
				Advertisement foundAdv = advertisementsEnum.nextElement();


				if (myPeerGroup.getPeerGroup()==null&&foundAdv.getAdvType().compareTo(PeerGroupAdvertisement.getAdvertisementType())==0) {
					Log.d(myPeerGroup.peerGroupName,"Found GROUP Advertisement");
					PeerGroup peerGroup =initPeerGroup((PeerGroupAdvertisement) foundAdv);
					joinGroup(peerGroup);
				}else if(foundAdv.getAdvType().compareTo(RendezVousAdvertisement.getAdvertisementType())==0){


					Log.d(myPeerGroup.peerGroupName,"Found RDV Advertisement");
					RendezVousAdvertisement rdvAdv = (RendezVousAdvertisement) foundAdv;
					if(!rdvAdv.getPeerId().equals(myPeerGroup.getPeerGroup().getPeerID())){

						if(myPeerGroup.getPeerGroup().getRendezVousService().isRendezVous()){


							List<PeerID> listRDV = myPeerGroup.getPeerGroup().getRendezVousService().getLocalRendezVousView();
							for (PeerID peerID : listRDV) {
								if(peerID.equals(rdvAdv.getPeerId())){
									if(peerRDVAdv==null||rdvAdv.getStartDate()<peerRDVAdv.getStartDate()){
										myPeerGroup.getRendezVousService().stopRendezVous();
										peerRDVAdv = null;
										break;

									}else{
										//myPeerGroup.getPeerGroup().getEndpointService().getEndpointRouter().suggestRoute(route)
										//myPeerGroup.getRendezVousService().disconnectFromRendezVous(rdvAdv.getPeerId());
									}
								}
							}

						}else{
							//							RouteAdvertisement r = (RouteAdvertisement) AdvertisementFactory.newAdvertisement(RouteAdvertisement.getAdvertisementType());
							//							r.addDestEndpointAddress(rdvAdv.getRendezVousAddress());
							//							myPeerGroup.getPeerGroup().getEndpointService().getEndpointRouter().suggestRoute(r);
							//myPeerGroup.getPeerGroup().getMembershipService()..EndpointService().
							//							try {
							//								//myPeerGroup.getRendezVousService().connectToRendezVous(rdvAdv.getRendezVousAddress());
							//							} catch (IOException e) {
							//								e.printStackTrace();
							//							}
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
