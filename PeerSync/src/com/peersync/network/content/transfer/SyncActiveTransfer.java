package com.peersync.network.content.transfer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.peersync.network.content.model.SyncContentShare;

import net.jxta.content.Content;
import net.jxta.document.Document;
import net.jxta.endpoint.Message;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

/**
 * A node being tracked by the ActiveTransferTracker class.  This class acts
 * as a session object of sorts, maintaining client/request specific
 * information for use in the near future.
 */
public class SyncActiveTransfer {
    /**
     * Amount of time which must elapse before a client will be considered
     * inactive - forfeiting it's slot - in seconds.
     */
    private static final long CLIENT_TIMEOUT =
            Long.getLong(SyncActiveTransfer.class.getName()
            + ".clientTimeout", 45).intValue() * 1000;

    /**
     * Timeout used for output pipe resolution, in seconds.
     */
    private static final int PIPE_TIMEOUT =
            Integer.getInteger(SyncActiveTransfer.class.getName()
            + ".pipeTimeout", 10).intValue() * 1000;

    /**
     * OutputPipe to send response data to.
     */
    private final OutputPipe destPipe;
   
    /**
     * The last time data was requested from this transfer client.
     */
    private long lastAccess = System.currentTimeMillis();

    /**
     * Constructs a new transfer client node.
     */
    public SyncActiveTransfer(
            PeerGroup peerGroup,
            PipeAdvertisement destination) throws IOException {

        // Setup a pipe to the source
        PipeService pipeService = peerGroup.getPipeService();
        destPipe = pipeService.createOutputPipe(destination, PIPE_TIMEOUT);

//        share = toShare;
//        Content content = toShare.getContent();
//        Document doc = content.getDocument();
//        BufferedInputStream in =  new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Attempt to get the data specified for the destination given.
     * @param share 
     *
     * @param offset position in the file of the beginning of the data
     * @param length number of bytes desired
     * @param out stream to write the data to
     * @return negative X when X bytes have been copied and EOF has been
     *      reached, positive X when X bytes have been copied and EOF was
     *      not reached, or 0 if no bytes could be copied.
     * @throws IOException when a problem arises working with IO
     */
//    public synchronized int getData(
//            SyncContentShare share, long offset, int length, OutputStream out)
//            throws IOException {
//        int result;
//
//       // result = window.getData(offset, length, out);
//        //lastAccess = System.currentTimeMillis();
////TODO
//        return result;
//    }

    /**
     * Determines whether or not this session has been idle for too long.
     *
     * @return true if the session is idle, false if it has been reasonably
     *  active
     */
    public synchronized boolean isIdle() {
        return (System.currentTimeMillis() - lastAccess) > CLIENT_TIMEOUT;
    }

    /**
     * Close out this session.
     *
     * @throws IOException when IO problem arises
     */
    public synchronized void close() throws IOException {
        destPipe.close();
    }

//    /**
//     * Gets the output pipe for sending responses back to this client.
//     *
//     * @return output pipe
//     */
//    public OutputPipe getOutputPipe() {
//        return destPipe;
//    }

	public boolean send(Message msg) throws IOException {
		lastAccess = System.currentTimeMillis();
		return destPipe.send(msg);
	}
}
