package com.peersync.network.content.model;

import com.peersync.network.content.message.DataResponseMessage;
import com.peersync.network.content.transfer.SyncActiveTransfer;

import net.jxta.content.Content;
import net.jxta.content.ContentShareEvent;
import net.jxta.content.ContentShareEvent.Builder;
import net.jxta.content.ContentShareListener;
import net.jxta.id.ID;
import net.jxta.impl.content.AbstractPipeContentShare;
import net.jxta.impl.content.defprovider.DefaultContentProvider;
import net.jxta.impl.content.defprovider.DefaultContentShareAdvertisementImpl;
import net.jxta.pipe.OutputPipe;
import net.jxta.protocol.ContentAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

/**
 * Implementation of the ContentShare interface for use in the
 * default implementation.  This class implements the bare minimum requirements
 * for a Content share implementation and will likely always need to be
 * extended by the provider implementation to be useful.
 */
public class SyncContentShare extends AbstractPipeContentShare<
    ContentAdvertisement, DefaultContentShareAdvertisementImpl> {

    /**
     * Construct a new DefaultContentShare object, generating a new
     * PipeAdvertisement.
     *
     * @param origin content provider sharing this content
     * @param content content object to share
     * @param pipeAdv pipe used to contact the server
     */
    public SyncContentShare(
            DefaultContentProvider origin, 
            Content content, PipeAdvertisement pipeAdv) {
	super(origin, content, pipeAdv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DefaultContentShareAdvertisementImpl createContentShareAdvertisement() {
        return new DefaultContentShareAdvertisementImpl();
    }

    /**
     * Notify all listeners of this object of a new session being
     * created.
     * 
     * @param session new session being created
     */
    protected void fireShareSessionOpened(SyncActiveTransfer session) {
        ContentShareEvent event = null;
        for (ContentShareListener listener : getContentShareListeners()) {
            if (event == null) {
                event = createEventBuilder(session).build();
            }
            listener.shareSessionOpened(event);
        }
    }

    /**
     * Notify all listeners of this object of an idle session being
     * garbage collected.
     * 
     * @param session session being closed
     */
    protected void fireShareSessionClosed(SyncActiveTransfer session) {
        ContentShareEvent event = null;
        for (ContentShareListener listener : getContentShareListeners()) {
            if (event == null) {
                event = createEventBuilder(session).build();
            }
            listener.shareSessionClosed(event);
        }
    }

    /**
     * Notify all listeners of this object that the share is being
     * accessed.
     * 
     * @param session share being accessed
     * @param resp response to the share access
     */
    protected void fireShareAccessed(
            SyncActiveTransfer session, DataResponseMessage resp) {
        ContentShareEvent event = null;
        for (ContentShareListener listener : getContentShareListeners()) {
            if (event == null) {
                Builder builder = createEventBuilder(session);
                builder.dataStart(resp.getOffset());
                builder.dataSize(resp.getLength());
                event = builder.build();
            }
            listener.shareAccessed(event);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private methods:

    /**
     * Creates and initializes a ContentShareEvent for the session
     * given.
     * 
     * @param session session to create event for
     * @return event object
     */
    private Builder createEventBuilder(SyncActiveTransfer session) {
        Builder result = new Builder(this, session);

        // Name the remote peer by it's pipe ID
        OutputPipe pipe = session.getOutputPipe();
        ID pipeID = pipe.getPipeID();
        result.remoteName(pipeID.toString());

        return result;
    }

}
