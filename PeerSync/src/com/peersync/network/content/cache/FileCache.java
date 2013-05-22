package com.peersync.network.content.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.logging.Logger;

import com.peersync.network.content.transfer.SyncActiveTransfer;

import net.jxta.impl.content.defprovider.RecoveryWindow;
import net.jxta.logging.Logging;

public class FileCache {

	private static final Logger LOG =
			Logger.getLogger(FileCache.class.getName());

	/**
	 * Maximum number of bytes per node.
	 */
	private static final int maxNodeLength =
			Integer.getInteger(RecoveryWindow.class.getName()
					+ ".maxNodeLength", 10 * 1024).intValue();

	private static final long CLIENT_TIMEOUT = 
			Integer.getInteger(FileCache.class.getName()
					+ ".pipeTimeout", 45).intValue() * 1000;

	private InputStream in;
	private Node head;

	private File file;

	private long lastAccess  = System.currentTimeMillis();

	/**
	 * Node to be used in unidirectional, soft referenced, linked list.
	 */
	private static class Node {
		public long offset;
		public byte[] data;
		public Reference previous;
		public Node next;
	}

	/**
	 * Creates a new recovery window, wrapping the source stream provided.
	 */
	public FileCache(File f) {
		this.file = f;
	}

	/**
	 * Returns the node which is at or before the starting offset supplied.
	 * If the source stream has not yet read the entire span of data requested,
	 * the source stream will be read, creating nodes along the way.  If the
	 * beginning of the data is no longer available, an IOException will be
	 * thrown.
	 */
	public synchronized int getData(long offset, int length, OutputStream out)
			throws IOException {
		if(in==null){
			in = new FileInputStream(file);
		}
		Node node = head;
		Node last = null;
		byte[] tooBig;
		int adjustedLen;
		int idx;
		int len;
		int read;
		int totalRead;
		int written = 0;

		Logging.logCheckedFiner(LOG, "Data request: offset=", offset, ", length=", length);

		try {

			// Walk backwards through exiting nodes, looking for an appropriate
			// starting point.
			if (head != null) {

				Logging.logCheckedFinest(LOG, "Walking backwards to find starting node");

				idx=0;

				while (node != null) {

					Logging.logCheckedFinest(LOG, "Now at node: offset=", node.offset, ", length=", node.data.length);

					if (node.offset <= offset) {
						// Beginning of chain found.
						break;
					}

					idx++;
					node = (Node) node.previous.get();

				}

				if (node == null) {

					// Cannot recover data that far back.
					Logging.logCheckedFinest(LOG, "Data requested extends beyond recovery window");
					throw(new IOException("Data requested extends beyond recovery window"));

				}

				Logging.logCheckedFinest(LOG, "Walked backwards ", idx, " nodes from head");

			}

			// Walk forwards through nodes that we do have
			Logging.logCheckedFinest(LOG, "Beginning forward walk");

			while (node != null) {

				Logging.logCheckedFinest(LOG, "Now at node: offset=", node.offset, ", length=", node.data.length);

				idx = (int) (offset - node.offset);
				adjustedLen = node.data.length - idx;
				len = (adjustedLen > length) ? length : adjustedLen;

				if (len > 0) {

					Logging.logCheckedFinest(LOG, "Writing: idx=", idx, ", len=", len );

					out.write(node.data, idx, len);
					written += len;
					offset += len;
					length -= len;

				}

				if (length == 0) {

					// No more data is required.
					// Already, know you, that which you need.
					Logging.logCheckedFinest(LOG, "Request fulfilled.  written=", written);

					return written;

				}

				if (node.next == node) {

					// This is EOF.  We're done.
					Logging.logCheckedFinest(LOG, "EOF encountered.  written=", -written);

					return -written;

				}

				last = node;
				node = node.next;

			}

			// Now try to read the data we dont have
			// Walk forwards through nodes that we do have
			Logging.logCheckedFinest(LOG, "Beginning new data reads");

			while (length > 0) {

				// Figure out where this node starts and ends
				idx = (int) ((last == null) ? 0 : (last.offset + last.data.length));

				if (idx < offset) {

					// We need to save and pass data before the requested position
					len = (int) (offset - idx);

				} else {

					// We can collect the desired data
					len = length;

				}

				if (len > maxNodeLength) {
					len = maxNodeLength;
				}

				Logging.logCheckedFinest(LOG, "Now at: offset=", idx, ", length=", len);

				// Allocate and link the new node
				node = new Node();
				node.data = new byte[len];
				node.offset = idx;

				if (last != null) {
					node.previous = new SoftReference<Node>(last);
					last.next = node;
				}

				head = node;

				Logging.logCheckedFinest(LOG, "Allocated new data node.  offset=", node.offset,
						", length=", node.data.length);

				// Read in the node's data
				totalRead = 0;

				while (totalRead < node.data.length) {

					read = in.read(node.data, totalRead, node.data.length - totalRead);

					if (read < 0) {

						// EOF.  Create resized copy of data and stop.
						tooBig = node.data;
						node.data = new byte[totalRead];
						System.arraycopy(tooBig, 0, node.data, 0, totalRead);
						node.next = node;

						Logging.logCheckedFinest(LOG, "Reallocating node.  offset=", node.offset,
								", len=", node.data.length);

					} else {

						totalRead += read;

					}

				}

				// Write the node's data
				if (idx == offset) {

					Logging.logCheckedFinest(LOG, "Writing node data.  offset=", node.offset,
							", len=", node.data.length);

					out.write(node.data);
					written += node.data.length;
					offset += node.data.length;
					length -= node.data.length;

				}

				if (node.next == node) {

					// This was EOF.  We're done.
					Logging.logCheckedFinest(LOG, "EOF encountered.  written=", -written);
					return -written;

				}

				last = node;

			}

			Logging.logCheckedFinest(LOG, "Request fulfilled.  written=", written);

		} catch (Exception x) {

			Logging.logCheckedFine(LOG, "Uncaught exception\n", x);

		}
		return written;
	}

	/**
	 * Releases all resources.
	 */
	public void close() throws IOException {
		try {
			head = null;
			in.close();
		} finally {
			in = null;
		}
	}

	/**
	 * Determines whether or not this session has been idle for too long.
	 *
	 * @return true if the session is idle, false if it has been reasonably
	 *  active
	 */
	public synchronized boolean isIdle() {
		return (System.currentTimeMillis() - lastAccess) > CLIENT_TIMEOUT;
	}



}
