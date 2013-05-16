package com.peersync;

import java.io.IOException;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import com.commands.ShellConsole;
import com.peersync.data.DataBaseManager;
import com.peersync.events.ScanService;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerSync;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;

public class main2 {

	
	public static void main(String[] args) {
//		System.setProperty("java.util.logging.config.file", "C:\\PeerSyncTest\\Client3\\log.properties");
//		System.setProperty("net.jxta.logging.Logging", "FINEST");
//		System.setProperty("net.jxta.level", "FINEST");
		Constants.getInstance().PEERNAME = "client3";
		Constants.getInstance().PORT = 9787;
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
