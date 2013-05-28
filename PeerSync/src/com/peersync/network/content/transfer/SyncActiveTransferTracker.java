package com.peersync.network.content.transfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.processing.FilerException;

import com.peersync.data.DataBaseManager;
import com.peersync.network.content.cache.FileCache;

import net.jxta.impl.content.defprovider.TooManyClientsException;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PipeAdvertisement;

/**
 * This class provides a somewhat efficient mechanism for tracking active
 * transfers and providing a cache for information related to those transfers.
 * As an example, Contents currently only have streamed data accessors.  Requests
 * for data will typically come in a linear fashion, so it makes good
 * sense from a performance perspective to leave the stream open for a period
 * of time.  Additionally, this tracker class puts some absolute limits in
 * place as to how many concurrent clients will be served, giving preference
 * to transfers already in progress.
 */
public class SyncActiveTransferTracker {
	/**
	 * Logger.
	 */
	private static final Logger LOG =
			Logger.getLogger(SyncActiveTransferTracker.class.getName());

	/**
	 * Maximum number of clients to serve concurrently.
	 */
	private static final int MAX_CLIENTS =
			Integer.getInteger(SyncActiveTransferTracker.class.getName()
					+ ".maxClients", 5).intValue();

	/**
	 * Garbage collection interval, in seconds.
	 */
	private static final long GC_INTERVAL =
			Long.getLong(SyncActiveTransferTracker.class.getName()
					+ ".gcInterval", 5).intValue();

	/**
	 *
	 */
	private final List<SyncActiveTransferTrackerListener> listeners =
			new CopyOnWriteArrayList<SyncActiveTransferTrackerListener>();

	/**
	 * PeerGroup to use to resolve pipes.
	 */
	private final PeerGroup group;

	/**
	 * Timer to use when scheduling tasks.
	 */
	private final ScheduledExecutorService schedExec;

	/**
	 * Garbage collection task;
	 */
	private ScheduledFuture gcTask;

	/**
	 * Map of clients being served, keyed off the destination pipe ID.
	 */
	private Map<Object, SyncActiveTransfer> clients =
			new HashMap<Object, SyncActiveTransfer>();

	private Map<Object, FileCache> filesCache =
			new HashMap<Object, FileCache>();

	private DataBaseManager dataBase;

	/**
	 * Constructor.
	 *
	 * @param peerGroup PeerGroup to use to resolve pipes
	 * @param executor executor to submit garbage collection tasks to
	 */
	public SyncActiveTransferTracker(
			PeerGroup peerGroup,
			ScheduledExecutorService executor,
			DataBaseManager database) {
		dataBase = database;
		group = peerGroup;
		schedExec = executor;
	}

	//////////////////////////////////////////////////////////////////////////
	// Public methods:

