package com.peersync.network.content.model;

import com.peersync.network.content.SyncContentProvider;
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
public class SyncFolderShare extends AbstractPipeContentShare<
    ContentAdvertisement, DefaultContentShareAdvertisementImpl> {

    /**
     * Construct a new DefaultContentShare object, generating a new
     * PipeAdvertisement.
     *
     * @param syncContentProvider content provider sharing this content
     * @param content content object to share
     * @param pipeAdv pipe used to contact the server
     */
    public SyncFolderShare(
            SyncContentProvider syncContentProvider, 
            Content content, PipeAdvertisement pipeAdv) {
	super(syncContentProvider, content, pipeAdv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DefaultContentShareAdvertisementImpl createContentShareAdvertisement() {
        return new DefaultContentShareAdvertisementImpl();
    }

}
