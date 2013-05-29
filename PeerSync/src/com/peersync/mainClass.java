package com.peersync;

import java.io.IOException;
import java.util.ArrayList;

import org.ibex.nestedvm.util.Seekable.File;

import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFileAvailability;
import com.peersync.network.UpnpManager;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.tools.Constants;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
		UpnpManager u = UpnpManager.getInstance();
		u.findGateway();
		int port = u.openPort(9789, 9789, 9989, "TCP", "PeerSync");
		System.out.println(port);

		}
	
	}


