package com.peersync.network.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.jxta.content.Content;
import net.jxta.content.ContentID;
import net.jxta.content.ContentProviderEvent;
import net.jxta.content.ContentProviderListener;
import net.jxta.content.ContentProviderSPI;
import net.jxta.content.ContentShare;
import net.jxta.content.ContentTransfer;
import net.jxta.document.Advertisement;
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
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.content.defprovider.ActiveTransfer;
import net.jxta.impl.content.defprovider.DefaultContentShare;
import net.jxta.impl.content.defprovider.TooManyClientsException;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ContentShareAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import com.peersync.data.DataBaseManager;
import com.peersync.network.content.listener.ClientEvent;
import com.peersync.network.content.listener.SyncContentProviderListener;
import com.peersync.network.content.message.AbstractSyncMessage;
import com.peersync.network.content.message.AvailabilityRequestMessage;
import com.peersync.network.content.message.AvailabilityResponseMessage;
import com.peersync.network.content.message.DataRequestMessage;
import com.peersync.network.content.message.DataResponseMessage;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.network.content.model.SyncFolderShare;
import com.peersync.network.content.transfer.FilesInfoManager;
import com.peersync.network.content.transfer.SyncActiveTransfer;
import com.peersync.network.content.transfer.SyncActiveTransferTracker;
import com.peersync.network.content.transfer.SyncActiveTransferTrackerListener;

