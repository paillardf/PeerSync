package com.peersync.network.content.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import com.peersync.network.content.ContentSecurity;
import com.peersync.network.content.SyncContentProvider;
import com.peersync.network.content.message.AbstractSyncMessage;
import com.peersync.network.content.message.AvailabilityRequestMessage;
import com.peersync.network.content.message.AvailabilityResponseMessage;
import com.peersync.network.content.message.DataRequestMessage;
import com.peersync.network.content.message.DataResponseMessage;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.network.content.model.SegmentToDownload;
import com.peersync.tools.Log;
import com.peersync.tools.KeyStoreManager;

class PipeManager implements PipeMsgListener{

	/**
	 * Logger instance.
	 */
	private final Logger LOG =
			Logger.getLogger(PipeManager.class.getName());


	class PipeQuery{
		private long lastAccess = System.currentTimeMillis();


		private AbstractSyncMessage message;

		public PipeQuery(AbstractSyncMessage message) {
			this.message = message;

		}

		public synchronized boolean isIdle() {
			return (System.currentTimeMillis() - lastAccess) > PIPE_QUERY_TIMEOUT;
		}

		public String getType() {
			return message.getTagRoot();
		}

		public void cancel() {
			if(getType().equals(DataRequestMessage.tagRoot)){
				syncFolderTranfert.filesInfoManager.cancelBookFileSegment((DataRequestMessage)message);
			}

		}
	}



	private static final int PIPE_ACTIVITY_TIMEOUT =
			Integer.getInteger(PipeManager.class.getName()
					+ ".activity", 20 * 1000).intValue();
	private static final int PIPE_QUERY_TIMEOUT =
			Integer.getInteger(PipeManager.class.getName()
					+ ".activity", 20 * 1000).intValue();

	private static final long PIPE_TIMEOUT =
			Long.getLong(SyncFolderTransfer.class.getName()
					+ ".pipeTimeout", 10).longValue() * 1000;

	private static final int FILE_AVAILABILITY_TIMEOUT =
			Integer.getInteger(PipeManager.class.getName()
					+ ".activity", 60 * 1000).intValue();

	/**
	 * Maximum number of outstanding requests by pipe.
	 */
	private static final int MAX_OUTSTANDING =
			Integer.getInteger(PipeManager.class.getName()
					+ ".maxOutstanding", 2).intValue();
	/**
	 * Maximum incoming message queue size.  Once full, additional incoming
	 * requests will be dropped.
	 */
	private static final int MAX_QUEUE_SIZE =
			Integer.getInteger(SyncFolderTransfer.class.getName()
					+ ".maxQueue", SyncFolderTransfer.MAX_OUTSTANDING_PIPE * MAX_OUTSTANDING * 2).intValue();



	private long lastActivity = System.currentTimeMillis();
	private long lastFileAvailabilityUpdate = 0;
	private List<PipeQuery> pipeQueries = new ArrayList<PipeQuery>();
	//private List<FileAvailability> fileAvailability = new ArrayList<FileAvailability>();

	private int queryID = 0;
	private SyncFolderTransfer syncFolderTranfert;
	private PipeAdvertisement pipeAdv;
	private PeerGroup peerGroup;
	private PipeAdvertisement responsePipeAdv;
	private InputPipe responsePipe;
	private final BlockingQueue<PipeMsgEvent> msgQueue =
			new ArrayBlockingQueue<PipeMsgEvent>(MAX_QUEUE_SIZE);
	private OutputPipe outputPipe;
	private boolean running = false;


	public PipeManager(SyncFolderTransfer syncFolderTranfert, PipeAdvertisement pipeAdv, PeerGroup peerGroup) {
		this.syncFolderTranfert = syncFolderTranfert;
		this.pipeAdv = pipeAdv;
		this.peerGroup = peerGroup;
	}

	public void init() throws Exception{
		if(!setupOutputPipe()||!setupResponsePipe())
			throw new Exception("Pipe Manager can't create needed Pipe");

		running = true;
	}

	public boolean isRunning() {
		return running;
	}

	private boolean setupOutputPipe() {
		try{

			PipeService pipeService = peerGroup.getPipeService();
			outputPipe = pipeService.createOutputPipe(
					pipeAdv, PIPE_TIMEOUT);

		} catch (IOException iox) {

			Logging.logCheckedWarning(LOG, "Could not resolve source pipe for Source: ",		pipeAdv, iox);
			return false;

		}
		return true;

	}

