package com.peersync;

import java.io.IOException;

import net.jxta.id.IDFactory;

import com.peersync.cli.ShellConsole;
import com.peersync.network.PeerSync;
import com.peersync.network.UpnpManager;
import com.peersync.network.content.SyncContentProvider;
import com.peersync.tools.Constants;

public class main1 {

	
	public static void main(String[] args) {
		

//		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client2\\log.properties");
//		System.setProperty("net.jxta.logging.Logging", "FINEST");
//		System.setProperty("net.jxta.level", "FINEST");
		
		
//		System.out.println(IDFactory.newModuleSpecID(IDFactory.newModuleClassID()).toString());

//		UpnpManager upnp = UpnpManager.getInstance();
//				upnp.findGateway();
//				int port = upnp.openPort(9711, 9711, 9715, "TCP", "PeerSyncServ");
//				System.exit(-1);
				Constants.getInstance().PORT = 9789;
		Constants.getInstance().PEERNAME = "client2";
	//	Constants.getInstance().PORT = 9788;
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
