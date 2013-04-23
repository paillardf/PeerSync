package com.peersync.network;

import java.io.File;
import java.net.URI;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.protocol.PeerGroupAdv;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.GroupUtils;
import com.peersync.network.group.PeerGroupManager;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;
import com.peersync.tools.PreferencesManager;

public class PeerManager {

	//DEBUG VAL //TODO
	private URI RendezVousSeedURI = URI.create("tcp://" + "127.0.0.1" + ":9711");
	private int PORT = 9799;

    
	

	
 

	public NetworkManager networkManager = null;
	//PeerGroup netPeerGroup = null;
	//PeerGroup myPeerGroup = null;
	//DiscoveryService netDiscoveryService;
	//DiscoveryService myDiscoveryService;
	private PeerGroupManager peerGroupManager;
	public NetworkConfigurator conf;
	public String NAME = "";
	public static String KeyStorePassword;
	public static URI KeyStorePath;
	
	public static PeerID PID_EDGE;

	//public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + "config"+System.getProperty("file.separator")+"jxta.conf");
//peerid = IDFactory.newPeerID(PeerGroupID.worldPeerGroupID, cert[0].getPublicKey().getEncoded());
    
	private static PeerManager instance;
	
	
	public synchronized static PeerManager getInstance(){
		if(instance==null){
			instance = new PeerManager();
		}
		return instance;
	}
	
	
	private PeerManager() {
		
		try {
			PORT = PreferencesManager.getInstance().getPort();
			NAME = Constants.getInstance().PEERNAME; //TODO RETIRER
			PID_EDGE = Constants.getInstance().PEERID;
			new File(Constants.TEMP_PATH+Constants.getInstance().PEERNAME+"/").mkdirs();
			
			File prefFolder = new File(Constants.getInstance().PREFERENCES_PATH()); 
			// Deleting any existing key store and content 
			if(Log.DEBUG)
				NetworkManager.RecursiveDelete(prefFolder); 
			
			prefFolder.mkdirs();
			PeerGroup netPeerGroup = null;
		
			KeyStoreManager ksm = KeyStoreManager.getInstance();
			//ksm.createNewKeys(Constants.PsePeerGroupID.toString(), Constants.PeerGroupKey.toCharArray());
			ksm.addNewKeys(Constants.PsePeerGroupID.toString(), Constants.PSE_SAMPLE_GROUP_ROOT_CERT,
					PSEUtils.pkcs5_Decrypt_pbePrivateKey(Constants.PeerGroupKey.toCharArray(), Constants.PSE_SAMPLE_GROUP_ROOT_CERT.getPublicKey().getAlgorithm(), Constants.PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY)
					, Constants.PeerGroupKey.toCharArray());
		// Start the JXTA network

			
			networkManager = new NetworkManager(
					NetworkManager.ConfigMode.EDGE, "PeerClient",
					prefFolder.toURI());
			//networkManager.setPeerID(PID_EDGE);
			conf = networkManager.getConfigurator();
			conf.addSeedRendezvous(RendezVousSeedURI);
			conf.setTcpPort(PORT);
			conf.setUseMulticast(true);
			conf.setTcpEnabled(true);
			conf.setTcpIncoming(true);
			conf.setTcpOutgoing(true);
			conf.setKeyStoreLocation(ksm.getKeyStoreLocation());
		    conf.setPassword(KeyStoreManager.MyKeyStorePassword);
			conf.setPrincipal(PID_EDGE.toString());
			//conf.setCertificate(ksm.getX509Certificate());
			//conf.setPrivateKey(ksm.getPrivateKey());
			//conf.setPeerID(PID_EDGE);
//conf.save();
//conf.load();

			
			netPeerGroup = networkManager.startNetwork();
			
			
			//globalPeerGroup = manager.getNetPeerGroup();
			//netPeerGroup.getRendezVousService().setAutoStart(false);
			
			 AdvertisementFactory.registerAdvertisementInstance(
		                GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv().getAdvType(),
		                new PeerGroupAdv.Instantiator());
			
			// Registering our stack advertisement instance
				AdvertisementFactory.registerAdvertisementInstance(
						StackAdvertisement.getAdvertisementType(),
						new StackAdvertisement.Instantiator());
				AdvertisementFactory.registerAdvertisementInstance(
	            		RendezVousAdvertisement.getAdvertisementType(),
	            		new RendezVousAdvertisement.Instantiator()
						);
			
				peerGroupManager = new PeerGroupManager(this, netPeerGroup);
				peerGroupManager.addPeerGroupToManage(Constants.PsePeerGroupID, Constants.PsePeerGroupName, Constants.PeerGroupKey);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 
	
		
	}
	
	public void run() {
	}

	public PeerID getPeerId() {
		return PID_EDGE;
	}
	
	public PeerGroupManager getPeerGroupManager(){
		return peerGroupManager;
	}

}
