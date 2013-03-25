package com.peersync.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
 
import net.jxta.content.Content;
import net.jxta.content.ContentID;
import net.jxta.content.ContentProviderEvent;
import net.jxta.content.ContentProviderListener;
import net.jxta.content.ContentService;
import net.jxta.content.ContentShare;
import net.jxta.content.ContentShareEvent;
import net.jxta.content.ContentShareListener;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.FileDocument;
import net.jxta.document.MimeMediaType;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ContentShareAdvertisement;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;



/**
 * This tutorial illustrates the use Content API from the
 * perspective of the serving peer.
 * <p/>
 * The server is started and instructed to serve a file
 * to all peers.
 */
public class ContentServer 
{
	static final String DEFAULT_CONTENT_ID ="urn:jxta:uuid-59616261646162614E50472050325033901EA80A652D476C9D1089545CEDE7B007";
	private transient NetworkManager manager = null;
	private transient PeerGroup netPeerGroup = null;
	private transient boolean waitForRendezvous = true;
	private final ContentID contentID;
	private final File file;
	/**
	 * Content provider listener used to be notified of any activity being
	 * performed by the contrnt provider.
	 */
	private ContentProviderListener provListener =
			new ContentProviderListener() {
		public void contentShared(ContentProviderEvent event) {
			logEvent("Content shared:", event);
		}
		public void contentUnshared(ContentProviderEvent event) {
			logEvent("Content unshared: ", event);
		}
		public boolean contentSharesFound(ContentProviderEvent event) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};
	/**
	 * Content share listener used to be notified of any activity during
	 * a content transfer.
	 */
	private ContentShareListener shareListener = new ContentShareListener() {
		public void shareSessionOpened(ContentShareEvent event) {
			logEvent("Session opened: ", event);
		}
		public void shareSessionClosed(ContentShareEvent event) {
			logEvent("Session closed", event);
		}
		public void shareAccessed(ContentShareEvent event) {
			logEvent("Share access", event);
		}
	};
	/**
	 * Constructor.
	 *
	 * @param toServe file to serve
	 * @param id ContentID to use when serving the toServer file, or
	 * {@code null} to generate a random ContentID
	 * @param waitForRendezvous true to wait for rdv connection, false otherwise
	 */
	public ContentServer(File toServe, ContentID id, boolean waitForRendezvous) {
		try {
			
			
			 File ConfigurationFile = new File("." + System.getProperty("file.separator") + "BLA21");
			 NetworkManager.RecursiveDelete(ConfigurationFile);
			 
			 //new File(new File(".cache"), "ContentClient").toURI()
			manager = new NetworkManager(
					NetworkManager.ConfigMode.EDGE, "ContentSERVER",
					ConfigurationFile.toURI());
			
			NetworkConfigurator MyNetworkConfigurator = manager.getConfigurator();

            // Setting Configuration
            //MyNetworkConfigurator.setUseMulticast(false);

           

            String TheRdvSeed = "tcp://" +"192.168.1.100" + ":"
                    +"9711";
            URI RendezVousSeedURI = URI.create(TheRdvSeed);
            MyNetworkConfigurator.addSeedRendezvous(RendezVousSeedURI);
            MyNetworkConfigurator.setTcpPort(9709);
                MyNetworkConfigurator.setTcpEnabled(true);
                MyNetworkConfigurator.setTcpIncoming(true);
                MyNetworkConfigurator.setTcpOutgoing(true);
			
			manager.startNetwork();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		PeerGroup pg = netPeerGroup = manager.getNetPeerGroup();
		System.out.println("!!!!!!!!!!! PeerGroup !!!!!!!: " + pg.toString());
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
		
		file = toServe;
		if (id == null) {
			contentID = IDFactory.newContentID(
					netPeerGroup.getPeerGroupID(), false);
		} else {
			contentID = id;
		}
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
			/*
			 * Here we setup a Content object that we plan on sharing.
JXSE 2.6 Programmer's Guide 16
			 */
			System.out.println("Creating Content object");
			System.out.println(" ID : " + contentID);
			FileDocument fileDoc = new FileDocument(file, MimeMediaType.AOS);
			Content content = new Content(contentID, null, fileDoc);
			System.out.println(" Content: " + content);
			/*
			 * Now we'll ask the ContentProvider implementations to share
			 * the Content object we just created. This single action may
			 * result in more than one way to share the object, so we get
			 * back a list of ContentShare objects. Pragmatically, we
			 * are likely to get one ContentShare per ContentProvider
			 * implementation, though this isn't necessarily true.
			 */
			List<ContentShare> shares = service.shareContent(content);
			/*
			 * Now that we have some shares, we can advertise them so that
			 * other peers can access them. In this tutorial we are using
			 * the DiscoveryService to publish the advertisements for the
			 * ContentClient program to be able to discover them.
			 */
			DiscoveryService discoService = netPeerGroup.getDiscoveryService();
			for (ContentShare share : shares) {
				/*
				 * We'll attach a listener to the ContentShare so that we
				 * can see any activity relating to it.
				 */
				share.addContentShareListener(shareListener);
				/*
				 * Each ContentShare has it's own Advertisement, so we publish
				 * them all.
				 */
				ContentShareAdvertisement adv = share.getContentShareAdvertisement();
				discoService.publish(adv);
			}
			/*
			 * Wait forever, allowing peers to retrieve the shared Content
			 * until we terminate.
			 */
			System.out.println("Waiting for clients...");
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException intx) {
					System.out.println("Interrupted");
				}
			}
			System.out.println("Exiting");
		} catch (IOException io) {
			io.printStackTrace();
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
	public static void main(String args[]) {
		/*
System.setProperty("net.jxta.logging.Logging", "FINEST");
System.setProperty("net.jxta.level", "FINEST");
System.setProperty("java.util.logging.config.file", "logging.properties");
		 */
		if (args.length > 2) {
			System.err.println("USAGE: ContentServer [File] [ContentID]");
			System.exit(1);
		}
		try {
			File file;
			if (args.length > 0) {
				file = new File(args[0]);
				// Use the file specified
				if (!file.exists()) {
					System.err.println("ERROR: File '" + args[0] + "' does not exist");
					System.exit(-1);
				}
			} else {
				// Create and use a temporary file
				/*file = File.createTempFile("ContentServer_", ".tmp");
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write("This is some test data for our demonstration Content");
						fileWriter.close();*/
				file = new File("test.txt");
			}
			Thread.currentThread().setName(ContentServer.class.getName() + ".main()");
			URI uri;
			if (args.length == 2) {
				uri = new URI(args[1]);
			} else {
				uri = new URI(DEFAULT_CONTENT_ID);
			}
			ContentID id = (ContentID) IDFactory.fromURI(uri);
			String value = System.getProperty("RDVWAIT", "false");
			boolean waitForRendezvous = Boolean.valueOf(value);
			ContentServer socEx = new ContentServer(file, id, true);
			socEx.run();
		} catch (Throwable e) {
			System.out.flush();
			System.err.println("Failed : " + e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}
	///////////////////////////////////////////////////////////////////////////
	// Private methods:
	private void logEvent(String title, ContentProviderEvent event) {
		System.out.println("ContentProviderEvent " + title);
		System.out.println(" ContentID: " + event.getContentID());
		System.out.println(" Provider : " + event.getContentProvider());
		System.out.println(" Shares : " + event.getContentShares().size());
		for (ContentShare share : event.getContentShares()) {
			System.out.println(" " + share.toString());
		}
	}
	private void logEvent(String title, ContentShareEvent event) {
		System.out.println("ContentShareEvent - " + title);
		System.out.println(" Source : " + event.getContentShare());
		System.out.println(" Name : " + event.getRemoteName());
		System.out.println(" Data Start: " + event.getDataStart());
		System.out.println(" Data Size : " + event.getDataSize());
	}
	private void stop() {
		manager.stopNetwork();
	}
}