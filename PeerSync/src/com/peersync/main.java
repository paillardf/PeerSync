package com.peersync;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import com.peersync.cli.ShellConsole;
import com.peersync.network.PeerSync;
import com.peersync.network.UpnpManager;
import com.peersync.tools.Constants;
import com.peersync.tools.KeyStoreManager;

public class main {

	public static final char BACKSPACE = '\b';
	
	public static void main(String[] args) {

		
		
		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client1\\log.properties");
		System.setProperty("net.jxta.logging.Logging", "FINEST");
		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client1";
		//UpnpManager upnp = UpnpManager.getInstance();
		//upnp.findGateway();
		//int port = upnp.openPort(9789, 9789, 9989, "TCP", "PeerSync");
		Constants.getInstance().PORT = 9711;
		
//		
//		try {
//			KeyStoreManager.getInstance().exportPeerGroup(Constants.PsePeerGroupID.toString(), "./key", "florian".toCharArray(), KeyStoreManager.MyKeyStorePassword.toCharArray());
//		} catch (UnrecoverableKeyException | KeyStoreException
//				| NoSuchAlgorithmException | URISyntaxException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
			PeerSync ps = PeerSync.getInstance();
			ps.start();
			ShellConsole s = ShellConsole.getShellConsole();
			s.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
