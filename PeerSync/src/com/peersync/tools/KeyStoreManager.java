package com.peersync.tools;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import net.jxta.impl.membership.pse.FileKeyStoreManager;
import net.jxta.impl.membership.pse.PSEUtils;

public class KeyStoreManager {

	public static final String MyPrivateKeyPassword = "PrivateKey Password";

	public static final String MyKeyStorePassword = "KeyStore Password";
	public static final String MyKeyStoreProvider = "KeyStore Provider";

	public static final String MyKeyStoreLocation =  "MyKeyStoreLocation";

	
	
	private X509Certificate TheX509Certificate;
	private PrivateKey ThePrivateKey;

	private KeyStore MyKeyStore;

	private String PID;

	public static File MyKeyStoreFile;

	public KeyStoreManager(String PID, String configFolder) throws NoSuchProviderException, KeyStoreException, IOException {
		this.PID = PID;
		// Preparing data
		final File MyKeyStoreDirectory = new File(configFolder  + MyKeyStoreLocation);
		MyKeyStoreFile = new File(configFolder +MyKeyStoreLocation + System.getProperty("file.separator") + "keystore");

		MyKeyStoreDirectory.mkdirs();


		// Creating the key store
		FileKeyStoreManager MyFileKeyStoreManager = new FileKeyStoreManager((String)null, MyKeyStoreProvider, MyKeyStoreFile);

		if(!MyKeyStoreFile.exists()){

			PSEUtils.IssuerInfo ForPSE = PSEUtils.genCert(PID, null);

			TheX509Certificate = ForPSE.cert;
			ThePrivateKey = ForPSE.issuerPkey;

			MyFileKeyStoreManager.createKeyStore(MyKeyStorePassword.toCharArray());

			if (!MyFileKeyStoreManager.isInitialized()) {
				Log.d(this.getClass().getName(), "Keystore is NOT initialized");
			} else {
				Log.d(this.getClass().getName(),"Keystore is initialized");
			}

			// Loading the (empty) keystore 
			MyKeyStore = MyFileKeyStoreManager.loadKeyStore(MyKeyStorePassword.toCharArray());

			// Setting data
			X509Certificate[] Temp = { TheX509Certificate };
			MyKeyStore.setKeyEntry(PID, ThePrivateKey, MyPrivateKeyPassword.toCharArray(), Temp);

			// Saving the data
			MyFileKeyStoreManager.saveKeyStore(MyKeyStore, MyKeyStorePassword.toCharArray());
		}
		// Reloading the KeyStore
		MyKeyStore = MyFileKeyStoreManager.loadKeyStore(MyKeyStorePassword.toCharArray());
		
	}

	public PrivateKey getPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		// Retrieving private key 
		PrivateKey MyPrivateKey = (PrivateKey) MyKeyStore.getKey(PID, MyPrivateKeyPassword.toCharArray());

		if (MyPrivateKey==null) {
			Log.d(this.getClass().getName(),"Private key CANNOT be retrieved");
		} else {
			Log.d(this.getClass().getName(),"Private key can be retrieved");
			Log.d(this.getClass().getName(),MyPrivateKey.toString());
		}
		return MyPrivateKey;
	}

	public X509Certificate getX509Certificate() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		// Retrieving Certificate
		TheX509Certificate = (X509Certificate) MyKeyStore.getCertificate(PID.toString());

		if (TheX509Certificate==null) {
			Log.d(this.getClass().getName(),"X509 Certificate CANNOT be retrieved");
		} else {
			Log.d(this.getClass().getName(),"X509 Certificate can be retrieved");
			Log.d(this.getClass().getName(),TheX509Certificate.toString());
		}
		return TheX509Certificate;
	}



}
