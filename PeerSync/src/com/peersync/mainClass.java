package com.peersync;

import java.util.ArrayList;

import com.peersync.data.DataBaseManager;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.tools.Constants;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
		FileAvailability fa = new FileAvailability("test");
	
		
		fa.addSegment(5,10);

		
	
		
		Constants.getInstance().PEERNAME = "client2";
		DataBaseManager.getInstance().saveFileAvailability(fa);
		ArrayList<FileAvailability> tmp = DataBaseManager.getInstance().getDownloadingFilesAvailibility();
		
		for(FileAvailability fa111 : tmp)
		{
			System.out.println(fa111.toXML().toString());
		}
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


