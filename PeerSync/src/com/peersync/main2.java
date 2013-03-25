package com.peersync;

import java.io.IOException;

import com.peersync.tools.Outils;

public class main2 {

	
	public static void main(String[] args) {
		try {
			new PeerManager(9786, "client3");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
