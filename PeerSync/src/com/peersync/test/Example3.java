package com.peersync.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public class Example3 extends JFrame {
	public static final File ConfigurationFile_RDV = new File("." + System.getProperty("file.separator") + "config"+System.getProperty("file.separator")+"example3.conf");

	private static PeerGroup netPeerGroup = null,
			wileyHowGroup = null,
			discoveredWileyHowGroup = null;
	private static PeerGroupID wileyHowGroupID;
	private DiscoveryService myDiscoveryService = null;
	private JTextArea displayArea;
	private final static MimeMediaType XMLMIMETYPE = new
			MimeMediaType("text/xml");
	public static void main(String args[]) {
		Example3 myapp = new Example3();
		myapp.addWindowListener (
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				}
				);
		myapp.run();
	}
	public Example3() {
		super("Creator");
		Container c = getContentPane();
		displayArea = new JTextArea();
		c.add (new JScrollPane(displayArea), BorderLayout.CENTER);
		setSize(300,150);
		show();
		launchJXTA();
		getServices();
		wileyHowGroupID = createPeerGroupID("jxta:uuid-DCEF4386EAED4908BE25CE5019EA02");
				wileyHowGroup = createPeerGroup(wileyHowGroupID, "wileyHowGroup",
						"Experimentation Group");
				joinGroup(wileyHowGroup);
	}
	public void run() {

	}
	private void launchJXTA() {
		displayArea.append("Launching Peer into JXTA Network...\n");
		try {
			NetworkManager manager;
			try {
				manager = new NetworkManager(
						NetworkManager.ConfigMode.EDGE, "PeerClient",
						ConfigurationFile_RDV.toURI());
				netPeerGroup = manager.startNetwork();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (PeerGroupException e) {
			System.out.println("Unable to create PeerGroup - Failure");
			e.printStackTrace();
			System.exit(1);
		}
	}
	private void getServices() {
		displayArea.append("Obtaining Discovery Service....\n");
		myDiscoveryService = netPeerGroup.getDiscoveryService();
	}
	PeerGroupID createPeerGroupID(String myStringID) {
		String PsePeerGroupName = "GroupeTest";
		return IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, PsePeerGroupName.getBytes());
	}
	PeerGroup createPeerGroup(PeerGroupID myPeerGroupID, String myPeerGroupName, String myPeerGroupDescription) {
		PeerGroupAdvertisement wileyHowGroupAdvertisement;
		PeerGroup tempPeerGroup = null;
		ModuleImplAdvertisement myGroupImpl = null;
		try {
			myGroupImpl = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		wileyHowGroupAdvertisement = (PeerGroupAdvertisement)
		AdvertisementFactory.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
		wileyHowGroupAdvertisement.setPeerGroupID(myPeerGroupID);
		wileyHowGroupAdvertisement.setModuleSpecID
		(myGroupImpl.getModuleSpecID());
		wileyHowGroupAdvertisement.setName(myPeerGroupName);
		wileyHowGroupAdvertisement.setDescription
		(myPeerGroupDescription);
		displayArea.append("New Peer Group Advertisement has been created\n");
		try {
			myDiscoveryService.publish(wileyHowGroupAdvertisement,
					 PeerGroup.DEFAULT_LIFETIME, PeerGroup.
					DEFAULT_EXPIRATION);
			myDiscoveryService.remotePublish(wileyHowGroupAdvertisement,
					 PeerGroup.DEFAULT_EXPIRATION);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		displayArea.append("New Peer Group Advertisement has been published\n");
				try {
					tempPeerGroup = netPeerGroup.newGroup(wileyHowGroupAdvertisement);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				displayArea.append("New Peer Group has been created\n");
				return tempPeerGroup;
	}
	void joinGroup(PeerGroup myLocalGroup) {
		StructuredDocument myCredentials = null;
		try {
			AuthenticationCredential myAuthenticationCredential =
					new AuthenticationCredential(myLocalGroup, null, myCredentials);
			MembershipService myMembershipService =
					myLocalGroup.getMembershipService();
			net.jxta.membership.Authenticator myAuthenticator =
					myMembershipService.apply(myAuthenticationCredential);
			if (!myAuthenticator.isReadyForJoin()) {
				displayArea.append("Authenticator is not complete\n");
				return;
			}
			
			myMembershipService.join(myAuthenticator);
			displayArea.append("Group has been joined\n");
		} catch (Exception e) {
			displayArea.append("Authentication failed - group not joined\n");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}