public class SyncContentProvider implements
ContentProviderSPI, PipeMsgListener, SyncActiveTransferTrackerListener{

	/**
	 * Logger instance.
	 */
	private static final Logger LOG =
			Logger.getLogger(SyncContentProvider.class.getName());


	private static final int MAX_QUEUE_SIZE = 
			Integer.getInteger(SyncContentProvider.class.getName()
            + ".maxQueue", 256).intValue();
	 /**
     * Message namespace used to identify our message elements.
     */
    public static final String MSG_NAMESPACE = "PeerSyncCont";
    /**
     * Message element name used to identify our data requests/responses.
     */
    public static final String MSG_ELEM_NAME = "PeerSyncDR";
	
	private final CopyOnWriteArrayList<SyncContentProviderListener> syncListeners =
			new CopyOnWriteArrayList<SyncContentProviderListener>();
	
	private final CopyOnWriteArrayList<ContentProviderListener> listeners =
			new CopyOnWriteArrayList<ContentProviderListener>();
	
	
	private final Queue<PipeMsgEvent> msgQueue =
            new ArrayBlockingQueue<PipeMsgEvent>(MAX_QUEUE_SIZE);
	
	private static ModuleSpecID specID;

	private FilesInfoManager filesInfoManager = new FilesInfoManager();

	 //////////////////////////////////////////////////////////////////////////
    // Constructors and initializers:
	 private static final String MODULE_SPEC_ID =
	            "urn:jxta:uuid-AC3AA08FC4A14C15A78A84B4D4F87554"
	            + "CDC361792F3F4EF2A6488BE56396AAEB99";
    /**
     * Static initializer.
     */
    static {
        try {
            URI specURI = new URI(MODULE_SPEC_ID);
            specID = (ModuleSpecID) IDFactory.fromURI(specURI);
        } catch (URISyntaxException urisx) {
            throw(new RuntimeException(
                    "Illegal ModuleSpecURI in code: " + MODULE_SPEC_ID,
                    urisx));
        }
    }
    
	// Initialized by init
	private PeerGroup peerGroup;
	private ScheduledExecutorService executor;
	private PipeAdvertisement pipeAdv;

	// Initialized and managed by start/stop
	private SyncActiveTransferTracker tracker;
	private InputPipe requestPipe;
	private boolean running = false;


	private DataBaseManager dataBase;


	private final Map<ID, SyncFolderShare> shares =
            new HashMap<ID, SyncFolderShare>();



	@Override
	public void init(PeerGroup group, ID assignedID, Advertisement implAdv)
			throws PeerGroupException {


		Logging.logCheckedFine(LOG, "initProvider(): group=", group);

		peerGroup = group;
		executor = Executors.newScheduledThreadPool(
				5, new ThreadFactoryImpl(group));

		pipeAdv =
				(PipeAdvertisement) AdvertisementFactory.newAdvertisement(
						PipeAdvertisement.getAdvertisementType());
		pipeAdv.setType(PipeService.UnicastType);// TODO SECURE

		PipeID pipeID = IDFactory.newPipeID(peerGroup.getPeerGroupID());
		pipeAdv.setPipeID(pipeID);
		
		dataBase = DataBaseManager.getInstance();
		
		tracker = new SyncActiveTransferTracker(group, executor, dataBase);
		tracker.addActiveTransferListener(this);
		
		


	}

	@Override
	public int startApp(String[] args) {
		 Logging.logCheckedFine(LOG, "startApp()");

	        if (running) return Module.START_OK;

	        running = true;

	        if (requestPipe == null) {

	            try {

	                PipeService pipeService = peerGroup.getPipeService();

	                if (pipeService == null) {

	                    Logging.logCheckedWarning(LOG, "Stalled until there is a pipe service");
	                    return Module.START_AGAIN_STALLED;

	                }

	                requestPipe = pipeService.createInputPipe(pipeAdv, this);

	            } catch (IOException iox) {

	                Logging.logCheckedWarning(LOG, "Could not create input pipe\n", iox);
	                requestPipe = null;
	                return Module.START_AGAIN_STALLED;

	            }
	        }

	        // Start the accept loop
	        executor.execute(new Runnable() {
	            public void run() {
	                try {
	                    processMessages();
	                } catch (InterruptedException intx) {
	                    Logging.logCheckedFine(LOG, "Interrupted\n" + intx);
	                    Thread.interrupted();
	                }
	            }
	        });

	        tracker.start();

	        return Module.START_OK;
	}

	@Override
	public void stopApp() {
		Logging.logCheckedFine(LOG, "stopApp()");

        if (!running) return;

        tracker.stop();
        msgQueue.clear();

        running = false;
        notifyAll();

	}
	
	 /**
     * Reentrant method used for multi-threaded processing of incoming
     * messages.  This method and all those it calls must remain perfectly
     * reentrant.
     */
    private void processMessages() throws InterruptedException {

        PipeMsgEvent pme;
        Message msg;

        Logging.logCheckedFine(LOG, "Worker thread starting");

        while (true) {
            synchronized(this) {
                if (!running) {
                    break;
                }     
                
                pme = msgQueue.poll();
                if (pme == null) {
                    wait();
                    continue;
                }
            }

            try {

                msg = pme.getMessage();
                processMessage(msg);

            } catch (Exception x) {

                Logging.logCheckedWarning(LOG, "Uncaught exception\n", x);

            }
        }

        Logging.logCheckedFine(LOG, "Worker thread closing up shop");

    }
    
    
    /**
     * Process the incoming message.
     */
    private void processMessage(Message msg) {

        MessageElement msge;
        ListIterator it;
        XMLElement doc;
        AbstractSyncMessage req;

        Logging.logCheckedFinest(LOG, "Incoming message:\n", msg.toString(), "\n");

        it = msg.getMessageElementsOfNamespace(MSG_NAMESPACE);

        while (it.hasNext()) {

            msge = (MessageElement) it.next();

            if (!MSG_ELEM_NAME.endsWith(msge.getElementName())) {
                // Not a data request
                continue;
            }

            try {

                StructuredDocument root = StructuredDocumentFactory.newStructuredDocument(msge);
                
                if(!XMLElement.class.isInstance(root)) {
                    throw new IllegalArgumentException(getClass().getName() +
                            " only supports XMLElement");
                }
                doc = (XMLElement) root;
                if (doc.getName().equals(DataRequestMessage.tagRoot)) {
                	req = new DataRequestMessage(doc);
                }else if(doc.getName().equals(AvailabilityRequestMessage.tagRoot)){
                	req = new AvailabilityRequestMessage(doc);
                }else{
                	throw new IllegalArgumentException(getClass().getName() +
                            " doesn't support this request");
                }
                
                
                

            } catch (IOException iox) {

                Logging.logCheckedFine(LOG, "Could not process message\n", iox);
                return;

            }

            Logging.logCheckedFinest(LOG, "Request: ", req.getDocument(MimeMediaType.XMLUTF8));
            processRequest(req);

        }

    }
    
    private void processRequest(AbstractSyncMessage req) {
    	
    	if(req instanceof DataRequestMessage){
    		processDataRequest((DataRequestMessage) req);
    	}else if(req instanceof AvailabilityRequestMessage){
    		processAvailabilityRequest((AvailabilityRequestMessage) req);
    	}
    	
    }
    
    /**
     * Processes an incoming availability request.
     */
    private void processAvailabilityRequest(AvailabilityRequestMessage req) {
    	
    	List<FileAvailability> fileAvailability = new ArrayList<FileAvailability>();
    	
    	for (String hash : req.getFilesHash()) {
    		fileAvailability.add(dataBase.getFileAvailability(hash));
		}
    	
    	AvailabilityResponseMessage resp = new AvailabilityResponseMessage(req,fileAvailability);
    	
    	
    	SyncActiveTransfer session;
		try {
			session = tracker.getSession(req.getResponsePipe());
			sendResponse((AbstractSyncMessage) resp, session, null);
		} catch (TooManyClientsException tmcx) {

            Logging.logCheckedWarning(LOG, "Too many concurrent clients.  Discarding.");

        } catch (IOException iox) {

            Logging.logCheckedWarning(LOG, "Exception while handling data request\n", iox);

        }
    	
    	
    }
    
    
    
    /**
     * Processes an incoming data request.
     */
    private void processDataRequest(DataRequestMessage req) {

        ByteArrayOutputStream byteOut = null;
        DataResponseMessage resp;
        int written;

        Logging.logCheckedFinest(LOG, "DataRequest:");
        Logging.logCheckedFinest(LOG, "   hash: ", req.getHash());
        Logging.logCheckedFinest(LOG, "   Offset : ", req.getOffset());
        Logging.logCheckedFinest(LOG, "   Length : ", req.getLength());
        Logging.logCheckedFinest(LOG, "   QID    : ", req.getQueryID());
        Logging.logCheckedFinest(LOG, "   PipeAdv: ", req.getResponsePipe());

        
        FileAvailability fAv = dataBase.getFileAvailability(req.getHash());

       
        try {

            SyncActiveTransfer session = tracker.getSession(
                    req.getResponsePipe());
            
            byteOut = new ByteArrayOutputStream();
            written = tracker.getData(req.getHash(),
                    req.getOffset(), req.getLength(), byteOut);

            // Send response
            resp = new DataResponseMessage(req,fAv);
            if (written <= 0) {
                written = -written;
                resp.setEOF(true);
            }
            resp.setLength(written);
            
            //TODO share.fireShareAccessed(session, resp);

            sendResponse(resp, session,
                    (written == 0) ? null : byteOut.toByteArray());

        } catch (TooManyClientsException tmcx) {

            Logging.logCheckedWarning(LOG, "Too many concurrent clients.  Discarding.");

        } catch (IOException iox) {

            Logging.logCheckedWarning(LOG, "Exception while handling data request\n", iox);

        }
    }

    /**
     * Sends a response to the destination specified.
     */
    private void sendResponse(AbstractSyncMessage resp, SyncActiveTransfer session,
            byte[] data) {
        MessageElement msge;
        XMLDocument doc;
        Message msg;

        msg = new Message();
        doc = (XMLDocument) resp.getDocument(MimeMediaType.XMLUTF8);
        msge = new TextDocumentMessageElement(MSG_ELEM_NAME, doc, null);
        msg.addMessageElement(MSG_NAMESPACE, msge);

        if (data != null) {
            msge = new ByteArrayMessageElement("data",
                    new MimeMediaType("application", "octet-stream"),
                    data, null);
            msg.addMessageElement(MSG_NAMESPACE, msge);
        }

        Logging.logCheckedFiner(LOG, "Sending response: " + msg);

        try {
            if (session.send(msg)) return;
        } catch (IOException iox) {
            Logging.logCheckedWarning(LOG, "IOException during message send\n", iox);
        }

        Logging.logCheckedFine(LOG, "Did not send message");

    }

    /**
     * Notify our listeners that the provided shares are being exposed.
     * 
     * @param shares list of fresh shares
     */
    private void fireContentShared(List<ContentShare> shares) {
        ContentProviderEvent event = null;
        for (ContentProviderListener listener : listeners) {
            if (event == null) {
                event = new ContentProviderEvent.Builder(this, shares)
                        .build();
            }
            listener.contentShared(event);
        }
    }
    
    /**
     * Notify our listeners that the provided shares are that are no
     * longer being exposed.
     * 
     * @param contentID ContentID of the content which is no longer
     *  being shared
     */
    private void fireContentUnshared(ContentID contentID) {
        ContentProviderEvent event = null;
        for (ContentProviderListener listener : listeners) {
            if (event == null) {
                event = new ContentProviderEvent.Builder(this, contentID)
                        .build();
            }
            listener.contentUnshared(event);
        }
    }

   
    /**
     * Notify our listeners that a new client is connected
     * 
     * @param id  of the client pipe witch was opened
     */
    private void fireClientConnection(ID id) {
    	ClientEvent event = null;
        for (SyncContentProviderListener listener : syncListeners) {
        	
			if(event == null){
        		event = new ClientEvent(this, id);
        	}
           listener.clientConnection(event);
        }
    }
    
    /**
     * Notify our listeners that a client is disconnected
     * 
     * @param id  of the client pipe witch was opened
     */
    private void fireClientDisconnection(ID id) {
    	ClientEvent event = null;
        for (SyncContentProviderListener listener : syncListeners) {
        	
			if(event == null){
        		event = new ClientEvent(this, id);
        	}
           listener.clientDisconnection(event);
        }
    }


	@Override
	public synchronized void pipeMsgEvent(PipeMsgEvent pme) {
        if (!running) {
            return;
        }

        if (msgQueue.offer(pme)) {
            notifyAll();
        } else {
            Logging.logCheckedFine(LOG, "Dropped message due to full queue");
        }
    }


	private SyncFolderShare getShare(ID id) {
		synchronized(shares) {
            return shares.get(id);
        }
	}



	@Override
	public void addContentProviderListener(ContentProviderListener listener) {
		listeners.add(listener);

	}

	@Override
	public void removeContentProviderListener(ContentProviderListener listener) {
		listeners.remove(listener);
	}

	public void addSyncContentProviderListener(SyncContentProviderListener listener) {
		syncListeners.add(listener);

	}

	public void removeSyncContentProviderListener(SyncContentProviderListener listener) {
		syncListeners.remove(listener);
	}
	
	@Override
	public ContentTransfer retrieveContent(ContentID contentID)
			throws UnsupportedOperationException {
		Logging.logCheckedFine(LOG, "retrieveContent(", contentID, ")");

		synchronized(this) {
			if (!running) return null;
		}
		return null;

		//        synchronized(shares) {
		//            ContentShare share = getShare(contentID);
		//            if (share != null) {
		//                return new NullContentTransfer(this, share.getContent());
		//            }
		//        }
		//        return new DefaultContentTransfer(this, executor, peerGroup, contentID);
		//TODO
	}

	@Override
	public ContentTransfer retrieveContent(ContentShareAdvertisement adv) {
	//	Logging.logCheckedFine(LOG, "retrieveContent(", contentID, ")");

		synchronized(this) {
			if (!running) return null;
		}
		//TODO
		return null;
		//        synchronized(shares) {
		//            ContentShare share = getShare(contentID);
		//            if (share != null) {
		//                return new NullContentTransfer(this, share.getContent());
		//            }
		//        }
		//        return new DefaultContentTransfer(this, executor, peerGroup, contentID);
	}

	@Override
	public List<ContentShare> shareContent(Content content)
			throws UnsupportedOperationException {
		Logging.logCheckedFine(LOG, "shareContent(): Content=", content, " ", this);

		PipeAdvertisement pAdv;

		synchronized(this) {
			pAdv = pipeAdv;
		}

		if (pipeAdv == null) {
			Logging.logCheckedFine(LOG, "Cannot create share before initialization");
			return null;
		}

		List<ContentShare> result = new ArrayList<ContentShare>(1);
		ID id = content.getContentID();
		SyncFolderShare share;
		synchronized(shares) {
			share = getShare(id);
			if (share == null) {
				share = new SyncFolderShare(this, content, pAdv);
				shares.put(id, share);
				result.add(share);
			}
		}

		if (result.size() == 0) {
			/*
			 * This content was already shared.  We'll skip notifying our
			 * listeners but will return it in the results.
			 */
			result.add(share);
		} else {
			fireContentShared(result);
		}
		return result;
	}

	@Override
	public boolean unshareContent(ContentID contentID)
			throws UnsupportedOperationException {
		Logging.logCheckedFine(LOG, "unhareContent(): ContentID=", contentID);

		ContentShare oldShare;
		synchronized(shares) {
			oldShare = shares.remove(contentID);
		}
		if (oldShare == null) {
			return false;
		} else {
			fireContentUnshared(contentID);
			return true;
		}
	}

	@Override
	public void findContentShares(int maxNum, ContentProviderListener listener)
			throws UnsupportedOperationException {
				 List<ContentShare> shareList = new ArrayList<ContentShare>();
		
			        synchronized(shares) {
			            shareList = new ArrayList<ContentShare>(
			                    Math.min(maxNum, shares.size()));
			            for (ContentShare share: shares.values()) {
			                if (shareList.size() >= maxNum) {
			                    break;
			                }
			                shareList.add(share);
			            }
			        }
		
			        listener.contentSharesFound(
			                new ContentProviderEvent.Builder(this, shareList)
			                    .lastRecord(true)
			                    .build());

	}

	
    
	@Override
	public Advertisement getImplAdvertisement() {
		ModuleImplAdvertisement adv =
                (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
                ModuleImplAdvertisement.getAdvertisementType());
        adv.setModuleSpecID(specID);
        adv.setCode(getClass().getName());
        adv.setProvider("http://peersync.utc/");
        adv.setDescription("ContentProvider implementation for PeerSync");

        return adv;
	}

	private class ThreadFactoryImpl
	implements ThreadFactory, UncaughtExceptionHandler {
		private ThreadGroup threadGroup;

		public ThreadFactoryImpl(PeerGroup group) {
			StringBuilder name = new StringBuilder();
			name.append(group.getPeerGroupName());
			name.append(" - ");
			name.append(SyncContentProvider.class.getName());
			name.append(" pool");

			threadGroup = new ThreadGroup(name.toString());
			threadGroup.setDaemon(true);
		}

		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(threadGroup, runnable);
			thread.setUncaughtExceptionHandler(this);
			return thread;
		}

		public void uncaughtException(Thread thread, Throwable throwable) {

			Logging.logCheckedSevere(LOG, "Uncaught throwable in pool thread: ", thread, "\n", throwable);

		}
	}

	
	 //////////////////////////////////////////////////////////////////////////
    // ActiveTransferTrackerListener interface methods:

	
	 
    public void sessionCreated(SyncActiveTransfer transfer) {
        ID id = transfer.getOutputPipe().getPipeID();
        fireClientConnection(id);
    }

    
    public void sessionCollected(SyncActiveTransfer transfer) {
    	ID id = transfer.getOutputPipe().getPipeID();
    	fireClientDisconnection(id);
    }

}
