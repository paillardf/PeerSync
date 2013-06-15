package com.peersync.network;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;
import org.xml.sax.SAXException;


public class UpnpManager {

	
	private GatewayDevice gateway;
	private static UpnpManager instance=null;
	
	public static UpnpManager getInstance()
	{
		if(instance==null)
			instance=new UpnpManager();
		return instance;
		
	}

	private UpnpManager()
	{
		setGateway(null);
	}
	
	
	public void findGateway()
	{
		GatewayDiscover discover = new GatewayDiscover();
    	try {
			discover.discover();
			setGateway(discover.getValidGateway());
		} catch (IOException| SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public int openPort(int localPort,int minPortToOpen,int maxPortToOpen,String protocol,String description)
	{
		GatewayDevice gd = getGateway();
		if(gd!=null && (protocol.equals("TCP") || protocol.equals("UDP")) )
		{
			PortMappingEntry portMapping = new PortMappingEntry();
			
			int currentPort=minPortToOpen;
			while(currentPort<=maxPortToOpen)
			{
				InetAddress localAddress = gd.getLocalAddress();
				
				try {
					boolean alreadyExist = gd.getSpecificPortMappingEntry(currentPort,protocol,portMapping);
					if(alreadyExist && portMapping.getInternalClient().equals(localAddress.getHostAddress().toString()) &&  portMapping.getInternalPort()==localPort)
					{
						return currentPort;
					}
					else if (!alreadyExist) 
					{

					    if (gd.addPortMapping(currentPort,localPort,localAddress.getHostAddress(),protocol,description)) 
					    {
					    	return currentPort;
					    	
					    }
					}
						
				} catch (IOException | SAXException e) {
					e.printStackTrace();
				}
				currentPort++;
			}
				
		}
		return -1;
		
		
	}

	public void deletePortMapping(int externalPort,String protocol)
	{
		try {
			getGateway().deletePortMapping(externalPort, protocol);
		} catch (IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GatewayDevice getGateway() {
		return gateway;
	}

	public void setGateway(GatewayDevice gateway) {
		this.gateway = gateway;
	}

}
