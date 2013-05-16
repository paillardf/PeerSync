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
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.listener.ThreadCompleteListener;
import com.peersync.network.query.FileQuery;
import com.peersync.tools.Log;


public class ContentBehaviour extends AbstractBehaviour implements ThreadCompleteListener{

	private static final String TAG = "ContentBehaviour";
	private long lastShareContentAdvertisment=0;
	private ArrayList<FileAvailable> currentlySharedFiles = new ArrayList<FileAvailable>();
	private List<FileQuery> downloadThreadList = new ArrayList<FileQuery>();
	private int runningThreadCount = 0;
	private ContentService service;
	private DataBaseManager db;
	private static final long PUBLISH_ADVERTISEMENT_DELAY = 10*60*1000;
	private static final int MAX_DOWNLOAD_THREAD = 1;


	public ContentBehaviour(BasicPeerGroup peerGroup){
		super(peerGroup);



	}

	@Override
	public void initialize() {
		service = myPeerGroup.getPeerGroup().getContentService();
		db = DataBaseManager.getInstance();
	};


	@Override
	protected int action() {

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

		ArrayList<FileToDownload> files = db.getFilesToDownload(myPeerGroup.getPeerGroup().getPeerGroupID().toString());
		for (FileToDownload fileToSync : files) {
			FileQuery fq = 	new FileQuery(myPeerGroup, fileToSync);
			if(!downloadThreadList.contains(fq)){
				downloadThreadList.add(fq);		
				fq.addListener(this);
			}
		}
		startDownloadThread();
		return 4000;	

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











}
