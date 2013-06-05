package com.peersync.network;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.content.ContentID;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.events.ScanService;
import com.peersync.models.SharedFolder;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.GroupUtils;
import com.peersync.network.group.PeerGroupManager;
import com.peersync.network.group.SyncPeerGroup;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;

public class PeerSync {

	//DEBUG VAL //TODO


//	private URI RendezVousSeedURI = URI.create("tcp://" + "78.240.35.201" + ":9711");

//	private URI RendezVousSeedURI = URI.create("tcp://" + "37.161.221.188" + ":9711");
	private URI RendezVousSeedURI = URI.create("tcp://" + "192.168.1.50" + ":9711");



	private int PORT = 9799;







	public NetworkManager networkManager = null;
	private PeerGroupManager peerGroupManager;
	private NetworkConfigurator conf;
	public String NAME = "";
	private ScanService scanService;


	private static PeerSync instance;


	public synchronized static PeerSync getInstance(){
		if(instance==null){
			instance = new PeerSync();
		}
		return instance;
	}


	private PeerSync(){

		
		
	

	}

	public void initialize() throws IOException{
		PORT =  Constants.getInstance().PORT;
		NAME = Constants.getInstance().PEERNAME; //TODO RETIRER
		File confFile = new File(Constants.TEMP_PATH+Constants.getInstance().PEERNAME+"/");
		
		File prefFolder = new File(Constants.getInstance().PREFERENCES_PATH()); 
		if(Log.DEBUG){
			NetworkManager.RecursiveDelete(prefFolder); 
			NetworkManager.RecursiveDelete(confFile);
		}
		prefFolder.mkdirs();
		confFile.mkdirs();
		

		//ksm.createNewKeys(Constants.PsePeerGroupID.toString(), Constants.PeerGroupKey.toCharArray());
		//		ksm.addNewKeys(Constants.PsePeerGroupID.toString(), Constants.PSE_SAMPLE_GROUP_ROOT_CERT,
		//				PSEUtils.pkcs5_Decrypt_pbePrivateKey(Constants.PeerGroupKey.toCharArray(), Constants.PSE_SAMPLE_GROUP_ROOT_CERT.getPublicKey().getAlgorithm(), Constants.PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY)
		//				, Constants.PeerGroupKey.toCharArray());
		// Start the JXTA network

		KeyStoreManager ksm = KeyStoreManager.getInstance();
		networkManager = new NetworkManager(
				NetworkManager.ConfigMode.EDGE, NAME,
				prefFolder.toURI());
		//networkManager.setPeerID(PID_EDGE);
		conf = networkManager.getConfigurator();
		conf.setUseOnlyRendezvousSeeds(false);
		conf.addSeedRendezvous(RendezVousSeedURI);
		
		conf.setUseMulticast(true);
		
		conf.setTcpEnabled(true);
		conf.setTcpIncoming(true);
		conf.setTcpOutgoing(true);
		conf.setTcpPort(PORT);
		
		conf.setHttp2Enabled(true);
		conf.setHttp2Incoming(true);
		conf.setHttp2Outgoing(true);
		conf.setHttp2Port(80);
		
		conf.setKeyStoreLocation(ksm.getKeyStoreLocation());
		conf.setPassword(KeyStoreManager.MyKeyStorePassword);
		conf.setPrincipal(NAME+System.currentTimeMillis());
		conf.setName(NAME);
	}
	public void start(){


		try {
			PeerGroup netPeerGroup = null;

			netPeerGroup = networkManager.startNetwork();

			//			 AdvertisementFactory.registerAdvertisementInstance(
			//		                GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv().getAdvType(),
			//		                new PeerGroupAdv.Instantiator());
			//			
			AdvertisementFactory.registerAdvertisementInstance(
					StackAdvertisement.getAdvertisementType(),
					new StackAdvertisement.Instantiator());
			AdvertisementFactory.registerAdvertisementInstance(
					RendezVousAdvertisement.getAdvertisementType(),
					new RendezVousAdvertisement.Instantiator()
					);
			scanService = ScanService.getInstance();
			scanService.startService();
			peerGroupManager = new PeerGroupManager(this, netPeerGroup);
			
			PeerGroupID id = createPeerGroup("my peer group", "sync peer group");
			peerGroupManager.startPeerGroup(id);
			addShareFolder(id, "C:\\PeerSyncTest\\"+Constants.getInstance().PEERNAME, "mon dossier");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 



	}

	

	
	
	public SharedFolder addShareFolder(PeerGroupID peerGroupID, String path, String name){
		ContentID id = IDFactory.newContentID(peerGroupID, false, (path+System.currentTimeMillis()).getBytes());
		SharedFolder sf = new SharedFolder(id.toString(), peerGroupID.toString(), path, name);
		DataBaseManager.getInstance().saveSharedFolder(sf);
		return sf;
	}
	
	public PeerGroupID createPeerGroup(String name, String description){
		KeyStoreManager ks = KeyStoreManager.getInstance();
		PeerGroupID peerGroupID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, (name+System.currentTimeMillis()).getBytes());
		ks.createNewKeys(peerGroupID.toString(), KeyStoreManager.MyKeyStorePassword.toCharArray());
		DataBaseManager db = DataBaseManager.getInstance();
		SyncPeerGroup peerGroup= new SyncPeerGroup(peerGroupID, name, description);
		db.savePeerGroup(peerGroup);
		peerGroupManager.addPeerGroup(peerGroup);
		return peerGroupID;
	}
	
	
	public void exportPeerGroup(String peerGroupID, String outPath, char[] encryptedKey) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, URISyntaxException, IOException {
		PeerGroupAdvertisement pse_pga = null;
		KeyStoreManager ks = KeyStoreManager.getInstance();
		EncryptedPrivateKeyInfo encryptedInvitationKey = ks.getEncryptedPrivateKey(peerGroupID, encryptedKey, KeyStoreManager.MyKeyStorePassword.toCharArray());
		X509Certificate[] issuerChain = {ks.getX509Certificate(peerGroupID)};
		// Create the invitation.
		pse_pga = GroupUtils.build_psegroup_adv(GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv(),peerGroupID, issuerChain
				,encryptedInvitationKey);

		XMLDocument asXML = (XMLDocument) pse_pga.getDocument(MimeMediaType.XMLUTF8);
		FileWriter invitation_file = new FileWriter(outPath);
		asXML.sendToWriter(invitation_file);
		invitation_file.close();
	}

