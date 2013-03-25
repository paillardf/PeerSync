package com.peersync.listener;

import net.jxta.content.ContentTransferEvent;
import net.jxta.content.ContentTransferListener;

/**
 * Content transfer listener used to receive asynchronous updates regarding
 * the transfer in progress.
 */
public class MyContentTransferListener implements ContentTransferListener{




	@Override
	public void contentLocationStateUpdated(ContentTransferEvent event) {
		System.out.println("Transfer location state: "
				+ event.getSourceLocationState());
		System.out.println("Transfer location count: "
				+ event.getSourceLocationCount());

	}

	@Override
	public void contentTransferProgress(ContentTransferEvent event) {
		System.out.println("Transfer state: " + event.getTransferState());

	}

	@Override
	public void contentTransferStateUpdated(ContentTransferEvent event) {
		Long bytesReceived = event.getBytesReceived();
		Long bytesTotal = event.getBytesTotal();
		System.out.println("Transfer progress: "
				+ bytesReceived + " / " + bytesTotal);

	}
}