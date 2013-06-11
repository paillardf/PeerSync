package com.peersync;

import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.Cipher;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.network.content.ContentSecurity;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;






public class mainClass {


	

		  public static void main(String[] unused) throws Exception {
			  
		    
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



