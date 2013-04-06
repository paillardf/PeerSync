package com.peersync;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManagerThread;
import com.peersync.models.Event;
import com.peersync.models.FileToSync;
import com.peersync.models.FileToSyncList;

public class mainClass {
	public static void main(String[] args){


		EventsManagerThread.getEventsManagerThread().start();
		
		//DataBaseManager.getDataBaseManager().checkEventsIntegrity();
		
//		FileToSyncList fsl = new FileToSyncList();
//		fsl.reload();
//		for (FileToSync fs : fsl.getFilesWithLocalSource())
//		{
//			System.out.println(fs.getRelFilePath()+"  "+fs.getLocalSource());
//		}
//		System.out.println("Down");
//		for (FileToSync fs : fsl.getFilesToDownload())
//		{
//			System.out.println(fs.getRelFilePath()+"  "+fs.getLocalSource());
//		}
		

		//DataBaseManager.getDataBaseManager().getStackVersionList("UUID2");

		//System.out.println(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\truc\\trez\\1"));

//		Event e = new Event("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png","newhash","oldhash",2,"Toto");
//		e.save();
		
		
//		Event ret = DataBaseManager.getDataBaseManager().getLastEventOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png");
//		if (ret!=null)
//			System.out.println(ret.getDate());

	}
}

