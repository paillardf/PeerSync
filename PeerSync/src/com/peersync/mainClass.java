package com.peersync;

import java.util.ArrayList;

import com.peersync.data.DataBaseManager;
import com.peersync.models.Event;
import com.peersync.models.SharedFolder;
import com.peersync.tools.Constants;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
//		UpnpManager u = UpnpManager.getInstance();
//		u.findGateway();
//		int port = u.openPort(9789, 9789, 9989, "TCP", "PeerSync");
//		System.out.println(port);
		Constants.getInstance().PEERNAME = "client1";
		DataBaseManager db = DataBaseManager.getInstance();
		db.saveSharedFolder(new SharedFolder("5000", "toBeReplaced","toooot","un nom" ));
	
	}
}


