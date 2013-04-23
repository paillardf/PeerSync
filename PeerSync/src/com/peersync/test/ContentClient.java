package com.peersync.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import Examples.Z_Tools_And_Others.Tools;

import net.jxta.content.ContentID;
import net.jxta.content.ContentService;
import net.jxta.content.ContentTransfer;
import net.jxta.content.ContentTransferAggregator;
import net.jxta.content.ContentTransferAggregatorEvent;
import net.jxta.content.ContentTransferAggregatorListener;
import net.jxta.content.ContentTransferEvent;
import net.jxta.content.ContentTransferListener;
import net.jxta.content.TransferException;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.RdvAdv;
import net.jxta.impl.protocol.RouteAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;

public class ContentClient { 
	private transient NetworkManager manager = null;
	private transient PeerGroup netPeerGroup = null;
	private transient boolean waitForRendezvous = true;
	private final ContentID contentID;
	/**
	 * Content transfer listener used to receive asynchronous updates regarding
	 * the transfer in progress.
	 */
	private ContentTransferListener xferListener =
			new ContentTransferListener() {
		public void contentLocationStateUpdated(ContentTransferEvent event) {
			System.out.println("Transfer location state: "
					+ event.getSourceLocationState());
			System.out.println("Transfer location count: "
					+ event.getSourceLocationCount());
		}
		public void contentTransferStateUpdated(ContentTransferEvent event) {
			System.out.println("Transfer state: " + event.getTransferState());
		}
		public void contentTransferProgress(ContentTransferEvent event) {
			Long bytesReceived = event.getBytesReceived();
			Long bytesTotal = event.getBytesTotal();
			System.out.println("Transfer progress: "
					+ bytesReceived + " / " + bytesTotal);
		}
	};
	/**
	 * Content transfer aggregator listener used to detect transitions
	 * between multiple ContentProviders.
	 */


	private ContentTransferAggregatorListener aggListener =
			new ContentTransferAggregatorListener() {
		public void selectedContentTransfer(ContentTransferAggregatorEvent event) {
			System.out.println("Selected ContentTransfer: "
					+ event.getDelegateContentTransfer());
		}
		public void updatedContentTransferList(ContentTransferAggregatorEvent event) {
			ContentTransferAggregator aggregator =
					event.getContentTransferAggregator();
			System.out.println("ContentTransfer list updated:");
			for (ContentTransfer xfer : aggregator.getContentTransferList()) {
				System.out.println(" " + xfer);
			}
		}
	};
	/**
	 * Constructor.
	 * 
	 * @param id ContentID of the Content we are going to attempt to retrieve.
	 * @param waitForRendezvous true to wait for rdv connection, false otherwise
	 */
	
