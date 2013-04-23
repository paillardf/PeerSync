package com.peersync.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;


public class Constants {
	public String PEERNAME;
	public String CONF_FOLDER = "conf";
	//public String PREFERENCES_PATH = CONF_FOLDER+"/"+PEERNAME;
	public PeerID PEERID;
	
	public static final String PsePeerGroupName = "SECURE PeerGroup";
	public static final PeerGroupID PsePeerGroupID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, PsePeerGroupName.getBytes());
	public static String PeerGroupKey  = "topsecret";
	
	public static final String TEMP_PATH = "./tmp/";

	
	
	private static Constants instance;
	
	
	
	public static Constants getInstance(){
		if(instance==null)
			instance = new Constants();
		return instance;
	}

	public String PREFERENCES_PATH() {
		return CONF_FOLDER+"/"+PEERNAME;
	}
	

	private final static String PSE_SAMPLE_GROUP_ROOT_CERT_BASE64 = 
            "MIICGTCCAYKgAwIBAgIBATANBgkqhkiG9w0BAQUFADBTMRUwEwYDVQQKEwx3d3cuanh0YS5vcmcx" + 
            "GzAZBgNVBAMTElBTRV9TYW1wbGVfUm9vdC1DQTEdMBsGA1UECxMURERFMkNGQ0MyMjVBNDM0OUQx" + 
            "QTAwHhcNMDUwNjE5MDIyODM4WhcNMTUwNjE5MDIyODM4WjBTMRUwEwYDVQQKEwx3d3cuanh0YS5v" + 
            "cmcxGzAZBgNVBAMTElBTRV9TYW1wbGVfUm9vdC1DQTEdMBsGA1UECxMURERFMkNGQ0MyMjVBNDM0" +
            "OUQxQTAwgZ4wDQYJKoZIhvcNAQEBBQADgYwAMIGIAoGAdVgeJotJWEEfh/NtusfI8cAIMAq7WxXA" + 
            "ZsPIOYnybHPXFNmCTozs/KW0dx01zI6kfHwO1qYXmR/djJAFhr3VhFdUp8y1wDCf6DT63vFOi47t" + 
            "6TC1yywjZe59VIAxhDt0B8XJnkEbsEl+uO95ec6/U6dYI1vrtWU4ORdSYz615XMCAwEAATANBgkq" + 
            "hkiG9w0BAQUFAAOBgQBRJXLRyIGHvw3GJC3lYwQUDwRSm6vaPKPlCA5Axfwy+jPuStldhuPYOvxz" + 
            "a3NxQ/iBlzTGwoVzgxzArM6oLRvtAAvvkQl8z6Lu+NF2ugMs6XfuzRKqrBvSjNaSYM83E51niga2" + 
            "3UGc4Brbn3RCTPRADykVhWxgiCADNGVBIBUAMw==";
   
    private final static String PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY_BASE64 = 
            "MIICoTAbBgkqhkiG9w0BBQMwDgQIPPpnqsvvaS0CAicQBIICgJAYTLxQfaUMFL08DnrO/tAZioTu" + 
            "TlUnt32h3n9nE/L0UM8u7Q9elq2YwBNN72LD6ODzZKPmS/PnUl0NnE1AOnLVuMUgl1OBXgmUtC4P" + 
            "jfgA+En7S0YEmgZN42ceqMpcKGDiBNdr0ebGD9SVy4/XkTLrNcEcHqrhyC6JkSOAo2EKL9OkS6gR" + 
            "bVp59JSEiAruDvAZnz3XTjlXJYchZGcMVNfCDJVEMCgCsaKkr1Pf5JAfj1kKBJbazwlvqVrU0eI7" + 
            "nPXTdTNVUaZLA7ucbUialef2/osefm5oB00DVkgIkUQSjesVM+THKu3UxIFe+3yTbUsI3zDja+DK" + 
            "36l+UBmCLwFSOzJ1HAzP2qj1yvE/crEsvZMr9QrfNp7acfZQCgJZWFBG0wkdkvpTC0SBbzD6TqdW" + 
            "hbGq8rca4KDkI4HeVoB3yBnMDm52NOtvh2uTKHul7Zz+3GTjXTIT7B4WcdiKmYo5hzdAidHzrWHV" +
            "eTmBnda34kM4o0uX1rQjWe3pfpp7rKG/zRDMUsqaZhK0k3t8IiNZroMnH39wz/kiRWgh+LBZOmi6" + 
            "vG4LeaNDom6+o1tH4lHFXh0uCOSjOOKvX91BaptgXXLuFpny1ZMPnSkWzZA20nCJgNB1+S5RLQGg" + 
            "jObczNUFtI8c/nSlbn339fN9G9/EpGaQuoMqxoSWwVnMnfmBnYlq2LehZ3UC3DgSaxRI9XN/F2Ul" + 
            "ako4dwiccGcMsGHB/eKHQU/Csk9E19GGghwC2L7Tb2zIx01Ctd2yecpK3clhvN35xR5cvtnKKLtA" + 
            "KSi8v6rCLDJ0cPa88QfIHRk+M5ZTDP5QN4A0uFKnsWtMI/xjA9tK4VsMEMtxqjBFem8=";
    public static X509Certificate PSE_SAMPLE_GROUP_ROOT_CERT;
    public static EncryptedPrivateKeyInfo PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY;


static {

    /* Initialize some static final variables */
    try {
        // Initialize the Root certificate.
        byte[] cert_der = PSEUtils.base64Decode(new StringReader(PSE_SAMPLE_GROUP_ROOT_CERT_BASE64));

        CertificateFactory cf = CertificateFactory.getInstance("X509");

        // Initialize the Root private key.
        PSE_SAMPLE_GROUP_ROOT_CERT = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert_der));

        byte[] key_der = PSEUtils.base64Decode(new StringReader(PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY_BASE64));

        PSE_SAMPLE_GROUP_ROOT_ENCRYPTED_KEY = new EncryptedPrivateKeyInfo(key_der);
    } catch (IOException failure) {
        IllegalStateException failed = new IllegalStateException("Could not read certificate or key.");

        failed.initCause(failure);
        throw failed;
    } catch (CertificateException failure) {
        IllegalStateException failed = new IllegalStateException("Could not process certificate.");

        failed.initCause(failure);
        throw failed;
    }
}

}