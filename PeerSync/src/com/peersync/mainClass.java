package com.peersync;

import java.util.ArrayList;

import com.peersync.data.DataBaseManager;
import com.peersync.models.Event;
import com.peersync.tools.Constants;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
//		UpnpManager u = UpnpManager.getInstance();
//		u.findGateway();
//		int port = u.openPort(9789, 9789, 9989, "TCP", "PeerSync");
//		System.out.println(port);
		Constants.getInstance().PEERNAME = "client1";
		ArrayList<Event> events = DataBaseManager.getInstance().getEventsInConflict(false);
		int cpt=0;
		System.out.println("Numero conflit : \t | Chemin du fichier  \t | Type de l'evenement en conflit");
		for(Event e : events)
		{
			cpt++;
			char action= 'C';
			switch (e.getAction())
			{ 
			case Event.ACTION_CREATE :
				action = 'C';
				break;
			case Event.ACTION_DELETE :
				action = 'D';
				break;
			case Event.ACTION_UPDATE :
				action = 'U';
				break;
			}
			System.out.println(cpt+"\t\t"+e.getAbsFilePath()+"  \t"+action);
		}
	
	}
}


