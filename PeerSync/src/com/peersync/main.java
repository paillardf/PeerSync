package com.peersync;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.util.Properties;

import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.cli.ShellConsole;
import com.peersync.exceptions.BasicPeerGroupException;
import com.peersync.network.PeerSync;
import com.peersync.network.UpnpManager;
import com.peersync.tools.Constants;
import com.peersync.tools.Log;

public class main {

	private static final String PATH = "-p";
	private static final String IMPORT = "-i";
	private static final String JXSELOG = "-log";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		boolean importer = false;
		boolean log = false;
		String prefpath = "config.properties";
		for (int i = 0; i< args.length; i++) {
			if(args[i].equals(PATH)){
				prefpath = args[i+1];
				i++;
			}else if(args[i].equals(IMPORT)){
				importer = true;
			}else if(args[i].equals(JXSELOG)){
				log = true;
			}
		}

		Properties prop = new Properties();
		prop.load(new FileInputStream(prefpath));
		String name = prop.getProperty("name");
		boolean rdv = prop.getProperty("rdv").equals("true");
		String path = prop.getProperty("path");
		String password = prop.getProperty("password");
		String rdvadress = prop.getProperty("rdvadress");
		System.setProperty(Log.LEVEL_DEBUG, prop.getProperty("debug"));
		int port = Integer.parseInt(prop.getProperty("port"));

		if(!log){
			System.setProperty("java.util.logging.config.file", path+"log.properties");
			System.setProperty("net.jxta.logging.Logging", "FINEST");
			System.setProperty("net.jxta.level", "FINEST");
		}
		
		//	System.setProperty("net.jxta.endpoint.WireFormatMessageFactory.CBJX_DISABLE", "false");


		//System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client1\\log.properties");
		//		System.setProperty("net.jxta.logging.Logging", "FINEST");
		//		System.setProperty("net.jxta.level", "FINEST");
		UpnpManager upnp = UpnpManager.getInstance();
		upnp.findGateway();
		int portOuvert = upnp.openPort(port, port, port+50, "TCP", "PeerSync");
		System.out.println("Port ouvert : " +portOuvert);
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
			ps.initialize(rdv, name,password, port, path, rdvadress);
			ps.start();


			//TODO to remove
			if(importer){
				try {
					PeerGroupID peerID = PeerSync.getInstance().importPeerGroup("export", "password".toCharArray());
					ps.getPeerGroupManager().startPeerGroup(peerID);
					ps.addShareFolder(peerID, "C:/PeerSyncTest/"+name, "mon dossier");
				} catch (UnrecoverableKeyException | PeerGroupException
						| ProtocolNotSupportedException | NoSuchAlgorithmException
						| BasicPeerGroupException | URISyntaxException e) {
					e.printStackTrace();
				}
			}






			ShellConsole s = ShellConsole.getShellConsole();
			s.start();
		} catch (IOException | NoSuchProviderException | KeyStoreException e) {
			e.printStackTrace();
		}
	}

}
