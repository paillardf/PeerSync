package com.peersync;

import java.util.ArrayList;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManagerThread;
import com.peersync.models.ClassicFile;
import com.peersync.models.FileAvailable;
import com.peersync.models.FileToDownload;
import com.peersync.models.SharedFolder;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.network.PeerManager;
import com.peersync.tools.Constants;
import com.peersync.tools.PreferencesManager;



public class mainClass {
	public static void main(String[] args){
		Constants.getInstance().PEERNAME = "client1";
		Constants.getInstance().PEERID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Constants.getInstance().PEERNAME.getBytes());

		DataBaseManager db = DataBaseManager.getInstance();
		for(ClassicFile cf : db.getFilesToRemove("urn:jxta:uuid-5345435552454050A565B247726F757059616261646162614E5047205032503302"))
			System.out.println(cf.getAbsFilePath());
//		db.saveSharedFolder(new SharedFolder("5000", "toto", "C:\\Users\\Nicolas.leleu\\Documents\\testTX2"));
//		
//		db.saveSharedFolder(new SharedFolder("5001", "toto", "C:\\Users\\Nicolas.leleu\\Documents\\testTX"));
//		db.saveSharedFolder(new SharedFolder("5002", "toto", "C:\\Users\\Nicolas.leleu\\Documents\\testTX\\ter"));
//		PreferencesManager pref = PreferencesManager.getInstance();
//		pref.setPort(9788);
//		//ArrayList<StackVersion> el = DataBaseManager.getInstance().getSharedFolderVersion("5002").getStackVersionList();
//		EventsManagerThread.getEventsManagerThread().start();
//		
		
//		ArrayList<FileAvailable> res = DataBaseManager.getInstance().getFilesAvailableForAPeerGroup("toto");
//		System.out.println(res.size());
//		for(FileAvailable f :res )
//			System.out.println(f.getAbsFilePath());
//		
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

