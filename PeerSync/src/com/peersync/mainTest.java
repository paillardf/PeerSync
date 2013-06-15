package com.peersync;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;

import javax.security.cert.CertificateException;

import net.jxta.exception.PeerGroupException;

import com.peersync.data.DataBaseManager;
import com.peersync.network.UpnpManager;
import com.peersync.tools.Constants;

public class mainTest {


	public static void main(String[] args) throws NoSuchProviderException, KeyStoreException, IOException, PeerGroupException, CertificateException, InterruptedException {
		UpnpManager upnp = UpnpManager.getInstance();
		upnp.findGateway();
		int port = upnp.openPort(9788, 9788, 9790, "TCP", "PeerSync");
	        System.exit(-1);
		
		
		Constants.getInstance().PEERNAME = "client3";
		Constants.getInstance().PORT = 9789;
		DataBaseManager.getInstance().getSharedFileAvailability("d08031229b89c27764e680e526d79144d8b821f3");
		
	}

}