	public PeerGroupID importPeerGroup(String path, char[] encryptedKey) throws IOException{
		FileReader csr_file = new FileReader(path);
		XMLDocument csr_doc = (XMLDocument) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, csr_file);
		csr_file.close();
		KeyStoreManager ks = KeyStoreManager.getInstance();
		PeerGroupAdvertisement pseGroupAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(csr_doc);


		PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement((XMLElement) pseGroupAdv.getServiceParam(PeerGroup.membershipClassID));
		pseConf.getEncryptedPrivateKey();
		PrivateKey private_key = PSEUtils.pkcs5_Decrypt_pbePrivateKey(encryptedKey, pseConf.getEncryptedPrivateKeyAlgo(),  pseConf.getEncryptedPrivateKey());//Encrypt_pbePrivateKey(encryptedKey, newPriv, 500);
		ks.addNewKeys(pseGroupAdv.getPeerGroupID().toString(),  pseConf.getCertificateChain()[0], private_key, KeyStoreManager.MyKeyStorePassword.toCharArray());
		pseGroupAdv.getName();
		pseGroupAdv.getDescription();
		SyncPeerGroup peerGroup = new SyncPeerGroup(pseGroupAdv.getPeerGroupID(), pseGroupAdv.getName(), pseGroupAdv.getDescription());
		
		DataBaseManager db = DataBaseManager.getInstance();
		db.savePeerGroup(peerGroup);
		peerGroupManager.addPeerGroup(peerGroup);
		
		return pseGroupAdv.getPeerGroupID();
	}


	public PeerGroupManager getPeerGroupManager(){
		return peerGroupManager;
	}

	public NetworkConfigurator getConf(){
		return conf;
	}


	public ScanService getScanService() {
		return scanService;

	}
}
