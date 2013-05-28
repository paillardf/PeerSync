package com.peersync;

import java.io.IOException;
import java.util.ArrayList;

import org.ibex.nestedvm.util.Seekable.File;

import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFileAvailability;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.tools.Constants;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
		FileAvailability fa = new FileAvailability("test");
	
		
		fa.addSegment(5,10);

		
	
		
		Constants.getInstance().PEERNAME = "client2";
		DataBaseManager.getInstance().saveFileAvailability(fa);
		
		SharedFileAvailability tmp = DataBaseManager.getInstance().getSharedFileAvailability("test");
		
		File f;
		try {
			f = new File("C:\\PeerSyncTest\\Client1\\qzd.txt");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			System.out.println(tmp.getFileAvailability().toXML().toString());
			System.out.println(tmp.getFileSize());
		//DataBaseManager.getInstance().saveFileAvailability(fa);
		
//		for(BytesSegment bs : fa.getSegments())
//		{
//			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
//		}
//		
		FileAvailability fa2 = new FileAvailability("test");
		fa2.addSegment(30, 30);

		//System.out.println("FA 2");
//		for(BytesSegment bs : fa2.getSegments())
//		{
//			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
//		}
		
		//fa.substract(fa2);
		
//		FileAvailability Mafa = new FileAvailability("test");
//		Mafa.addSegment(10, 20);
//		
//		Smarties n = new Smarties("test");
//		ID faID = IDFactory.newPeerGroupID();
//		ID fa2ID = IDFactory.newPeerGroupID();
//		System.out.println("FA1 : "+faID);
//		System.out.println("FA2 : "+fa2ID);
//		n.addFileAvailability(fa, faID);
//		n.addFileAvailability(fa2,fa2ID );
//		n.display(Mafa);
//		
//		SegmentToDownload bs = n.getBestChoice(Mafa);
//		System.out.println("Segment Choisi : "+bs.getSegment().offset+"  "+(bs.getSegment().offset+bs.getSegment().length));
////		for(BytesSegment bs : fa.getSegments())
////		{
////			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
////		}
//		
		

		}
	
	}


