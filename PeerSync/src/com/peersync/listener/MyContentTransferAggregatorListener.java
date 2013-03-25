package com.peersync.listener;

import net.jxta.content.ContentTransfer;
import net.jxta.content.ContentTransferAggregator;
import net.jxta.content.ContentTransferAggregatorEvent;
import net.jxta.content.ContentTransferAggregatorListener;

/**
 * Content transfer aggregator listener used to detect transitions
 * between multiple ContentProviders.
 */



public class MyContentTransferAggregatorListener implements ContentTransferAggregatorListener {

	@Override
	public void selectedContentTransfer(ContentTransferAggregatorEvent event) {
		System.out.println("Selected ContentTransfer: "
				+ event.getDelegateContentTransfer());
		
	}

	@Override
	public void updatedContentTransferList(ContentTransferAggregatorEvent event) {
		ContentTransferAggregator aggregator =	event.getContentTransferAggregator();
		System.out.println("ContentTransfer list updated:");
		for (ContentTransfer xfer : aggregator.getContentTransferList()) {
			System.out.println(" " + xfer);
		}
	}

}
