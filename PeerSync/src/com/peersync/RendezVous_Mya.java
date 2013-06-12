
package com.peersync;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.PeerGroupAdv;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import Examples.Z_Tools_And_Others.ConnectivityMonitor;
import Examples.Z_Tools_And_Others.DelayedJxtaNetworkStopper;

import com.peersync.network.advertisment.RendezVousAdvertisement;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.tools.Outils;

/**
 * Simple RENDEZVOUS peer connecting via the NetPeerGroup.
 */
public class RendezVous_Mya {

    // Static

    public static final String Name_RDV = "RENDEZVOUS";
    public static final PeerID PID_RDV = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Name_RDV.getBytes());
    public static final int TcpPort_RDV = 9711;
    public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + Name_RDV);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            // Removing any existing configuration?
            NetworkManager.RecursiveDelete(ConfigurationFile_RDV);

            // Creation of the network manager
            final NetworkManager MyNetworkManager = new NetworkManager(
                    NetworkManager.ConfigMode.RENDEZVOUS,
                    Name_RDV, ConfigurationFile_RDV.toURI());

            // Retrieving the network configurator
            NetworkConfigurator MyNetworkConfigurator = MyNetworkManager.getConfigurator();
            
            // Setting Configuration
            MyNetworkConfigurator.setUseMulticast(false);

            MyNetworkConfigurator.setTcpPort(TcpPort_RDV);
            MyNetworkConfigurator.setTcpEnabled(true);
            MyNetworkConfigurator.setTcpIncoming(true);
            MyNetworkConfigurator.setTcpOutgoing(true);

            // Setting the Peer ID
           // MyNetworkConfigurator.setPeerID(PID_RDV);
//            AdvertisementFactory.registerAdvertisementInstance(
//	                Outils.createAllPurposePeerGroupWithPSEModuleImplAdv(net).getAdvType(),
//	                new PeerGroupAdv.Instantiator());
//		
//            AdvertisementFactory.registerAdvertisementInstance(
//            		PeerRDVAdvertisement.getAdvertisementType(),
//            		new PeerAdv.Instantiator()
//					);
            
		// Registering our stack advertisement instance
			AdvertisementFactory.registerAdvertisementInstance(
					StackAdvertisement.getAdvertisementType(),
					new StackAdvertisement.Instantiator());
			
			AdvertisementFactory.registerAdvertisementInstance(
            		RendezVousAdvertisement.getAdvertisementType(),
            		new RendezVousAdvertisement.Instantiator()
					);
            // Starting the JXTA network
            PeerGroup NetPeerGroup = MyNetworkManager.startNetwork();
          
            // Starting the connectivity monitor
            new ConnectivityMonitor(NetPeerGroup);

            // Stopping the network asynchronously
            ConnectivityMonitor.TheExecutor.schedule(
                new DelayedJxtaNetworkStopper(
                    MyNetworkManager,
                    "Click to stop " + Name_RDV,
                    "Stop"),
                0,
                TimeUnit.SECONDS);

        } catch (IOException Ex) {

            System.err.println(Ex.toString());

        } catch (PeerGroupException Ex) {

            System.err.println(Ex.toString());

        }

    }

}
