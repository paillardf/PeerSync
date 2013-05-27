package com.peersync.network.content.message;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.peersync.network.content.model.FileAvailability;

import net.jxta.logging.Logging;
import net.jxta.content.ContentID;
import net.jxta.document.Attribute;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.IDFactory;

/**
 * Implements a Content Data Response Message according to the schema:
 *
 * <p/><pre>
 * &lt;xs:element name="DataResponse" type="DataResponseType"/>
 *
 * &lt;xs:complexType name="EOFType">
 *   &lt;xs:attribute name="reached" type="xs:boolean" />
 * &lt;/xs:complexType>
 *
 * &lt;xs:complexType name="DataResponseType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="FileHash" type="xs:string"
 *       minOccurs="1" maxoccurs="1000" >
 *       &lt;xs:element name="FileAvailability" avBegin="xs:long" avEnd="xs:long" type="xs:string"
 *       minOccurs="1" maxoccurs="1000" />
 *     &lt;/xs:element>
 *     &lt;xs:element name="Offs" type="xs:int"
 *       minOccurs="1" maxoccurs="1" />
 *     &lt;xs:element name="Len"  type="xs:int"
 *       minOccurs="1" maxoccurs="1" />
 *     &lt;xs:element name="QID"  type="xs:int"
 *       minOccurs="0" maxoccurs="1" />
 *     &lt;xs:element name="EOF"  type="EOFType"
 *       minOccurs="0" maxoccurs="1" />
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class DataResponseMessage extends AbstractSyncMessage {
	private static Logger LOG =
			Logger.getLogger(DataResponseMessage.class.getName());
	public static final String tagRoot = "DataResponse";
	private static final String tagOffs = "Offs";
	private static final String tagLen = "Len";
	private static final String tagQueryID = "QID";
	private static final String tagEOF = "EOF";
	private static final String attrReached = "reached";

	private long offs;
	private long len;
	private boolean eofReached;
	private FileAvailability fileAvailability;

	/**
	 * Default constructor.
	 */
	public DataResponseMessage() {
	}

	/**
	 * Builds response object, initializing values from data found in request.
	 */
	public DataResponseMessage(DataRequestMessage req, FileAvailability fAv) {
		fileAvailability = fAv;
		setOffset(req.getOffset());
		setLength(req.getLength());
		setQueryID(req.getQueryID());
	}

	/**
	 * Build response object from existing XML document.
	 */
	public DataResponseMessage(Element root) {
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
	protected boolean handleElement(Element raw) {
		XMLElement elem = (XMLElement) raw;
		Attribute attr;
		boolean reached;
		int i;
		long l;

		if (elem.getName().equals(FileAvailability.tagHash)) {
			fileAvailability = new FileAvailability(elem);
			return true;
		} else if (elem.getName().equals(tagOffs)) {
			try {
				l = Long.parseLong(elem.getTextValue());
				setOffset(l);
				if (l < 0) {
					throw new IllegalArgumentException("Unusable offset in response");
				}
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Unusable offset in response", nfx);
			}
			return true;
		} else if (elem.getName().equals(tagLen)) {
			try {
				i = Integer.parseInt(elem.getTextValue());
				setLength(i);
				if (i < 0) {
					throw new IllegalArgumentException("Unusable length in response");
				}
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Unusable length in response", nfx);
			}
			return true;
		} else if (elem.getName().equals(tagQueryID)) {
			try {
				i = Integer.parseInt(elem.getTextValue());
				setQueryID(i);
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Unusable query ID in response", nfx);
			}
			return true;
		} else if (elem.getName().equals(tagEOF)) {
			attr = elem.getAttribute(attrReached);
			reached = (attr == null) ? true
					: Boolean.parseBoolean(attr.getValue());
			setEOF(reached);
			return true;
		}

		// element was not handled
		return false;
	}



	/**
	 * Read in an XML document.
	 */
	public Document getDocument(MimeMediaType asMimeType) {
		StructuredDocument doc = (StructuredTextDocument)
				StructuredDocumentFactory.newStructuredDocument(asMimeType, tagRoot);
		Attribute attr;
		Element e;

		if (doc instanceof XMLDocument) {
			XMLDocument xmlDoc = (XMLDocument) doc;
			xmlDoc.addAttribute("xmlns:jxta", "http://jxta.org");
		}

		
		fileAvailability.appendSegment(doc);

		e = doc.createElement(tagOffs, Long.toString(getOffset()));
		doc.appendChild(e);

		e = doc.createElement(tagLen, Long.toString(getLength()));
		doc.appendChild(e);

		e = doc.createElement(tagQueryID, Integer.toString(getQueryID()));
		doc.appendChild(e);

		if (getEOF()) {
			e = doc.createElement(tagEOF);
			doc.appendChild(e);
			attr = new Attribute(attrReached, Boolean.toString(getEOF()));
			((XMLElement) e).addAttribute(attr);
		}

		return doc;
	}


	/**
	 * Sets the starting offset of this response.
	 */
	public void setOffset(long offs) {
		this.offs = offs;
	}

	/**
	 * Returns starting offset of this response.
	 */
	public long getOffset() {
		return offs;
	}

	/**
	 * Sets the length of this response.
	 */
	public void setLength(long len) {
		this.len = len;
	}

	/**
	 * Returns length of this response.
	 */
	public long getLength() {
		return len;
	}

	

	/**
	 * Sets the EOF status of this response.
	 */
	public void setEOF(boolean reached) {
		this.eofReached = reached;
	}

	/**
	 * Returns the EOF status for this response.
	 */
	public boolean getEOF() {
		return eofReached;
	}

	@Override
	public String getTagRoot() {
		return tagRoot;
	}
	
	public FileAvailability getFileAvailability() {
		return fileAvailability;
	}
}
