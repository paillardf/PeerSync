package com.peersync;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;

import javax.security.cert.CertificateException;

import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.FileKeyStoreManager;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ConfigParams;

public class mainTest {

	
	public static void main(String[] args) throws NoSuchProviderException, KeyStoreException, IOException, PeerGroupException, CertificateException {

		
		// Preparing data 
		String MyKeyStoreFileName = "MyKeyStoreFile"; 
		String MyKeyStoreLocation = "." + File.separator + "MyKeyStoreLocation"; 
		String MyKeyStorePassword = "My Key Store Password"; 
		String MyKeyStoreProvider = "Me Myself And I"; 

		File MyKeyStoreDirectory = new File(MyKeyStoreLocation); 
		File MyKeyStoreFile = new File(MyKeyStoreLocation + File.separator 
		+ MyKeyStoreFileName); 

		// Deleting any existing key store and content 
		NetworkManager.RecursiveDelete(MyKeyStoreDirectory); 
		MyKeyStoreDirectory.mkdirs(); 

		// Creating the key store 
		FileKeyStoreManager MyFileKeyStoreManager = new FileKeyStoreManager( 
		(String)null, MyKeyStoreProvider, MyKeyStoreFile); 
		 
		
		MyFileKeyStoreManager.createKeyStore(MyKeyStorePassword.toCharArray()); 
		// Checking initialization 
		if (MyFileKeyStoreManager.isInitialized()) { 
		     System.out.println("Keystore initialized successfully"); 
		} else { 
		     System.out.println("Keystore NOT initialized successfully"); 
		} 
		
		File prefFolder = new File("/test/"); 
		
		// Erasing any existing configuration 
		NetworkManager.RecursiveDelete(prefFolder);  

		// Creation of the network manager 
		NetworkManager MyNetworkManager = new NetworkManager( 
		NetworkManager.ConfigMode.EDGE, 
		"My Network Manager instance name", prefFolder.toURI()); 
		 
		// Retrieving the network configurator 
		NetworkConfigurator MyNetworkConfigurator = MyNetworkManager.getConfigurator(); 

		// Setting the key store location 
		MyNetworkConfigurator.setKeyStoreLocation(MyKeyStoreFile.toURI()); 

		// Setting the name to be used for the X500 principal 
		String ThePrincipal = "Me, Myself & I"; 
		MyNetworkConfigurator.setPrincipal(ThePrincipal); 

		// Setting a password for encrypting the private key that will be 
		// created as part of the X.509 automatic certificate creation 
		//String SecretKeyPassword = "My Secret Key Password"; 
		MyNetworkConfigurator.setPassword(MyKeyStorePassword); 
		MyNetworkConfigurator.setName("test");
		MyNetworkConfigurator.setDescription("test");
		// Saving the configuration, which will trigger the X.509 certificate creation 
		MyNetworkConfigurator.save(); 

		// Trying to retrieve the X.509 certificate from the configurator 
		X509Certificate MyX509Certificate = MyNetworkConfigurator.getCertificate(); 

		if (MyX509Certificate==null) { 

		     System.out.println("Cannot retrieve X509 certificate from NetworkConfigurator"); 

		} 
		
		MyNetworkManager = new NetworkManager( 
				NetworkManager.ConfigMode.EDGE, 
				"My Network Manager instance name", prefFolder.toURI()); 
		 MyNetworkConfigurator = MyNetworkManager.getConfigurator(); 
		 MyNetworkConfigurator.load();
		// Starting JXTA and retrieving the net peer group 
		PeerGroup TheNetPeerGroup = MyNetworkManager.startNetwork(); 
		
		// Retrieving the configuration parameters 
		ConfigParams MyConfigParams = MyNetworkConfigurator.getPlatformConfig(); 

		// Retrieving the PSE configuration advertisement 
		PSEConfigAdv MyPSEConfigAdv = (PSEConfigAdv) 

		MyConfigParams.getSvcConfigAdvertisement(PeerGroup.membershipClassID); 

		// Retrieving the X.509 certificate 
		MyX509Certificate = MyPSEConfigAdv.getCertificate(); 

		System.out.println(MyX509Certificate.toString()); 
		
	}

}
