package com.peersync.network.behaviour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.jxta.content.Content;
import net.jxta.content.ContentService;
import net.jxta.content.ContentShare;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.FileDocument;
import net.jxta.document.MimeMediaType;
import net.jxta.protocol.ContentShareAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.models.FileAvailable;
import com.peersync.models.FileToDownload;
import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.network.listener.ThreadCompleteListener;
import com.peersync.network.query.FileQuery;
import com.peersync.tools.Log;


public class ContentBehaviour extends AbstractBehaviour implements ThreadCompleteListener{

	private static final String NAME = "ContentBehaviour";
	private long lastShareContentAdvertisment=0;
	private ArrayList<FileAvailable> currentlySharedFiles = new ArrayList<FileAvailable>();
	private List<FileQuery> downloadThreadList = new ArrayList<FileQuery>();
	private int runningThreadCount = 0;
	//	private StackVersionQuery queryHandler;
	//	private static final long VALIDITY_STACKVERSION_ADV = 2*60*1000;
	private static final long PUBLISH_ADVERTISEMENT_DELAY = 10*60*1000;
	private static final int MAX_DOWNLOAD_THREAD = 3;


	public ContentBehaviour(MyPeerGroup peerGroup){
		super(peerGroup);
		//queryHandler = new StackVersionQuery(myPeerGroup);


	}

	@Override
	public void run() {
		ContentService service = myPeerGroup.getPeerGroup().getContentService();
		DataBaseManager db = DataBaseManager.getInstance();
		try {

			while(true){
				sleep(4000);

				if(System.currentTimeMillis()-lastShareContentAdvertisment>PUBLISH_ADVERTISEMENT_DELAY){
					lastShareContentAdvertisment  = System.currentTimeMillis();


					ArrayList<FileAvailable> files = db.getFilesAvailableForAPeerGroup(myPeerGroup.getPeerGroup().getPeerGroupID().toString());
					ArrayList<FileAvailable>  unsharedFile = (ArrayList<FileAvailable>) currentlySharedFiles.clone();
					unsharedFile.removeAll(files);

					
					for (FileAvailable fileToUnshare : unsharedFile) {
						service.unshareContent(fileToUnshare.getContentID());
						Log.d("ContentBehaviour", "remove "+fileToUnshare.getAbsFilePath()+"from publish");
					}
					
					currentlySharedFiles = files;
					for (FileAvailable fileToSync : files) {
						FileDocument fileDoc = new FileDocument(new File(fileToSync.getAbsFilePath()), MimeMediaType.AOS);
						Content content = new Content(fileToSync.getContentID(), null, fileDoc);
						List<ContentShare> shares = service.shareContent(content);
						
						DiscoveryService discoService = myPeerGroup.getDiscoveryService();
						for (ContentShare share : shares) {
							//share.addContentShareListener(shareListener);
							ContentShareAdvertisement adv = share.getContentShareAdvertisement();
							try {
								discoService.publish(adv);
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						Log.d("ContentBehaviour", "add "+fileToSync.getAbsFilePath()+" / "+fileToSync.getContentID()+" to publish");
					}
				}
				
//				int threadAlive = 0;
//				
//				for (Entry<String, FileQuery> e : downloadList.entrySet()) {
//					FileQuery fq = e.getValue();
//					if(fq.getState()==Thread.State.TERMINATED){
//						
//					}else if(fq.getState()==Thread.State.NEW){
//						
//					}else if(fq.isAlive()){
//						threadAlive++;
//					}
//				}
				
				ArrayList<FileToDownload> files = db.getFilesToDownload(myPeerGroup.getPeerGroup().getPeerGroupID().toString());
				for (FileToDownload fileToSync : files) {
					FileQuery fq = 	new FileQuery(myPeerGroup, fileToSync);
					if(!downloadThreadList.contains(fq)){
						downloadThreadList.add(fq);		
						fq.addListener(this);
					}
				}
				startDownloadThread();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}



	@Override
	protected void parseAdvertisement(
			Enumeration<Advertisement> advertisementsEnum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPeerGroup(PeerGroupEvent event) {
		if(myPeerGroup.getNetPeerGroup().getPeerGroupID().toString().equals(event.getPeerGroupID().toString())){
			//NETPEERGROUPEVENT
		}else{

			switch (event.getID()) {
			case PeerGroupEvent.RDV_CONNECTION:
				lastShareContentAdvertisment = 0;
				break;
			case PeerGroupEvent.STACK_UPDATE:
				lastShareContentAdvertisment = 0;
				break;
			}

		}
	}

	@Override
	public void notifyOfThreadComplete(Thread thread) {
		
		
		for (int i = 0; i<downloadThreadList.size(); i++) {
			if(downloadThreadList.get(i)==thread){
				downloadThreadList.remove(i);
				break;
			}
				
		}
		runningThreadCount--;
		startDownloadThread();
		
	}
	
	private void startDownloadThread(){
		if(runningThreadCount <MAX_DOWNLOAD_THREAD){
			int threadToRun = MAX_DOWNLOAD_THREAD-runningThreadCount;
			
			for (int i = 0; i<downloadThreadList.size(); i++) {
				if(downloadThreadList.get(i).getState()==Thread.State.NEW&&threadToRun>0){
					downloadThreadList.get(i).start();
					runningThreadCount++;
					threadToRun--;
				}
			}
		}
		
	}

@Override
public void interrupt() {
	for (FileQuery fq : downloadThreadList) {
		fq.interrupt();
	}
	super.interrupt();
}



}
