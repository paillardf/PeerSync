package com.peersync;

import java.sql.SQLException;

import com.peersync.events.EventsManager;

public class mainClass {
	public static void main(String[] args){
		//		EventsManagerThread et;
		//		try {
		//			et = new EventsManagerThread();
		//			et.start();
		//		} catch (ClassNotFoundException | SQLException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		EventsManager et;
		try {
			et = new EventsManager();
			et.getStackVersionList("UUID1");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
}

