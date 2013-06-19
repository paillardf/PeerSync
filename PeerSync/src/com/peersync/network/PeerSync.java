package com.peersync.network;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.PeerGroupAdvertisement;
import Examples.Z_Tools_And_Others.ConnectivityMonitor;

import com.peersync.data.DataBaseManager;
import com.peersync.events.ScanService;
import com.peersync.exceptions.BasicPeerGroupException;
import com.peersync.models.SharedFolder;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.group.GroupUtils;
import com.peersync.network.group.PeerGroupManager;
import com.peersync.network.group.SyncPeerGroup;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;


public class PeerSync {



	//	private URI RendezVousSeedURI = URI.create("tcp://" + "78.240.35.201" + ":9711");

	


	//	private URI RendezVousSeedURI = URI.create("tcp://" + "37.161.221.188" + ":9711");
	//private URI RendezVousSeedURI = URI.create("tcp://" + "78.240.35.201" + ":9788");


	private int PORT = 9799;







	public NetworkManager networkManager = null;
	private PeerGroupManager peerGroupManager;
	private NetworkConfigurator conf;
	public String NAME = "";
	private ScanService scanService;


	public String tempPath;
	public  String PREFERENCES_PATH ;

	private static PeerSync instance;


	public synchronized static PeerSync getInstance(){
		if(instance==null){
			instance = new PeerSync();
		}
		return instance;
	}


	private PeerSync(){





	}

	public void initialize(boolean rdv, String name, String password, int port, String configPath, String rdvadress) throws IOException, NoSuchProviderException, KeyStoreException{
		//Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		PORT = port;
		NAME = name;
		System.setProperty(PeerSync.class.getName()+".NAME", NAME);
		File confFile = new File(configPath);
		PREFERENCES_PATH = configPath+"pref/";
		File prefFolder = new File(PREFERENCES_PATH); 
		tempPath = configPath+"tmp/";
		if(Log.isDebug()){
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

		ConfigMode mode =NetworkManager.ConfigMode.EDGE;
		if( rdv)
			mode  = NetworkManager.ConfigMode.RENDEZVOUS;
		KeyStoreManager ksm = KeyStoreManager.getInstance();
		ksm.init(password, configPath);
		networkManager = new NetworkManager(
				mode, NAME,
				prefFolder.toURI());
		//networkManager.setPeerID(PID_EDGE);
		conf = networkManager.getConfigurator();
		if (!conf.exists()||Log.isDebug()) {
			conf.setUseOnlyRendezvousSeeds(false);
			
			if(rdvadress!=null){
				URI RendezVousSeedURI = URI.create(rdvadress);
				conf.addSeedRendezvous(RendezVousSeedURI);
			}
			
			

			conf.setUseMulticast(false);
			conf.setHttpEnabled(false);
			conf.setHttp2Enabled(false);
			conf.setTcpEnabled(true);
			conf.setTcpIncoming(true);
			conf.setTcpOutgoing(true);
			conf.setTcpPort(PORT);

			//		conf.setHttp2Enabled(true);
			//		conf.setHttp2Incoming(true);
			//		conf.setHttp2Outgoing(true);
			//		conf.setHttp2Port(80);

			conf.setKeyStoreLocation(ksm.getKeyStoreLocation());
			conf.setPassword(KeyStoreManager.MyKeyStorePassword);
			conf.setPrincipal(NAME+System.currentTimeMillis());
			conf.setName(NAME);
		}

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
			
			if(netPeerGroup.isRendezvous()&&Log.isDebug())
				new ConnectivityMonitor(netPeerGroup);
			scanService = ScanService.getInstance();
			scanService.startService();
			peerGroupManager = new PeerGroupManager(this, netPeerGroup);

			//PeerGroupID peerID = createPeerGroup("group", "SyncGroup");
			//peerGroupManager.startPeerGroup(id);
			//addShareFolder(id, "C:\\PeerSyncTest\\"+Constants.getInstance().PEERNAME, "mon dossier");
			//exportPeerGroup(id, "C:\\PeerSyncTest\\export", "password".toCharArray());
			//new ConnectivityMonitor(netPeerGroup);


//			PeerGroupID peerID = PeerSync.getInstance().importPeerGroup("C:\\PeerSyncTest\\export", "password".toCharArray());
//			peerGroupManager.startPeerGroup(peerID);
//			addShareFolder(peerID, "C:\\PeerSyncTest\\"+Constants.getInstance().PEERNAME, "mon dossier");

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


	public void exportPeerGroup(PeerGroupID peerGroupID, String outPath, char[] encryptedKey) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, URISyntaxException, IOException {
		PeerGroupAdvertisement pse_pga = null;
		KeyStoreManager ks = KeyStoreManager.getInstance();
		BasicPeerGroup pg = peerGroupManager.getPeerGroup(peerGroupID);
		EncryptedPrivateKeyInfo encryptedInvitationKey = ks.getEncryptedPrivateKey(peerGroupID.toString(), encryptedKey, KeyStoreManager.MyKeyStorePassword.toCharArray());
		X509Certificate[] issuerChain = {ks.getX509Certificate(peerGroupID.toString())};
		// Create the invitation.
		pse_pga = GroupUtils.build_psegroup_adv(GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv(),peerGroupID.toString(),pg.getPeerGroupName(),pg.getDescription(), issuerChain
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


	public void exit() {
		try {
			peerGroupManager.stopPeerGroups();
		} catch (BasicPeerGroupException e) {
			e.printStackTrace();
		}
		networkManager.stopNetwork();
		System.exit(0);
	}
}