	private boolean setupResponsePipe() {
		PipeService pipeService;
		PipeID pipeID;



		if (responsePipeAdv == null) {
			responsePipeAdv =
					(PipeAdvertisement) AdvertisementFactory.newAdvertisement(
							PipeAdvertisement.getAdvertisementType());
			responsePipeAdv.setType(PipeService.UnicastType); //TODO

			pipeID = IDFactory.newPipeID(peerGroup.getPeerGroupID());
			responsePipeAdv.setPipeID(pipeID);
		}


		if (responsePipe == null) {

			try {

				pipeService = peerGroup.getPipeService();
				responsePipe = pipeService.createInputPipe(responsePipeAdv, this);

			} catch (IOException iox) {

				Logging.logCheckedWarning(LOG, "Could not create input pipe\n", iox);
				responsePipe = null;
				return false;
			}

		}
		return true;
	}


	public synchronized boolean isIdle() {
		return (System.currentTimeMillis() - lastActivity) > PIPE_ACTIVITY_TIMEOUT;
	}

	public synchronized boolean needsUpdate() {
		synchronized (this) {
			return (msgQueue.size()>0||canQuery());			
		}
	}

	synchronized boolean needsFileAvailabilityUpdate() {
		return (System.currentTimeMillis() - lastFileAvailabilityUpdate) > FILE_AVAILABILITY_TIMEOUT;
	}

	private boolean canQuery() {
		return pipeQueries.size()<MAX_OUTSTANDING;
	}

	private boolean queryFileAvailabilityRunning() {
		for (PipeQuery query : pipeQueries) {
			if(query.getType().equals(AvailabilityRequestMessage.tagRoot))
				return true;
		}
		return false;
	}
	private int nextQueryId() {
		return queryID++;
	}
	private void queryGC() {
		synchronized (this) {
			for (int i = pipeQueries.size()-1; i >= + pipeQueries.size(); i--) {
				if(pipeQueries.get(i).isIdle()){
					pipeQueries.get(i).cancel();
					pipeQueries.remove(i);
				}
			}
		}

	}

	private AbstractSyncMessage getRequest(){
		AbstractSyncMessage message = null;

		if(canQuery()&&!queryFileAvailabilityRunning()){
			if(needsFileAvailabilityUpdate()){
				message = createFileAvailabilityMessage();
			}else{
				SegmentToDownload bs = getBestSegment();
				if(bs!=null){
					message = createDataRequestMessage(bs);
				}

			}
		}
		if(message!=null){
			lastActivity = System.currentTimeMillis();
		}

		return message;
	}

	private SegmentToDownload getBestSegment() {
		return syncFolderTranfert.filesInfoManager.getBestFileAvailability(syncFolderTranfert.sharedFolderUID ,getID());
	}



	private DataRequestMessage createDataRequestMessage(SegmentToDownload bs) {
		DataRequestMessage req = new DataRequestMessage(bs.getHash(),nextQueryId(), bs.getSegment().offset, bs.getSegment().length, responsePipeAdv);
		return req;
	}



	private AvailabilityRequestMessage createFileAvailabilityMessage() {
		AvailabilityRequestMessage arm = new AvailabilityRequestMessage();
		arm.setQueryID(nextQueryId());
		arm.setFilesHash(syncFolderTranfert.getNeededFilesHash());
		arm.setResponsePipe(responsePipeAdv);
		return arm;
	}

	/**
	 * Reentrant method used for multi-threaded processing of incoming
	 * messages.
	 *
	 * @throws InterruptedException if interupted while waiting for incoming
	 *  messages
	 */
	private void processMessages(){

		List<PipeMsgEvent> workQueue = new ArrayList<PipeMsgEvent>();
		Message msg;


		workQueue.clear();
		synchronized(this) {
			int count = msgQueue.drainTo(workQueue);
			if (count == 0) {
				return;
			}
		}

		for (PipeMsgEvent pme : workQueue) {

			msg = pme.getMessage();


			try {

				processMessage(msg);

			} catch (Exception x) {
				x.printStackTrace();
			}

		}
		lastActivity = System.currentTimeMillis();
	}


