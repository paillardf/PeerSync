package com.peersync;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManagerThread;
import com.peersync.models.Event;

public class mainClass {
	public static void main(String[] args){


		//EventsManagerThread.getEventsManagerThread().start();
		
		DataBaseManager.getDataBaseManager().checkEventsIntegrity();

		//DataBaseManager.getDataBaseManager().getStackVersionList("UUID2");

		//System.out.println(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\truc\\trez\\1"));

//		Event e = new Event("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png","newhash","oldhash",2,"Toto");
//		e.save();
		
		
//		Event ret = DataBaseManager.getDataBaseManager().getLastEventOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png");
//		if (ret!=null)
//			System.out.println(ret.getDate());

	}
}

