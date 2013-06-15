package com.peersync;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.LogUtils;
import org.wetorrent.upnp.PortMappingEntry;






public class mainClass {




	public static void main(String[] unused) throws Exception {

		
		Logger logger = LogUtils.getLogger();
		logger.info("Starting weupnp");

		GatewayDiscover discover = new GatewayDiscover();
		logger.info("Looking for Gateway Devices");
		discover.discover();
		GatewayDevice d = discover.getValidGateway();

		if (null != d) {
		    logger.log(Level.INFO, "Gateway device found.\n{0} ({1})", new Object[]{d.getModelName(), d.getModelDescription()});
		} else {
		    logger.info("No valid gateway device found.");
		    return;
		}

		InetAddress localAddress = d.getLocalAddress();
		logger.log(Level.INFO, "Using local address: {0}", localAddress);
		String externalIPAddress = d.getExternalIPAddress();
		logger.log(Level.INFO, "External address: {0}", externalIPAddress);
		PortMappingEntry portMapping = new PortMappingEntry();

		logger.log(Level.INFO, "Attempting to map port {0}", 9000);
		logger.log(Level.INFO, "Querying device to see if mapping for port {0} already exists", 9000);

		if (!d.getSpecificPortMappingEntry(9000,"TCP",portMapping)) {
		    logger.info("Sending port mapping request");

		    if (d.addPortMapping(9000,9000,localAddress.getHostAddress(),"TCP","test")) {
		        logger.log(Level.INFO, "Mapping succesful: waiting {0} seconds before removing mapping.", 20);
		        
		        Thread.sleep(1000*20);
		        d.deletePortMapping(9000,"TCP");

		        logger.info("Port mapping removed");
		        logger.info("Test SUCCESSFUL");
		    } else {
		        logger.info("Port mapping removal failed");
		        logger.info("Test FAILED");
		    }
		    
		} else {
		    logger.info("Port was already mapped. Aborting test.");
		}

		logger.info("Stopping weupnp");

	}
}



