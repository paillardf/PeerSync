package com.peersync.network;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import com.peersync.data.DataBaseManager;
import com.peersync.events.ScanService;
import com.peersync.models.SharedFolder;
import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.PeerGroupManager;
import com.peersync.network.group.SyncPeerGroup;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;

public class PeerSync {

	//DEBUG VAL //TODO
	private URI RendezVousSeedURI = URI.create("tcp://" + "127.0.0.1" + ":9711");
	private int PORT = 9799;







	public NetworkManager networkManager = null;
	private PeerGroupManager peerGroupManager;
	private NetworkConfigurator conf;
	public String NAME = "";
	private ScanService scanService;


	private static PeerSync instance;


	public synchronized static PeerSync getInstance() throws IOException{
		if(instance==null){
			instance = new PeerSync();
		}
		return instance;
	}


	private PeerSync() throws IOException {

		PORT =  Constants.getInstance().PORT;
		NAME = Constants.getInstance().PEERNAME; //TODO RETIRER
		new File(Constants.TEMP_PATH+Constants.getInstance().PEERNAME+"/").mkdirs();
		File prefFolder = new File(Constants.getInstance().PREFERENCES_PATH()); 
		if(Log.DEBUG)
			NetworkManager.RecursiveDelete(prefFolder); 
		prefFolder.mkdirs();



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
		conf.addSeedRendezvous(RendezVousSeedURI);
		conf.setTcpPort(PORT);
		conf.setUseMulticast(true);
		conf.setTcpEnabled(true);
		conf.setTcpIncoming(true);
		conf.setTcpOutgoing(true);
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
			ScanService.getInstance().startService();
			peerGroupManager = new PeerGroupManager(this, netPeerGroup);
			
			
			//TODO TEST
			
			KeyStoreManager ks = KeyStoreManager.getInstance();
			ks.createNewKeys(Constants.PsePeerGroupID.toString(), KeyStoreManager.MyKeyStorePassword.toCharArray());
			SyncPeerGroup pg = new SyncPeerGroup(netPeerGroup,conf,  Constants.PsePeerGroupID, Constants.PsePeerGroupName);
			peerGroupManager.addPeerGroupToManage(pg);
			pg.initialize();
			pg.start();
			
			scanService.startService();
			DataBaseManager.getInstance().saveSharedFolder(new SharedFolder("5000", Constants.PsePeerGroupID.toString(), "C:/PeerSyncTest/"+getConf().getName()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 



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