	public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + "ContentClient");
	
	
	public ContentClient(ContentID id, boolean waitForRendezvous) throws IOException {
		contentID = id;
		// Start the JXTA network
		try {
			
			NetworkManager.RecursiveDelete(ConfigurationFile_RDV);
			manager = new NetworkManager(
					NetworkManager.ConfigMode.EDGE, "ContentClient",
					ConfigurationFile_RDV.toURI());
			
			String TheRdvSeed = "tcp://" + "192.168.1.100" + ":9711";
            URI RendezVousSeedURI = URI.create(TheRdvSeed);
            NetworkConfigurator conf = manager.getConfigurator();
            conf.addSeedRendezvous(RendezVousSeedURI);
//            String TheRdvSeed1 = "tcp://" + "192.168.1.83" + ":9711";
//            URI RendezVousSeedURI1 = URI.create(TheRdvSeed1);
//            conf.addSeedRendezvous(RendezVousSeedURI1);
            conf.setTcpPort(9740);
            conf.setTcpEnabled(true);
            conf.setTcpIncoming(true);
            conf.setTcpOutgoing(true);
            
			PeerGroup peergroup = manager.startNetwork();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		netPeerGroup = manager.getNetPeerGroup();
		if (waitForRendezvous) {
			manager.waitForRendezvousConnection(0);
		}
		DiscoveryService TheDiscoveryService = netPeerGroup.getDiscoveryService();
		
		
		int TheAdvEnum = TheDiscoveryService.getRemoteAdvertisements( null,
				DiscoveryService.PEER,
				null,null,
				0, new DiscoveryListener() {
					
					@Override
					public void discoveryEvent(DiscoveryEvent TheDiscoveryEvent) {
						  // Who triggered the event?
				        DiscoveryResponseMsg TheDiscoveryResponseMsg = TheDiscoveryEvent.getResponse();
				        
				        if (TheDiscoveryResponseMsg!=null) {
				            
				            Enumeration<Advertisement> TheEnumeration = TheDiscoveryResponseMsg.getAdvertisements();
				            
				            while (TheEnumeration.hasMoreElements()) {
				                
				                try {
				                    
				                    PeerAdvertisement ThePeer = (PeerAdvertisement) TheEnumeration.nextElement();
				                    System.out.println( "Received advertisement of: " + ThePeer.getName());
				                    System.out.println( "Advertisement count reponses: " + TheDiscoveryResponseMsg.getResponseCount());
				                    Enumeration<Advertisement> reponses = TheDiscoveryEvent.getSearchResults();
				                    while (reponses.hasMoreElements()) {
				                    	
				                    	System.out.println( "reponses: " + reponses.nextElement());
				                        
				                    }
				                    
				                    
				                    
				                   // Tools.PopInformationMessage(Name, "Received advertisement of: " + ThePeer.getName());
				                    
				                } catch (ClassCastException Ex) {
				                    
				                    // We are not dealing with a Peer Advertisement
				                    
				                }
				                
				            }
				            
				        }
						
					}
				} );
		
		
       // Enumeration<Advertisement> TheAdvEnum = TheDiscoveryService.getLocalAdvertisements(DiscoveryService.ADV, null, null);
        
//        while (TheAdvEnum.hasMoreElements()) { 
//            
//            Advertisement TheAdv = TheAdvEnum.nextElement();
//            
//            String ToDisplay = "Found " + TheAdv.getClass().getSimpleName();
//            
//            if (TheAdv.getClass().getName().compareTo(RouteAdv.class.getName())==0) {
//                
//                // We found a route advertisement
//                RouteAdv Temp = (RouteAdv) TheAdv;
//                ToDisplay = ToDisplay + "\n\nto " + Temp.getDestPeerID().toString();
//                
//            } else if (TheAdv.getClass().getName().compareTo(RdvAdv.class.getName())==0) {
//                
//                // We found a rendezvous advertisement
//                RdvAdv Temp = (RdvAdv) TheAdv;
//                ToDisplay = ToDisplay + "\n\nof " + Temp.getPeerID().toString();
//                
//            }
//            
//            // Displaying the advertisement
//            Tools.PopInformationMessage("ContentClient", ToDisplay);
//
//            // Flushing advertisement
//            TheDiscoveryService.flushAdvertisement(TheAdv);
//                    
//        }
//		
		
	}
	/**
	 * Interact with the server.
	 */
	public void run() {
		try {
			/*
			 * Get the PeerGroup's ContentService instance.
			 */
			ContentService service = netPeerGroup.getContentService();
			System.out.println("Initiating Content transfer");
			// Create a transfer instance
			ContentTransfer transfer = service.retrieveContent(contentID);
			if (transfer == null) {
				/*
				 * * This can happen if no ContentProvider implementations
				 * have the ability to locate and retrieve this ContentID.
				 * In most scenarios, this wouldn't happen.
				 */
				System.out.println("Could not retrieve Content");
			} else {
				/*
				 * Add a listener so that we can watch the state transitions
				 * as they occur.
				 */
				transfer.addContentTransferListener(xferListener);
				/*
				 * If the transfer is actually an aggregation fronting multiple
				 * provider implementations, we can listen in on what the
				 * aggregator is doing.
				 */
				if (transfer instanceof ContentTransferAggregator) {
					ContentTransferAggregator aggregator = (ContentTransferAggregator)
							transfer;
					aggregator.addContentTransferAggregatorListener(aggListener);
				}
				/*
				 * This step is not required but is here to illustrate the
				 * possibility. This advises the ContentProviders to
				 * try to find places to retrieve the Content from but will
				 * not actually start transferring data. We'll sleep after
				 * we initiate source location to allow this to proceed
				 * outwith actual retrieval attempts.
				 */
				System.out.println("Starting source location");
				transfer.startSourceLocation();
				System.out.println("Waiting for 5 seconds to demonstrate source	location...");
						Thread.sleep(5000);
						/*
						 * Now we'll start the transfer in earnest. If we had chosen
						 * not to explicitly request source location as we did above,
						 * this would implicitly locate sources and start the transfer
						 * as soon as enough sources were found.
						 */
						
						transfer.startTransfer(new File("content"));
						/*
						 * Finally, we wait for transfer completion or failure.
						 */
						transfer.waitFor();
			}
		} catch (TransferException transx) {
			transx.printStackTrace(System.err);
		} catch (InterruptedException intx) {
			System.out.println("Interrupted");
		} finally {
			stop();
		}
	}
	/**
	 * If the java property RDVWAIT set to true then this demo
	 * will wait until a rendezvous connection is established before
	 * initiating a connection
	 *
	 * @param args none recognized.
	 */
	
	static final String DEFAULT_CONTENT_ID = "urn:jxta:uuid-59616261646162614E50472050325033901EA80A652D476C9D1089545CEDE7B007";
	public static void main(String args[]) {
		/*
			 System.setProperty("net.jxta.logging.Logging", "FINEST");
			 System.setProperty("net.jxta.level", "FINEST");
			 System.setProperty("java.util.logging.config.file", "logging.properties");
		 */
		if (args.length > 1) {
			System.err.println("USAGE: ContentClient [ContentID]");
			System.exit(1);
		}
		try {
			Thread.currentThread().setName(ContentClient.class.getName() + ".main()");
			URI uri;
			if (args.length == 0) {
				uri = new URI(DEFAULT_CONTENT_ID);
			} else {
				uri = new URI(args[0]);
			}
			ContentID id = (ContentID) IDFactory.fromURI(uri);
			String value = System.getProperty("RDVWAIT", "false");
			boolean waitForRendezvous = Boolean.valueOf(value);
			ContentClient socEx = new ContentClient(id, true);
			socEx.run();
		} catch (Throwable e) {
			System.out.flush();
			System.err.println("Failed : " + e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}
	private void stop() {
		manager.stopNetwork();
	}


}
