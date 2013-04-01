package com.peersync;

import com.peersync.events.EventsManagerThread;

public class mainClass {
	public static void main(String[] args){


		EventsManagerThread.getEventsManagerThread().start();

		//DataBaseManager.getDataBaseManager().getStackVersionList("UUID2");




	}
}

