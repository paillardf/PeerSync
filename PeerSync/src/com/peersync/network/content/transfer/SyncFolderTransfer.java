package com.peersync.network.content.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.jxta.content.ContentID;
import net.jxta.content.ContentTransferEvent;
import net.jxta.content.ContentTransferListener;
import net.jxta.content.ContentTransferState;
import net.jxta.content.TransferException;
import net.jxta.impl.content.defprovider.DefaultContentShareAdvertisementImpl;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ContentShareAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.models.ClassicFile;
import com.peersync.network.content.SyncContentProvider;
import com.peersync.network.content.model.FilesInfoManager;
import com.peersync.tools.Log;

/**
 *
 */
public class SyncFolderTransfer extends AbstractFolderTransfer {
	/**
	 * Logger instance.
	 */
	private static final Logger LOG =
			Logger.getLogger(SyncFolderTransfer.class.getName());

	/**
	 * The number of seconds between source discovery attempts.
	 */
	private static final int SOURCE_LOCATION_INTERVAL =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".sourceLocationInterval", 30).intValue();

	/**
	 * The number of of knownSources considered to be "enough".
	 */
	private static final int ENOUGH_SOURCES =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".enoughSources", 5).intValue();

	

	/**
	 * The discovery threshold to use.
	 */
	private static final int DISCOVERY_THRESHOLD =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".discoveryTreshold", 10).intValue();

	/**
	 * Periodic check interval, in seconds.
	 */
	private static final int PERIODIC_CHECK_INTERVAL =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".periodicCheckInterval", 5).intValue();



	/**
	 * Maximum number of server pipe.
	 */
	static final int MAX_OUTSTANDING_PIPE =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".maxOutstanding", 5).intValue();






	// Initialized at construction
	private final ScheduledExecutorService executor;
	private final PeerGroup peerGroup;
	public FilesInfoManager filesInfoManager;

	// Managed over the course of the transfer
	private List<DefaultContentShareAdvertisementImpl> sourcesRemaining =
			new ArrayList<DefaultContentShareAdvertisementImpl>();
	private List<DefaultContentShareAdvertisementImpl> sourcesTried =
			new ArrayList<DefaultContentShareAdvertisementImpl>();

	// Initialized via transferInit()
	private ScheduledFuture periodicTask;
	private boolean running = false;

	// Managed by the worker thread and periodic threads after initialiation
	private Thread ownerThread = null;


	private List<PipeManager> pipesManager = new ArrayList<PipeManager>();

	public String sharedFolderUID;







	

	//////////////////////////////////////////////////////////////////////////
	// Constructors:


	/**
	 * Constructor for use with ContentIDs.
	 *
	 * @param origin content provider which created and manager this
	 *  transfer
	 * @param schedExecutor executor to use when running tasks
	 * @param group parent peer group
	 * @param contentID ID of the content that we want to retrieve
	 */
	public SyncFolderTransfer(
			SyncContentProvider origin,
			ScheduledExecutorService schedExecutor,
			PeerGroup group,
			FilesInfoManager filesInfoM,
			ContentID contentID) {
		super(origin, schedExecutor, group, contentID, "SyncContentTransfer");
		setSourceLocationInterval(SOURCE_LOCATION_INTERVAL);
		setDiscoveryThreshold(DISCOVERY_THRESHOLD);
		executor = schedExecutor;
		peerGroup = group;
		filesInfoManager = filesInfoM;
		this.sharedFolderUID = contentID.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// AbstractContentTransfer methods:

	/**
	 * {@inheritDoc}
	 */
	protected boolean hasEnoughLocationCount() {
		return (pipesManager.size()>=MAX_OUTSTANDING_PIPE);
	}

	
	

	/**
	 * {@inheritDoc}
	 */
	protected boolean isAdvertisementOfUse(ContentShareAdvertisement adv) { //FIXME 
		return (adv instanceof DefaultContentShareAdvertisementImpl);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws TransferException when a problem occurs during transfer attempt
	 */
	protected ContentTransferState transferAttempt()
			throws TransferException {
		running = true;
		

		//initialize PipesManager
		checkPipes();
		
		periodicTask = executor.scheduleWithFixedDelay(

				new Runnable() {

					public void run() {
						try {
							criticalEntry();
							periodicCheck();
						} catch (InterruptedException intx) {
							Logging.logCheckedFinest(LOG, "Periodic check interrupted\n", intx);
						} finally {
							criticalExit();
						}
						checkPipes();
					}

				}, 0, PERIODIC_CHECK_INTERVAL, TimeUnit.SECONDS);
		
		
		while (running) {
			if (pipesManager.size() == 0) throw(new TransferException("Could not find usable source"));

			try {
				criticalEntry();

				boolean progress = false;
				for (int i = pipesManager.size()-1; i>=0 ; i--) {
					if(pipesManager.get(i).isRunning()){
						if(pipesManager.get(i).needsUpdate()){
							pipesManager.get(i).update();
							progress = true;
						}
					}else{
						pipesManager.remove(i);
					}
					
					
				}

				if(!progress){
					synchronized (this) {
						wait();
					}
					
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				criticalExit();
			}


			if(DataBaseManager.getInstance().getFilesToDownload(sharedFolderUID).size()==0){
				// We should only get here on success
				return ContentTransferState.COMPLETED;
			}
		}




		return ContentTransferState.CANCELLED;
	}

	protected void checkPipes(){
		List<ContentShareAdvertisement> newList =
				new ArrayList<ContentShareAdvertisement>();
		List<ContentShareAdvertisement> allList =
				new ArrayList<ContentShareAdvertisement>();


		synchronized(lockObject) {

			allList.addAll(allSources);
			newList.addAll(newSources);
			// Reset the new sources list
			newSources.clear();

		}

		// Add new sources to our tracked list
		for (ContentShareAdvertisement candidate : newList) {
			if (candidate instanceof DefaultContentShareAdvertisementImpl) {
				sourcesRemaining.add(
						(DefaultContentShareAdvertisementImpl) candidate);
				Log.d("SyncFolderTransfer", "nouveau candidat "+((DefaultContentShareAdvertisementImpl) candidate).getPipeAdvertisement().getPipeID());
			}
		}

		Logging.logCheckedFine(LOG, "Sources remaining: ", sourcesRemaining.size());
		Logging.logCheckedFine(LOG, "Sources tried    : ", sourcesTried.size());

		if (sourcesRemaining.size() == 0&&pipesManager.size()<MAX_OUTSTANDING_PIPE) {

			//            Logging.logCheckedFine(LOG, "No sources remaining to try");
			//            return ContentTransferState.STALLED;

			/* Another option:*/
			LOG.fine("Resetting remaining/tried lists");
			sourcesRemaining.addAll(sourcesTried);
			sourcesTried.clear();

		}



		while(pipesManager.size()<MAX_OUTSTANDING_PIPE) {
			DefaultContentShareAdvertisementImpl adv= null;
			synchronized (sourcesRemaining) {
				
			
			if (sourcesRemaining.size() <= 0) {
				break;
			}

			adv = sourcesRemaining.remove(0);
			
			sourcesTried.add(adv);
			if(adv.getPipeAdvertisement().getID().equals(((SyncContentProvider)this.getContentProvider()).getPipeID())){
				continue;
			}
			
			}

			boolean exist = false;
			try {
				criticalEntry();

				for(int i = 0; i < pipesManager.size(); i++){
					if(pipesManager.get(i).getID().equals(adv.getPipeAdvertisement().getID())){
						exist=true;
						break;
					}
				}
				if(exist)
					continue;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}finally{
				criticalExit();
			}

			PipeManager pipeM ;
			try {
				Log.d("SyncFolderTransfer", "nouveau Pipe" +adv.getPipeAdvertisement().getPipeID());
				pipeM = new PipeManager(this, adv.getPipeAdvertisement(), peerGroup);
				pipeM.init();
			} catch (Exception e) {
				pipeM = null;
			}
			if(pipeM!=null){
				pipesManager.add(pipeM);
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		super.cancel();
		synchronized(this) {
			running = false;
			try {
				criticalEntry();

				periodicTask.cancel(false);
				periodicTask = null;

				for (PipeManager pipeM : pipesManager) {
					pipeM.clear();

				}
				pipesManager.clear();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				criticalExit();
			}


		}
	}





	/**
	 * Notify listeners of a change in the source location state.
	 */
	public void fireTransferProgress(long received) {
		ContentTransferEvent event = null;
		for (ContentTransferListener listener : getContentTransferListeners()) {
			if (event == null) {
				event = new ContentTransferEvent.Builder(this)
				.bytesReceived(received)
				.build();
			}
			listener.contentTransferProgress(event);
		}
	}

	/**
	 * Ensures that the transfer stays healthy.
	 */
	private void periodicCheck(){
		for (int i = 0; i < pipesManager.size(); i++) {
			pipesManager.get(i).periodicCheck();
		}

	}






	/**
	 * Used by the periodic execution and message processing threads to
	 * protect access to shared code to help minimize the amount of
	 * synchronization required.  This method blocks until the executing
	 * thread has become the critical section owner.
	 */
	public void criticalEntry() throws InterruptedException {

		Thread me = Thread.currentThread();

		synchronized(this) {

			while (ownerThread != null && ownerThread != me) {
				Logging.logCheckedFinest(LOG, "Waiting for access to critical section");
				wait();
			}

			ownerThread = me;

		}

		Logging.logCheckedFinest(LOG, "Access to critical section granted");

	}

	/**
	 * Used by the periodic execution and message processing threads to
	 * protect access to shared code to help minimize the amount of
	 * synchronizatoin required.  This method releases the lock on the
	 * shared/critical code section.
	 */
	public void criticalExit() {

		Logging.logCheckedFinest(LOG, "Releasing access to critical section");

		Thread me = Thread.currentThread();

		synchronized(this) {
			if (ownerThread == me) {
				ownerThread = null;
				notifyAll();
			}
		}

	}


	public boolean isRunning() {
		return running;
	}

	public List<String> getNeededFilesHash() {
		ArrayList<ClassicFile> fTD = filesInfoManager.getFilesToDownload(sharedFolderUID);
		List<String> filesHash = new ArrayList<String>();
		for (ClassicFile fileToDownload : fTD) {
			filesHash.add(fileToDownload.getFileHash());
		}
		return filesHash;
	}

}
