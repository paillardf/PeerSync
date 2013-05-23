package com.peersync.network.content.message;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.logging.Logger;
import net.jxta.logging.Logging;
import net.jxta.content.ContentID;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.IDFactory;
import net.jxta.protocol.PipeAdvertisement;


public abstract class AbstractSyncMessage {
	private static Logger LOG =
			Logger.getLogger(DataRequestMessage.class.getName());
	protected PipeAdvertisement adv;
	protected int qid;


	/**
	 * Default constructor.
	 */
	public AbstractSyncMessage() {
	}

	/**
	 * Build request object from existing XML document.
	 */
	public AbstractSyncMessage(Element root) {
		initialize(root);
	}


	protected abstract boolean handleElement(Element raw);

	protected void initialize(Element root) {
		XMLElement doc = (XMLElement) root;

		if(!XMLElement.class.isInstance(root)) {
			throw new IllegalArgumentException(getClass().getName() +
					" only supports XMLElement");
		}

		if (!doc.getName().equals(getTagRoot())) {
			throw new IllegalArgumentException(
					"Could not construct : " + getClass().getName() +
					"from doc containing a " + doc.getName());
		}

		Enumeration elements = doc.getChildren();
		while (elements.hasMoreElements()) {
			Element elem = (Element) elements.nextElement();

			if (!handleElement(elem)) {
				Logging.logCheckedFine(LOG, "Unhandled Element : ", elem);
			}
		}
	}

	public  abstract String getTagRoot();

	/**
	 * Read in an XML document.
	 */
	public abstract Document getDocument(MimeMediaType asMimeType) ;

	
	/**
     * Sets the query ID of this request.
     */
    public void setQueryID(int qid) {
        this.qid = qid;
    }

    /**
     * Returns query ID of this request.
     */
    public int getQueryID() {
        return qid;
    }

}
