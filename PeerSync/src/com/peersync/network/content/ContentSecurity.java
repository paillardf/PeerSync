package com.peersync.network.content;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class ContentSecurity {

	public static byte[] encrypt(byte[] inpBytes, PublicKey key) throws Exception {
		String xform = "RSA/NONE/PKCS1PADDING";
		Cipher cipher = Cipher.getInstance(xform);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(inpBytes);
	}
	public static byte[] decrypt(byte[] inpBytes, PrivateKey key) throws Exception{
		String xform = "RSA/NONE/PKCS1PADDING";
		Cipher cipher = Cipher.getInstance(xform);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(inpBytes);
	}

}