	/**
	 * Processes incoming Content service responses.
	 *
	 * @param msg message received
	 */
	private void processMessage(Message msg) {
		MessageElement msge;
		ListIterator it;
		XMLElement doc;



		it = msg.getMessageElementsOfNamespace(SyncContentProvider.MSG_NAMESPACE);

		if (!it.hasNext()) {

			Logging.logCheckedWarning(LOG, "Unknown message structure");
			return;

		}

		msge = (MessageElement) it.next();



		if (!SyncContentProvider.MSG_ELEM_NAME.equals(msge.getElementName())) {

			Logging.logCheckedWarning(LOG, "Not a response: ", msge.getElementName());

			// Not a response
			return;

		}

		StructuredDocument root;
		try {
			root = StructuredDocumentFactory.newStructuredDocument(msge);


			if(!XMLElement.class.isInstance(root)) {
				throw new IllegalArgumentException(getClass().getName() +
						" only supports XMLElement");
			}
			doc = (XMLElement) root;
			AbstractSyncMessage req;
			byte[] data =null;
			if (doc.getName().equals(DataResponseMessage.tagRoot)) {
				req = new DataResponseMessage(doc);
				ByteArrayMessageElement bmsge = (ByteArrayMessageElement) it.next();
				data = bmsge.getBytes();
			}else if(doc.getName().equals(AvailabilityResponseMessage.tagRoot)){
				req = new AvailabilityResponseMessage(doc);
			}else{
				throw new IllegalArgumentException(getClass().getName() +
						" doesn't support this request");
			}

			PipeQuery pipeQ = null;
			for (int i = 0; i < pipeQueries.size(); i++) {
				if(pipeQueries.get(i).message.getQueryID()==req.getQueryID()){
					pipeQ = pipeQueries.get(i);
					break;
				}
			}
			if(pipeQ==null)
				return;


			processResponse(req, data);
			data = null;
			System.gc();
			pipeQueries.remove(pipeQ);
		} catch (IOException e) {
			new IllegalArgumentException(getClass().getName() +
					" only supports XMLElement");
		}
	}

	private void processResponse(AbstractSyncMessage req, byte[] data) {

		if(req instanceof DataResponseMessage){
			processDataResponse((DataResponseMessage) req, data);
		}else if(req instanceof AvailabilityResponseMessage){
			processAvailabilityResponse((AvailabilityResponseMessage) req);
		}

	}

	private void processAvailabilityResponse(AvailabilityResponseMessage req) {
		lastFileAvailabilityUpdate = System.currentTimeMillis();
		syncFolderTranfert.filesInfoManager.addFilesAvailability(req.getFilesAvailability(), getID());

	}

	private void processDataResponse(DataResponseMessage req, byte[] data) {



		try {
			byte[] decBytes = ContentSecurity.decrypt(data, KeyStoreManager.getInstance().getPrivateKey(peerGroup.getPeerGroupID().toString(),KeyStoreManager.MyKeyStorePassword.toCharArray()));


			FileAvailability fAvUpdate = req.getFileAvailability();
			syncFolderTranfert.filesInfoManager.addFileAvailability(fAvUpdate, getID());
			if(req.getQueryID()%10==0)
				Log.d(getClass().getSimpleName(), "from " + req.getOffset() +" to "+ (int) req.getLength());
			syncFolderTranfert.filesInfoManager.writeDownloadedSegment(fAvUpdate.getHash(), req.getOffset(), (int) decBytes.length, decBytes);
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	private void sendRequest(AbstractSyncMessage req) {

		XMLDocument doc = (XMLDocument) req.getDocument(MimeMediaType.XMLUTF8);
		MessageElement msge = new TextDocumentMessageElement(
				SyncContentProvider.MSG_ELEM_NAME, doc, null);
		Message msg = new Message();
		msg.addMessageElement(SyncContentProvider.MSG_NAMESPACE, msge);

		if (Logging.SHOW_FINEST && LOG.isLoggable(Level.FINEST)) {
			Logging.logCheckedFinest(LOG, "Sending Request "+doc);
		}

		try {

			if (outputPipe.send(msg)){
				pipeQueries.add(new PipeQuery(req));
				lastActivity = System.currentTimeMillis();
			}

		} catch (IOException iox) {

			//	Logging.logCheckedWarning(LOG, "IOException during message send\n", iox);

		}


	}



	public void pipeMsgEvent(PipeMsgEvent pme) {
		synchronized(this) {
			if (syncFolderTranfert.isRunning()) {
				msgQueue.offer(pme);
			} else {
				msgQueue.clear();
			}
			synchronized (syncFolderTranfert) {
				syncFolderTranfert.notifyAll();
			}

		}
	}

	public ID getID() {
		return pipeAdv.getPipeID();
	}

	public void clear() {
		synchronized(this) {
			if(outputPipe!=null){
				outputPipe.close();
			}
			if(responsePipe!=null){
				responsePipe.close();
			}
			msgQueue.clear();
			pipeQueries.clear();
			running = false;
		}

	}

	public void periodicCheck() {
		synchronized (this) {
			if(isIdle()){
				clear();
				return;
			}
			queryGC();

		}

	}



	public void update() {
		if(!isRunning())
			return;
		processMessages();
		AbstractSyncMessage req;
		while((req=getRequest())!=null){
			sendRequest(req);
		}

	}








}