	/**
	 * Adds an tracker listener to notify of interesting events.
	 * 
	 * @param listener listener to add
	 */
	public void addActiveTransferListener(
			SyncActiveTransferTrackerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a transfer listener, preventin further event
	 * notifications.
	 * 
	 * @param listener listener to add
	 */
	public void removeActiveTransferListener(
			SyncActiveTransferTrackerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Creates and/or retrieves the active transfer session for the
	 * provided combination of share and destination.
	 *
	 * @param share content share to serve to the destination
	 * @param destination pipe to send data to
	 * @return trnsfer session object
	 */
	public SyncActiveTransfer getSession(PipeAdvertisement destination)
			throws TooManyClientsException, IOException {
		Object key = destination.getPipeID().getUniqueValue();
		SyncActiveTransfer result;
		boolean newSession = false;
		synchronized(this) {
			result = clients.get(key);
			if (result == null) {
				if (clients.size() < MAX_CLIENTS) {
					result = new SyncActiveTransfer(group, destination);
					newSession = true;
					clients.put(key, result);
					Logging.logCheckedFine(LOG, "Added client node: ", key);
				}
			}
		}

		// Too many clients to serve this request.
		if (result == null) {
			Logging.logCheckedFine(LOG, "Cound not add client node.  Too many clients.");
			throw(new TooManyClientsException());
		}

		// Notify listners
		if (newSession) fireSessionCreated(result);

		return result;
	}


//	public int getData(String hash, long offset, int length,
//			ByteArrayOutputStream byteOut) throws IOException {
//
//		FileCache result = null;
//		synchronized(this) {
//			result = filesCache.get(hash);
//			if (result == null) {
//				//TODO voir si on peut pas faire mieux
//				String path = dataBase.getSharedFileAvailability(hash).getAbsPath();
//				File f = new File(path);
//				result = new FileCache(f);
//				filesCache.put(hash, result);
//			}
//		}
//		
//
//		return result.getData(offset, length, byteOut);
//	}


	/**
	 * Start tracking and serving.
	 */
	public synchronized void start() {

		if (gcTask == null || gcTask.isDone()) {

			Logging.logCheckedFine(LOG, "Starting GC task");

			gcTask = schedExec.scheduleAtFixedRate(new Runnable() {
				public void run() {
					clientGC();
					cacheGC();
				}
			}, 0, GC_INTERVAL, TimeUnit.SECONDS);
		}
	}

	/**
	 * Stop tracking and serving, freeing any resources held.
	 */
	public void stop() {

		List<SyncActiveTransfer> toNotify = new ArrayList<SyncActiveTransfer>();

		synchronized(this) {

			for (Entry<Object, SyncActiveTransfer> entry : clients.entrySet()) {

				SyncActiveTransfer session = entry.getValue();
				Logging.logCheckedFine(LOG, "Closing client session: ", entry.getKey());

				try {
					session.close();
				} catch (IOException iox) {
					Logging.logCheckedFinest(LOG, "Ignoring exception\n", iox);
				}
				toNotify.add(session);
			}
			clients.clear();

			try {
				if (gcTask != null) {
					Logging.logCheckedFine(LOG, "Stopping GC task");
					gcTask.cancel(false);
				}
			} catch (IllegalStateException isx) {
				Logging.logCheckedFinest(LOG, "Ignoring exception\n" + isx);
			} finally {
				gcTask = null;
			}

		}

		// Notify listeners
		for (SyncActiveTransfer transfer : toNotify) {
			fireSessionCollected(transfer);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Private methods:

	/**
	 * Periodic cleanup task to remove any inactive clients.
	 */
	private void clientGC() {

		Iterator<Map.Entry<Object, SyncActiveTransfer>> it;
		Map.Entry<Object, SyncActiveTransfer> entry;
		List<SyncActiveTransfer> toNotify = null;
		SyncActiveTransfer session;

		Logging.logCheckedFinest(LOG, "clientGC");

		synchronized(this) {

			it = clients.entrySet().iterator();

			while (it.hasNext()) {

				entry = it.next();
				session = entry.getValue();

				if (session.isIdle()) {

					Logging.logCheckedFine(LOG, "Closing client session: ", entry.getKey());

					try {
						session.close();
					} catch (IOException iox) {
						Logging.logCheckedFinest(LOG, "Ignoring exception\n", iox);
					}

					if (toNotify == null) {
						toNotify = new ArrayList<SyncActiveTransfer>();
						toNotify.add(session);
					}

					it.remove();
				}
			}
		}

		// Notify listeners
		if (toNotify != null) {
			for (SyncActiveTransfer transfer : toNotify) {
				fireSessionCollected(transfer);
			}
		}
	}
	
	private void cacheGC() {

		Iterator<Entry<Object, FileCache>> it;
		Map.Entry<Object, FileCache> entry;
		FileCache cache;

		Logging.logCheckedFinest(LOG, "cacheGC");

		synchronized(this) {

			it = filesCache.entrySet().iterator();

			while (it.hasNext()) {

				entry = it.next();
				cache = entry.getValue();

				if (cache.isIdle()) {

					Logging.logCheckedFine(LOG, "Closing file cache: ", entry.getKey());

					try {
						cache.close();
					} catch (IOException iox) {
						Logging.logCheckedFinest(LOG, "Ignoring exception\n", iox);
					}

					it.remove();
				}
			}
		}
	}

	/**
	 * Notify all listeners that a new session has been created.
	 * 
	 * @param transfer the new session
	 */
	private void fireSessionCreated(SyncActiveTransfer transfer) {
		for (SyncActiveTransferTrackerListener listener : listeners) {
			listener.sessionCreated(transfer);
		}
	}

	/**
	 * Notify all listeners that an idle session has been garbage
	 * collected.
	 * 
	 * @param transfer the idle session
	 */
	private void fireSessionCollected(SyncActiveTransfer transfer) {
		for (SyncActiveTransferTrackerListener listener : listeners) {
			listener.sessionCollected(transfer);
		}
	}

}
