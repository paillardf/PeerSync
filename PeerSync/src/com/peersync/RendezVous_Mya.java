
package com.peersync;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.peersync.network.UpnpManager;

import Examples.Z_Tools_And_Others.ConnectivityMonitor;
import Examples.Z_Tools_And_Others.DelayedJxtaNetworkStopper;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

/**
 * Simple RENDEZVOUS peer connecting via the NetPeerGroup.
 */
public class RendezVous_Mya {

    // Static

    public static final String Name_RDV = "RENDEZVOUS";
    public static final PeerID PID_RDV = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Name_RDV.getBytes());
    public static final int TcpPort_RDV = 9788;
    public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + Name_RDV);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
        	UpnpManager upnp = UpnpManager.getInstance();
    		upnp.findGateway();
    		int port = upnp.openPort(9788, 9788, 9788, "TCP", "PeerSync");
    		System.out.println(port);

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
            
            MyNetworkConfigurator.setHttp2Enabled(false);
            MyNetworkConfigurator.setHttp2Incoming(false);
            MyNetworkConfigurator.setHttp2Outgoing(false);
    	
            MyNetworkConfigurator.setHttpEnabled(false);
            MyNetworkConfigurator.setHttpIncoming(false);
            MyNetworkConfigurator.setHttpOutgoing(false);

            // Setting the Peer ID
            //MyNetworkConfigurator.setPeerID(PID_RDV);

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
