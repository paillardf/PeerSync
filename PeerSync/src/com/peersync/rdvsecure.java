
package com.peersync;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.FileKeyStoreManager;
import net.jxta.impl.membership.pse.PSEConfig;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;

public class rdvsecure {

	// Static attributes
	public static final String Name = "RendezVous Adelaide, at one end";
	public static final String RdvId = "59616261646162614E50472050325033B1C66B1C64A767E0C896C8F2E708C9D903";
	public static final int TcpPort = 9726;
	public static PeerID PID;
	public static final File CertificateDirectory = new File("./certificates");
	public static final File ConfigurationFile = new File("." + System.getProperty("file.separator") + Name);
	public static final File CertificateFile = new File(CertificateDirectory,RdvId +".crt");
	public static final File KeystoreFile = new File(ConfigurationFile,"keystore");
	public static final String MyJxtaPassword = "password";

	// Global
	public static NetworkManager MyNetworkManager;
	public static NetworkConfigurator MyNetworkConfigurator;
	public static PeerGroup MyNetworkPeerGroup;

	public static KeyStore MyKeyStore;
	public static X509Certificate PSEX509Certificate;
	public static PrivateKey PSEPrivateKey;

	public static PipeAdvertisement GetPipeAdvertisement() {

		// Creating a Pipe Advertisement
		PipeAdvertisement MyPipeAdvertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		PipeID MyPipeID = IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID, Name.getBytes());
		System.out.println("PipeID : "+MyPipeID);

		MyPipeAdvertisement.setPipeID(MyPipeID);
		MyPipeAdvertisement.setType(PipeService.UnicastSecureType);
		MyPipeAdvertisement.setName("Test Socket");
		MyPipeAdvertisement.setDescription("Created by " + Name);

