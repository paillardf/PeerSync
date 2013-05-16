package com.peersync;

import java.io.IOException;

import com.commands.ShellConsole;
import com.peersync.network.PeerSync;
import com.peersync.tools.Constants;

public class main1 {

	
	public static void main(String[] args) {
		

//		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client2\\log.properties");
//		System.setProperty("net.jxta.logging.Logging", "FINEST");
//		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client2";
		Constants.getInstance().PORT = 9788;
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
