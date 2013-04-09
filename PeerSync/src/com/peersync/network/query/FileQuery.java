package com.peersync.network.query;

import java.io.File;

import net.jxta.content.ContentID;
import net.jxta.content.ContentService;
import net.jxta.content.ContentTransfer;
import net.jxta.content.TransferException;

import com.peersync.network.group.MyPeerGroup;
import com.peersync.network.listener.NotifyingThread;
import com.peersync.tools.Log;

public class FileQuery extends NotifyingThread{
	
	
	private MyPeerGroup myPeerGroup;
	private ContentID contentID;

	public FileQuery(MyPeerGroup myPeerGroup, ContentID contentID) {
		this.myPeerGroup = myPeerGroup;
		this.contentID = contentID;
	}
	
	public void doRun() {
		try {
			
			ContentService service = myPeerGroup.getPeerGroup().getContentService();
			Log.d("FileQuery", "Initiating Content transfer");
			ContentTransfer transfer = service.retrieveContent(contentID);
			if (transfer == null) {
				Log.d("FileQuery", "Could not retrieve Content");
			} else {
				//transfer.addContentTransferListener(xferListener);
//				if (transfer instanceof ContentTransferAggregator) {
//					ContentTransferAggregator aggregator = (ContentTransferAggregator)
//							transfer;
//					aggregator.addContentTransferAggregatorListener(aggListener);
//				}
				transfer.startSourceLocation();
				Thread.sleep(8000);
				if(transfer.getSourceLocationState().hasEnough()){
					transfer.startTransfer(new File(contentID.toString()));
						/*
						 * Finally, we wait for transfer completion or failure.
						 */
					transfer.waitFor();
					Log.d("FileQuery", "END OF TRANSFERT");
					
				}else{
					Log.d("FileQuery", "has not Enough SourceLocation");
				}
			}
		} catch (TransferException transx) {
			transx.printStackTrace(System.err);
		} catch (InterruptedException intx) {
			Log.d("FileQuery", "Interrupted");
		}
	}

	@Override
	public boolean equals(Object obj) {
		FileQuery fq = (FileQuery)obj;
		return fq.contentID.equals(this.contentID);
	}
	
}
