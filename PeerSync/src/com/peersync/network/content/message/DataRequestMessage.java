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

/**
 * Implements a Sync Content Data Request Message according to the schema:
 *
 * <p/><pre>
 * &lt;xs:element name="DataRequest" type="DataRequestType"/>
 *
 * &lt;xs:complexType name="DataRequestType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="FileHash" type="xs:string"
 *       minOccurs="1" maxOccurs="1" />
 *     &lt;xs:element name="Offs" type="xs:int"
 *       minOccurs="1" maxOccurs="1" />
 *     &lt;xs:element name="Len"  type="xs:int"
 *       minOccurs="1" maxOccurs="1" />
 *     &lt;xs:element name="PipeAdv"  type="jxta:PipeAdvertisement"
 *       minOccurs="1" maxOccurs="1" />
 *     &lt;xs:element name="QID"  type="xs:int"
 *       minOccurs="0" maxOccurs="1" />
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class DataRequestMessage extends AbstractSyncMessage{

	private static Logger LOG =
			Logger.getLogger(DataRequestMessage.class.getName());
	public static final String tagRoot = "DataRequest";
	private static final String tagID = "FileHash";
	private static final String tagOffs = "Offs";
	private static final String tagLen = "Len";
	private static final String tagQueryID = "QID";
	private static final String tagAdv = "PipeAdv";

	private PipeAdvertisement adv;
	private long offs;
	private int len;
	private String hash;

	/**
	 * Default constructor.
	 */
	public DataRequestMessage() {
	}

	/**
	 * Build request object from existing XML document.
	 */
	public DataRequestMessage(Element root) {
		initialize(root);
	}

	/**
	 *  Process an individual element from the document during parse. Normally,
	 *  implementations will allow the base advertisments a chance to handle the
	 *  element before attempting ot handle the element themselves. ie.
	 *
	 *  <p/><pre><code>
	 *  protected boolean handleElement(Element elem) {
	 *
	 *      if (super.handleElement()) {
	 *           // it's been handled.
	 *           return true;
	 *           }
	 *
	 *      <i>... handle elements here ...</i>
	 *
	 *      // we don't know how to handle the element
	 *      return false;
	 *      }
	 *  </code></pre>
	 *
	 *  @param raw the element to be processed.
	 *  @return true if the element was recognized, otherwise false.
	 **/

	@Override
	protected boolean handleElement(Element raw) {
		PipeAdvertisement pAdv;
		XMLElement elem = (XMLElement) raw;
		ContentID contentId;
		URI uri;
		int i;
		long l;

		if (elem.getName().equals(tagID)) {
			hash = elem.getTextValue();
			return true;
		} else if (elem.getName().equals(tagOffs)) {
			try {
				l = Long.parseLong(elem.getTextValue());
				setOffset(l);
				if (l < 0) {
					throw new IllegalArgumentException("Unusable offset in request");
				}
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Unusable offset in request", nfx);
			}
			return true;
		} else if (elem.getName().equals(tagLen)) {
			try {
				i = Integer.parseInt(elem.getTextValue());
				setLength(i);
				if (i <= 0) {
					throw new IllegalArgumentException("Unusable length in request");
				}
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Unusable length in request", nfx);
			}
			return true;
		} else if (elem.getName().equals(tagQueryID)) {
			try {
				i = Integer.parseInt(elem.getTextValue());
				setQueryID(i);
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException( "Unusable query ID in request" );
			}
			return true;
		} else if (elem.getName().equals(tagAdv)) {
			try {
				XMLDocument xml = (XMLDocument) StructuredDocumentFactory.newStructuredDocument(
						MimeMediaType.XMLUTF8, new StringReader(elem.getTextValue()) );
				pAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(xml);
				setResponsePipe(pAdv);
			} catch ( IOException iox ) {
				throw new IllegalArgumentException(
						"Could not extract PipeAdvertisement from request\n", iox);
			}
			return true;
		}

		// element was not handled
		return false;
	}


	/**
	 * Read in an XML document.
	 */
	@Override
	public Document getDocument(MimeMediaType asMimeType) {
		StructuredTextDocument doc = (StructuredTextDocument)
				StructuredDocumentFactory.newStructuredDocument(asMimeType, tagRoot);
		Element e;

		if (doc instanceof XMLDocument) {
			XMLDocument xmlDoc = (XMLDocument) doc;
			xmlDoc.addAttribute("xmlns:jxta", "http://jxta.org");
		}

		e = doc.createElement(tagID, hash);
		doc.appendChild(e);

		e = doc.createElement(tagOffs, Long.toString(getOffset()));
		doc.appendChild(e);

		e = doc.createElement(tagLen, Integer.toString(getLength()));
		doc.appendChild(e);

		e = doc.createElement(tagQueryID, Integer.toString(getQueryID()));
		doc.appendChild(e);

		e = doc.createElement(tagAdv, getResponsePipe().toString());
		doc.appendChild(e);

		return doc;
	}



	/**
	 * Sets the starting offset of this request.
	 */
	public void setOffset(long offs) {
		this.offs = offs;
	}

	/**
	 * Returns starting offset of this request.
	 */
	public long getOffset() {
		return offs;
	}

	/**
	 * Sets the length of this request.
	 */
	public void setLength(int len) {
		this.len = len;
	}

	/**
	 * Returns length of this request.
	 */
	public int getLength() {
		return len;
	}

	
	/**
	 * Sets the response pipe of this request.
	 */
	public void setResponsePipe(PipeAdvertisement adv) {
		this.adv = adv;
	}

	/**
	 * Returns the response pipe for this request.
	 */
	public PipeAdvertisement getResponsePipe() {
		return adv;
	}

	@Override
	public String getTagRoot() {
		return tagRoot;
	}

	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
}
