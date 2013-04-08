package com.peersync;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManagerThread;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;



public class mainClass {
	public static void main(String[] args){
		Constants.getInstance().PEERNAME = "client2";
		Constants.getInstance().PEERID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Constants.getInstance().PEERNAME.getBytes());

		DataBaseManager db = DataBaseManager.getInstance();
		db.saveSharedFolder(new SharedFolder("5000", "", "C:\\Users\\Nicolas.leleu\\Documents\\testTX2"));
		
		db.saveSharedFolder(new SharedFolder("5001", "", "C:\\Users\\Nicolas.leleu\\Documents\\testTX"));
		db.saveSharedFolder(new SharedFolder("5002", "", "C:\\Users\\Nicolas.leleu\\Documents\\testTX\\ter"));
		PreferencesManager pref = PreferencesManager.getInstance();
		pref.setPort(9788);
		EventsManagerThread.getEventsManagerThread().start();
		//PeerManager.getInstance();
		
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