		return MyPipeAdvertisement;

	}

	public static ByteArrayOutputStream getBytesFromFile(File f) throws IOException {
		FileInputStream cfis;

		cfis = new FileInputStream(f);

		int n;
		byte[] buffer = new byte[16];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		while ((n = cfis.read(buffer)) != -1){
			baos.write(buffer,0,n);
		}

		cfis.close();

		return baos;
	}

	public static String getFilenameWithoutExtension(final String fileName)
	{
		final int extensionIndex = fileName.lastIndexOf(".");

		if (extensionIndex == -1)
		{
			return fileName;
		}

		return fileName.substring(0, extensionIndex);
	}


	public static void main(String[] args) throws InterruptedException {
		System.setProperty("net.jxta.endpoint.WireFormatMessageFactory.CBJX_DISABLE", "true");

		//JxtaServerSocket SocketServer = null;
		Socket SocketClient;

		System.out.println("Step 0 : Setting the PID + directories creation if needed");
		try {
			PID = (PeerID) IDFactory.fromURI(URI.create("urn:jxta:cbid-"+RdvId));
			if(!CertificateDirectory.exists()) CertificateDirectory.mkdirs();
			if (!ConfigurationFile.exists()) ConfigurationFile.mkdirs();
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		System.out.println("Step 1 : Configuring the KeyStore");
		try {
			FileKeyStoreManager fksm = new FileKeyStoreManager((String)null, "KS Provider", KeystoreFile);

			if (!fksm.isInitialized()) {
				System.out.println("Keystore is NOT initialized");
				fksm.createKeyStore(MyJxtaPassword.toCharArray());

				PSEUtils.IssuerInfo ForPSE = PSEUtils.genCert("WorldPeerGroup", null);
				PSEX509Certificate = ForPSE.cert;
				PSEPrivateKey = ForPSE.issuerPkey;

				MyKeyStore = fksm.loadKeyStore(MyJxtaPassword.toCharArray());

				X509Certificate[] Temp = { PSEX509Certificate };
				MyKeyStore.setKeyEntry(PID.toString(), PSEPrivateKey, MyJxtaPassword.toCharArray(), Temp);

				fksm.saveKeyStore(MyKeyStore, MyJxtaPassword.toCharArray());
			} else {
				System.out.println( "Keystore is initialized");
				MyKeyStore = fksm.loadKeyStore(MyJxtaPassword.toCharArray());
				PSEX509Certificate = (X509Certificate) MyKeyStore.getCertificate(PID.toString());
				PSEPrivateKey = (PrivateKey) MyKeyStore.getKey(PID.toString(), MyJxtaPassword.toCharArray());
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Step 2 : Configuring and starting the network manager for the Rendez-Vous");
		try {
			MyNetworkManager = new NetworkManager(NetworkManager.ConfigMode.RENDEZVOUS, Name, ConfigurationFile.toURI());

			MyNetworkConfigurator = MyNetworkManager.getConfigurator();

			if( ! MyNetworkConfigurator.exists()) {
				MyNetworkConfigurator.setTcpEnabled(true);

				MyNetworkConfigurator.setHttpEnabled(false);
				MyNetworkConfigurator.setHttp2Enabled(false);

				MyNetworkConfigurator.setTcpIncoming(true);
				MyNetworkConfigurator.setTcpOutgoing(true);
				MyNetworkConfigurator.setTcpPort(TcpPort);

				MyNetworkConfigurator.setPeerID(PID);

				MyNetworkConfigurator.setKeyStoreLocation(KeystoreFile.toURI());
				MyNetworkConfigurator.setPassword(MyJxtaPassword);

				MyNetworkConfigurator.save();
			} else {
				MyNetworkConfigurator.setKeyStoreLocation(KeystoreFile.toURI());
				MyNetworkConfigurator.setPassword(MyJxtaPassword);

				MyNetworkConfigurator.load();
			}

			MyNetworkManager.startNetwork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Step 3 : Getting info on the peer group + adding trusted certificates");
		MyNetworkPeerGroup = MyNetworkManager.getNetPeerGroup();

		try {
			PSEMembershipService pms = (PSEMembershipService) MyNetworkPeerGroup.getMembershipService();
			PSEConfig pcfg = pms.getPSEConfig();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			if ( !CertificateFile.exists()) {
				FileOutputStream cfos = new FileOutputStream(CertificateFile);
				cfos.write(pcfg.getTrustedCertificate(PID).getEncoded());
				cfos.close();
			}

			File[] CRTfiles = CertificateDirectory.listFiles((new rdvsecure()).new CRTFilter());
			int i = CRTfiles.length;

			while(i>0) {
				i--;

				PeerID loadedPeer = (PeerID) IDFactory.fromURI(URI.create("urn:jxta:cbid-"+getFilenameWithoutExtension(CRTfiles[i].getName())));
				if(!loadedPeer.equals(PID)) {
					System.out.println("\tAdding PeerID to trusted list : "+getFilenameWithoutExtension(CRTfiles[i].getName()));

					ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile(CRTfiles[i]).toByteArray());
					X509Certificate loadedCertificate = (X509Certificate) cf.generateCertificate(bais);

					pcfg.setTrustedCertificate(loadedPeer, loadedCertificate);
				}
			}
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Step 4 : Setting up the listener + the BidiPipe");

		Thread.sleep(5000);

		InputPipe inputPipe = null;
		try {
			inputPipe = MyNetworkPeerGroup.getPipeService().createInputPipe(rdvsecure.GetPipeAdvertisement(), new PipeMsgListener() {

				@Override
				public void pipeMsgEvent(PipeMsgEvent event) {
					System.out.println("Je revois qqch");

				}
			});
			//SocketClient = new JxtaSocket(MyNetworkPeerGroup, null, rdvsecure.GetPipeAdvertisement(),30000, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Step 5 : Connecting through the BidiPipe to send message");
		if (inputPipe!=null) {
			System.out.println("\tSuccessfully bound for socket");

			Thread.sleep(40000);
		}
	}

	class CRTFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".crt"));
		}
	}
}
