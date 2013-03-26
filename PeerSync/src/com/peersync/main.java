package com.peersync;

import java.io.IOException;

import com.peersync.network.PeerManager;
import com.peersync.tools.Outils;

public class main {

	
	public static void main(String[] args) {
		try {
			new PeerManager(9789, "client1");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
