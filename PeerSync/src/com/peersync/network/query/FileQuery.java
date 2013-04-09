package com.peersync.network.query;

import java.io.File;

import net.jxta.content.ContentID;
import net.jxta.content.ContentService;
import net.jxta.content.ContentTransfer;
import net.jxta.content.ContentTransferAggregator;
import net.jxta.content.TransferException;

import com.peersync.network.group.MyPeerGroup;

public class FileQuery extends Thread{
	
	
	private MyPeerGroup myPeerGroup;
	private ContentID contentID;

	public FileQuery(MyPeerGroup myPeerGroup, ContentID contentID) {
		this.myPeerGroup = myPeerGroup;
		this.contentID = contentID;
	}
	
	@Override
	public void run() {
		try {
			
			ContentService service = myPeerGroup.getPeerGroup().getContentService();
			System.out.println("Initiating Content transfer");
			ContentTransfer transfer = service.retrieveContent(contentID);
			if (transfer == null) {
				/*
				 * * This can happen if no ContentProvider implementations
				 * have the ability to locate and retrieve this ContentID.
				 * In most scenarios, this wouldn't happen.
				 */
				System.out.println("Could not retrieve Content");
			} else {
				/*
				 * Add a listener so that we can watch the state transitions
				 * as they occur.
				 */
				transfer.addContentTransferListener(xferListener);
				/*
				 * If the transfer is actually an aggregation fronting multiple
				 * provider implementations, we can listen in on what the
				 * aggregator is doing.
				 */
				if (transfer instanceof ContentTransferAggregator) {
					ContentTransferAggregator aggregator = (ContentTransferAggregator)
							transfer;
					aggregator.addContentTransferAggregatorListener(aggListener);
				}
				/*
				 * This step is not required but is here to illustrate the
				 * possibility. This advises the ContentProviders to
				 * try to find places to retrieve the Content from but will
				 * not actually start transferring data. We'll sleep after
				 * we initiate source location to allow this to proceed
				 * outwith actual retrieval attempts.
				 */
				System.out.println("Starting source location");
				transfer.startSourceLocation();
				System.out.println("Waiting for 5 seconds to demonstrate source	location...");
						Thread.sleep(5000);
						/*
						 * Now we'll start the transfer in earnest. If we had chosen
						 * not to explicitly request source location as we did above,
						 * this would implicitly locate sources and start the transfer
						 * as soon as enough sources were found.
						 */
						
						transfer.startTransfer(new File("content"));
						/*
						 * Finally, we wait for transfer completion or failure.
						 */
						transfer.waitFor();
			}
		} catch (TransferException transx) {
			transx.printStackTrace(System.err);
		} catch (InterruptedException intx) {
			System.out.println("Interrupted");
		} finally {
			stop();
		}
	}
	
	
}
