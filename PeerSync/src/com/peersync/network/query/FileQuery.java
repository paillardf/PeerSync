package com.peersync.network.query;

import java.io.File;
import java.util.ArrayList;

import net.jxta.content.ContentID;
import net.jxta.content.ContentService;
import net.jxta.content.ContentTransfer;
import net.jxta.content.TransferException;

import com.peersync.data.DataBaseManager;
import com.peersync.models.ClassicFile;
import com.peersync.models.FileToDownload;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.network.listener.NotifyingThread;
import com.peersync.tools.Constants;
import com.peersync.tools.FileUtils;
import com.peersync.tools.Log;

public class FileQuery extends NotifyingThread{

	private static int val = 0;
	private MyPeerGroup myPeerGroup;
	private ContentID contentID;
	private String hash;

	public FileQuery(MyPeerGroup myPeerGroup, FileToDownload f) {
		this.myPeerGroup = myPeerGroup;
		this.contentID = f.getContentID();
		this.hash = f.getFileHash();
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
					val++;
					File file = new File(Constants.TEMP_PATH+hash+".tmp");
					transfer.startTransfer(file);
					/*
					 * Finally, we wait for transfer completion or failure.
					 */
					transfer.waitFor();
					Log.d("FileQuery", "END OF TRANSFERT");
					DataBaseManager.exclusiveAccess.lock();
					
					DataBaseManager db = DataBaseManager.getInstance();
					ArrayList<ClassicFile> files = db.getFilesToSyncConcernByThisHash(hash);
					for (ClassicFile classicFile : files) {
						
						FileUtils.copy(file, new File(classicFile.getRelFilePath()));
						
						//TODO mettre a jour des evenements
					}
					DataBaseManager.exclusiveAccess.unlock();
					

				}else{
					Log.d("FileQuery", "has not Enough SourceLocation");
				}
			}
		} catch (TransferException transx) {
			transx.printStackTrace(System.err);
		} catch (InterruptedException intx) {
			Log.d("FileQuery", "Interrupted");
		}
		System.gc();
	}


	

	@Override
	public boolean equals(Object obj) {
		FileQuery fq = (FileQuery)obj;
		return fq.contentID.equals(this.contentID);
	}

}
