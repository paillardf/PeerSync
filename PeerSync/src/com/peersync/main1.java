package com.peersync;

import java.io.IOException;

import com.peersync.network.PeerManager;
import com.peersync.tools.Outils;

public class main1 {

	
	public static void main(String[] args) {
		try {
			new PeerManager(9788, "client2");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
