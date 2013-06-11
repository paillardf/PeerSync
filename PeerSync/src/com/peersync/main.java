package com.peersync;


import java.io.IOException;

import com.peersync.cli.ShellConsole;
import com.peersync.network.PeerSync;
import com.peersync.tools.Constants;

public class main {

	public static final char BACKSPACE = '\b';
	
	public static void main(String[] args) {

		System.setProperty("net.jxta.endpoint.WireFormatMessageFactory.CBJX_DISABLE", "true");
		
//		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client1\\log.properties");
//		System.setProperty("net.jxta.logging.Logging", "FINEST");
//		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client1";
		//UpnpManager upnp = UpnpManager.getInstance();
		//upnp.findGateway();
		//int port = upnp.openPort(9789, 9789, 9989, "TCP", "PeerSync");
		Constants.getInstance().PORT = 9788;
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
			ps.initialize();
			ps.start();
			ShellConsole s = ShellConsole.getShellConsole();
			s.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
