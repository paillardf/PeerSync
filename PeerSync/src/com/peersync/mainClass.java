package com.peersync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jxta.id.IDFactory;
import net.jxta.impl.endpoint.tls.TlsTransport;
import net.jxta.peergroup.PeerGroup;

import com.peersync.cli.ShellConsole;





public class mainClass {
	public static String getValue(String queryString)
	{

		Pattern regexName = Pattern.compile(ShellConsole.SPACES+"-f"+ShellConsole.SPACES+ShellConsole.ARG+ShellConsole.SPACES);
		Matcher mName = regexName.matcher(queryString);


		if(mName.find())
			return mName.group(1);
		
		else
			return null;

	}
	
	public static void main( String[] args ) {
		System.out.println(IDFactory.newModuleSpecID(PeerGroup.membershipClassID));
//		UpnpManager u = UpnpManager.getInstance();
//		u.findGateway();
//		int port = u.openPort(9789, 9789, 9989, "TCP", "PeerSync");
//		System.out.println(port);
//		Constants.getInstance().PEERNAME = "client1";
//		DataBaseManager db = DataBaseManager.getInstance();
//		db.saveSharedFolder(new SharedFolder("5000", "toBeReplaced","toooot","un nom" ));
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	 
		try {
			String lineRead = reader.readLine();
			System.out.println(getValue(lineRead));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
}


