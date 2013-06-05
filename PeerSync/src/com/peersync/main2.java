package com.peersync;

import java.io.IOException;

import com.peersync.cli.ShellConsole;
import com.peersync.network.PeerSync;
import com.peersync.tools.Constants;

public class main2 {

	
	public static void main(String[] args) {
//		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client3\\log.properties");
		System.setProperty("net.jxta.logging.Logging", "FINEST");
		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client3";
		Constants.getInstance().PORT = 9787;
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
