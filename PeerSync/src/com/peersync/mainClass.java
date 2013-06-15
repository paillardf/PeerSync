package com.peersync;

import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.peersync.network.UpnpManager;
import com.peersync.network.content.ContentSecurity;
import com.peersync.tools.KeyStoreManager;






public class mainClass {


	

		  public static void main(String[] unused) throws Exception {
			  System.out.println(InetAddress.getLocalHost().getHostAddress()  );
			  UpnpManager upnp = UpnpManager.getInstance();
			  upnp.findGateway();
			  int port = upnp.openPort(9788, 9788, 9790, "TCP", "PeerSync");
			  System.out.println(port);	
		    System.exit(-1);
		    // Generate a key-pair
		   // KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		    //kpg.initialize(512); // 512 is the keysize.
		    //KeyPair kp = kpg.generateKeyPair();
		    KeyStoreManager ks =  KeyStoreManager.getInstance();
		    ks.createNewKeys("test", KeyStoreManager.MyKeyStorePassword.toCharArray());
		    PublicKey pubk = ks.getX509Certificate("test").getPublicKey();
		    PrivateKey prvk = ks.getPrivateKey("test",KeyStoreManager.MyKeyStorePassword.toCharArray()) ;

		    byte[] dataBytes =
		        "J2EE Security for Servlets, EJBs and Web Services".getBytes();

		    byte[] encBytes = ContentSecurity.encrypt(dataBytes, pubk);
		    byte[] decBytes = ContentSecurity.decrypt(encBytes, prvk);

		    boolean expected = java.util.Arrays.equals(dataBytes, decBytes);
		    System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
		  }


	}



