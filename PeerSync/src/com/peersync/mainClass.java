package com.peersync;

import net.jxta.id.ID;
import net.jxta.id.IDFactory;

import com.peersync.network.content.model.BytesSegment;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.network.content.transfer.Smarties;
import com.peersync.network.content.transfer.SegmentToDownload;





public class mainClass {
	
	
	public static void main( String[] args ) {
		
		FileAvailability fa = new FileAvailability("test");
		fa.addSegment(21, 20);
		fa.addSegment(0, 20);
		
		fa.addSegment(10, 100);
		System.out.println("FA 1");
		for(BytesSegment bs : fa.getSegments())
		{
			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
		}
		
		FileAvailability fa2 = new FileAvailability("test");
		fa2.addSegment(80, 300);

		System.out.println("FA 2");
		for(BytesSegment bs : fa2.getSegments())
		{
			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
		}
		
		//fa.substract(fa2);
		
		Smarties n = new Smarties();
		ID faID = IDFactory.newPeerGroupID();
		ID fa2ID = IDFactory.newPeerGroupID();
		System.out.println("FA1 : "+faID);
		System.out.println("FA2 : "+fa2ID);
		n.addFileAvailability(fa, faID);
		n.addFileAvailability(fa2,fa2ID );
		n.display();
		
		SegmentToDownload bs = n.getBestChoice();
		System.out.println("Segment Choisi : "+bs.getSegment().offset+"  "+(bs.getSegment().offset+bs.getSegment().length));
//		for(BytesSegment bs : fa.getSegments())
//		{
//			System.out.println("Segment : "+bs.offset+"  "+(bs.offset+bs.length));
//		}
		

		}
	}


