package com.peersync;

import java.sql.SQLException;

import com.peersync.events.EventsManager;

public class mainClass {
	public static void main(String[] args){

		try {

			EventsManagerThread.getEventsManagerThread().start();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			EventsManager.getEventsManager().getStackVersionList("UUID2");
//		} catch (ClassNotFoundException | SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}



	}
}

