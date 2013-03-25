package com.peersync;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.PeerGroupAdv;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;
import com.peersync.tools.Outils;

public class PeerManager {

	//DEBUG VAL //TODO
	private URI RendezVousSeedURI = URI.create("tcp://" + "192.168.1.100" + ":9711");
	private int PORT = 9799;

    
	public static final String PsePeerGroupName = "SECURE PeerGroup";
	public static final PeerGroupID PsePeerGroupID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, PsePeerGroupName.getBytes());


	
 

	NetworkManager manager = null;
	//PeerGroup netPeerGroup = null;
	//PeerGroup myPeerGroup = null;
	NetworkStats networkStats;
	//DiscoveryService netDiscoveryService;
	//DiscoveryService myDiscoveryService;
	private KeyStoreManager keyStoreManager;
	private DiscoveryBehaviour peerNetworkManager;
	private PeerGroupManager peerGroupManager;
	static PeerID PID_EDGE;

	//public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + "config"+System.getProperty("file.separator")+"jxta.conf");


	public PeerManager(int port, String name) throws IOException {
		PORT = port;
		PID_EDGE = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());
		String configFolder = "." + System.getProperty("file.separator") + name +System.getProperty("file.separator");
		
		
		final File ConfigurationFile_RDV = new File(configFolder+"jxta.conf");
		networkStats = new NetworkStats();
		
		PeerGroup netPeerGroup = null;
		DiscoveryService netDiscoveryService = null;
		try {
			keyStoreManager = new KeyStoreManager(PID_EDGE.toString(), configFolder);
		
		
		// Start the JXTA network

			if(Log.DEBUG)
				NetworkManager.RecursiveDelete(ConfigurationFile_RDV);
			manager = new NetworkManager(
					NetworkManager.ConfigMode.EDGE, "PeerClient",
					ConfigurationFile_RDV.toURI());
			NetworkConfigurator conf = manager.getConfigurator();
			conf.addSeedRendezvous(RendezVousSeedURI);
			conf.setTcpPort(PORT);
			conf.setUseMulticast(true);
			conf.setTcpEnabled(true);
			conf.setTcpIncoming(true);
			conf.setTcpOutgoing(true);
			
			conf.setPeerID(PID_EDGE);
			conf.setKeyStoreLocation(keyStoreManager.MyKeyStoreFile.toURI());
			conf.setPassword(KeyStoreManager.MyKeyStorePassword);
			netPeerGroup = manager.startNetwork();
			netDiscoveryService = netPeerGroup.getDiscoveryService();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 
		//globalPeerGroup = manager.getNetPeerGroup();
		//netPeerGroup.getRendezVousService().setAutoStart(false);
		
		 AdvertisementFactory.registerAdvertisementInstance(
	                Outils.createAllPurposePeerGroupWithPSEModuleImplAdv().getAdvType(),
	                new PeerGroupAdv.Instantiator());
		
		
		
		
		peerGroupManager = new PeerGroupManager(this, netPeerGroup);
		peerGroupManager.addPeerGroupToManage(PsePeerGroupID, PsePeerGroupName);
		
//		peerNetworkManager = new DiscoveryManager(this);
//		peerNetworkManager.start();
		
	}
	
	public void run() {
	}

}
