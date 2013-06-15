package com.peersync.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.ID;
import net.jxta.impl.membership.pse.FileKeyStoreManager;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerGroupAdvertisement;

import com.peersync.network.group.GroupUtils;

public class KeyStoreManager {

	//public static final String MyPrivateKeyPassword = "PrivateKey Password";

	public static final String MyKeyStorePassword = "KeyStore Passwordddd";
	public static final String MyKeyStoreProvider = "KeyStore Provider";

	public static final String MyKeyStoreLocation =  Constants.getInstance().PREFERENCES_PATH() + "/MyKeyStoreLocation/";


	private KeyStore MyKeyStore;

	private File MyKeyStoreFile;

	private FileKeyStoreManager MyFileKeyStoreManager;

	private static KeyStoreManager instance;


	public static KeyStoreManager getInstance(){
		if(instance==null)
			try {
				instance=new KeyStoreManager();
			} catch (NoSuchProviderException | KeyStoreException | IOException e) {
				e.printStackTrace();
			}
		return instance;
	}

	private KeyStoreManager() throws NoSuchProviderException, KeyStoreException, IOException {
		// Preparing data
		final File MyKeyStoreDirectory = new File(MyKeyStoreLocation);
		MyKeyStoreFile = new File(MyKeyStoreLocation+"keystore");

		MyKeyStoreDirectory.mkdirs();


		// Creating the key store
		MyFileKeyStoreManager = new FileKeyStoreManager((String)null, MyKeyStoreProvider, MyKeyStoreFile);

		if(!MyKeyStoreFile.exists()){

			MyFileKeyStoreManager.createKeyStore(MyKeyStorePassword.toCharArray());

			if (!MyFileKeyStoreManager.isInitialized()) {
				Log.s("Keystore is NOT initialized");
			} else {
				Log.i("Keystore is initialized");
			}
		}
		// Reloading the KeyStore
		MyKeyStore = MyFileKeyStoreManager.loadKeyStore(MyKeyStorePassword.toCharArray());

	}




	public boolean createNewKeys(String ID, char[] key) {
		PSEUtils.IssuerInfo ForPSE = PSEUtils.genCert(ID, null);		
		X509Certificate TheX509Certificate = ForPSE.cert;
		PrivateKey ThePrivateKey = ForPSE.issuerPkey;
		Log.i(ForPSE.cert.toString());
		Log.i(ForPSE.issuerPkey.toString());
		return this.addNewKeys(ID, TheX509Certificate, ThePrivateKey, key);
	}

	public boolean addNewKeys(String ID , X509Certificate TheX509Certificate,PrivateKey ThePrivateKey, char[] key ) {

		try {

			// Setting data
			X509Certificate[] Temp = { TheX509Certificate };
			MyKeyStore.setKeyEntry(ID, ThePrivateKey, key, Temp);

			// Saving the data

			MyFileKeyStoreManager.saveKeyStore(MyKeyStore, MyKeyStorePassword.toCharArray());
			MyKeyStore = MyFileKeyStoreManager.loadKeyStore(MyKeyStorePassword.toCharArray());
		} catch (IOException | KeyStoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public X509Certificate getX509Certificate(String ID) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		// Retrieving Certificate
		X509Certificate TheX509Certificate = (X509Certificate) MyKeyStore.getCertificate(ID);

		if (TheX509Certificate==null) {
			Log.w("X509 Certificate CANNOT be retrieved");
		} else {
			Log.i("X509 Certificate can be retrieved");
			Log.i(TheX509Certificate.toString());
		}
		return TheX509Certificate;
	}

	public URI getKeyStoreLocation() {
		return this.MyKeyStoreFile.toURI();
	}
	public PrivateKey getPrivateKey(String ID , char[] key ) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		// Retrieving private key 
		PrivateKey MyPrivateKey = (PrivateKey) MyKeyStore.getKey(ID,key);

		if (MyPrivateKey==null) {
			Log.w("Private key CANNOT be retrieved");
		} else {
			Log.i("Private key can be retrieved");
			Log.i(MyPrivateKey.toString());
		}
		return MyPrivateKey;
	}
	public EncryptedPrivateKeyInfo getEncryptedPrivateKey(String ID, char[] encryptedKey, char[] keyStoreKey) {
		PrivateKey newPriv;
		EncryptedPrivateKeyInfo encypted = null;
		try {
			newPriv = getPrivateKey(ID, keyStoreKey);
			encypted = PSEUtils.pkcs5_Encrypt_pbePrivateKey(encryptedKey, newPriv, 500);
		} catch (UnrecoverableKeyException | KeyStoreException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encypted;
	}



	


}
