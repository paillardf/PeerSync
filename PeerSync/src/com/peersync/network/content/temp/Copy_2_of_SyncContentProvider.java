package com.peersync.network.content.temp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.content.defprovider.ActiveTransfer;
import net.jxta.impl.content.defprovider.ActiveTransferTracker;
import net.jxta.impl.content.defprovider.ActiveTransferTrackerListener;
import net.jxta.impl.content.defprovider.DataRequest;
import net.jxta.impl.content.defprovider.DataResponse;
import net.jxta.impl.content.defprovider.DefaultContentShare;
import net.jxta.impl.content.defprovider.TooManyClientsException;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ContentShareAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

public class Copy_2_of_SyncContentProvider implements
ContentProviderSPI, PipeMsgListener, ActiveTransferTrackerListener{

	/**
	 * Logger instance.
	 */
	private static final Logger LOG =
			Logger.getLogger(Copy_2_of_SyncContentProvider.class.getName());


	private static final int MAX_QUEUE_SIZE = 
			Integer.getInteger(Copy_2_of_SyncContentProvider.class.getName()
            + ".maxQueue", 256).intValue();
	 /**
     * Message namespace used to identify our message elements.
     */
    protected static final String MSG_NAMESPACE = "PeerSyncCont";
    /**
     * Message element name used to identify our data requests/responses.
     */
    protected static final String MSG_ELEM_NAME = "PeerSyncDR";
	
	private final CopyOnWriteArrayList<ContentProviderListener> listeners =
			new CopyOnWriteArrayList<ContentProviderListener>();
	private final Queue<PipeMsgEvent> msgQueue =
            new ArrayBlockingQueue<PipeMsgEvent>(MAX_QUEUE_SIZE);
	
	private static ModuleSpecID specID;

	

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
	private ActiveTransferTracker tracker;
	private InputPipe requestPipe;
	private boolean running = false;



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

		tracker = new ActiveTransferTracker(group, executor);
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

        /*
         * XXX 20070911 mcumings: We really need to be able to abort all
         * ContentTransfer instances that we've created that are still
         * in-flight.  Right now the ContentTransfers will silently
         * fail if the ScheduledExecutorService is shutdown while the
         * transfer is in-flight.  I don't like the idea of maintaining
         * references to every ContentTransfer instance, but I also don't
         * like the idea of each instance using it's own dedicated thread.
         * Suggestions?
         */

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
        StructuredDocument doc;
        DataRequest req;

        Logging.logCheckedFinest(LOG, "Incoming message:\n", msg.toString(), "\n");

        it = msg.getMessageElementsOfNamespace(MSG_NAMESPACE);

        while (it.hasNext()) {

            msge = (MessageElement) it.next();

            if (!MSG_ELEM_NAME.endsWith(msge.getElementName())) {
                // Not a data request
                continue;
            }

            try {

                doc = StructuredDocumentFactory.newStructuredDocument(msge);
                req = new DataRequest(doc);

            } catch (IOException iox) {

                Logging.logCheckedFine(LOG, "Could not process message\n", iox);
                return;

            }

            Logging.logCheckedFinest(LOG, "Request: ", req.getDocument(MimeMediaType.XMLUTF8));
            processDataRequest(req);

        }

    }
    
    /**
     * Processes an incoming data request.
     */
    private void processDataRequest(DataRequest req) {

        ByteArrayOutputStream byteOut = null;
        DataResponse resp;
        DefaultContentShare share;
        int written;

        Logging.logCheckedFinest(LOG, "DataRequest:");
        Logging.logCheckedFinest(LOG, "   ContentID: ", req.getContentID());
        Logging.logCheckedFinest(LOG, "   Offset : ", req.getOffset());
        Logging.logCheckedFinest(LOG, "   Length : ", req.getLength());
        Logging.logCheckedFinest(LOG, "   QID    : ", req.getQueryID());
        Logging.logCheckedFinest(LOG, "   PipeAdv: ", req.getResponsePipe());

        share = getShare(req.getContentID());

        if (share == null) {

            Logging.logCheckedWarning(LOG, "Content not shared");
            return;

        }

        try {

            ActiveTransfer session = tracker.getSession(
                    share, req.getResponsePipe());
            byteOut = new ByteArrayOutputStream();
            written = session.getData(
                    req.getOffset(), req.getLength(), byteOut);

            // Send response
            resp = new DataResponse(req);
            if (written <= 0) {
                written = -written;
                resp.setEOF(true);
            }
            resp.setLength(written);
            share.fireShareAccessed(session, resp);

            sendDataResponse(resp, session.getOutputPipe(),
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
    private void sendDataResponse(DataResponse resp, OutputPipe destPipe,
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
            if (destPipe.send(msg)) return;
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

	@Override
	public void sessionCreated(ActiveTransfer transfer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionCollected(ActiveTransfer transfer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		// TODO Auto-generated method stub

	}


	private DefaultContentShare getShare(ID id) {
		return null;
		//TODO
	}



	@Override
	public void addContentProviderListener(ContentProviderListener listener) {
		listeners.add(listener);

	}

	@Override
	public void removeContentProviderListener(ContentProviderListener listener) {
		listeners.remove(listener);
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
		Logging.logCheckedFine(LOG, "retrieveContent(", contentID, ")");

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
		DefaultContentShare share;
		synchronized(shares) {
			share = getShare(id);
			if (share == null) {
				share = new DefaultContentShare(this, content, pAdv);
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
		//		 List<ContentShare> shareList = new ArrayList<ContentShare>();
		//
		//	        synchronized(shares) {
		//	            shareList = new ArrayList<ContentShare>(
		//	                    Math.min(maxNum, shares.size()));
		//	            for (ContentShare share: shares.values()) {
		//	                if (shareList.size() >= maxNum) {
		//	                    break;
		//	                }
		//	                shareList.add(share);
		//	            }
		//	        }
		//
		//	        listener.contentSharesFound(
		//	                new ContentProviderEvent.Builder(this, shareList)
		//	                    .lastRecord(true)
		//	                    .build());

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
			name.append(CopyOfSyncContentProvider.class.getName());
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

}
