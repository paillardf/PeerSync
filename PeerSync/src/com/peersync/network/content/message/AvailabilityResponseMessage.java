package com.peersync.network.content.message;

import java.util.List;
import java.util.logging.Logger;

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

import com.peersync.network.content.model.FileAvailability;

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
 *     &lt;xs:element name="QID"  type="xs:int"
 *       minOccurs="0" maxoccurs="1" />
 *     &lt;xs:element name="EOF"  type="EOFType"
 *       minOccurs="0" maxoccurs="1" />
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class AvailabilityResponseMessage extends AbstractSyncMessage {
	private static Logger LOG =
			Logger.getLogger(AvailabilityResponseMessage.class.getName());
	private static final String tagRoot = "AvailabilityResponse";
	private static final String tagID = "FileHash";

	private static final String tagQueryID = "QID";
	private static final String tagEOF = "EOF";
	private static final String attrReached = "reached";




	private int qid;
	private boolean eofReached;
	private List<FileAvailability> filesAvailability;


	/**
	 * Default constructor.
	 */
	public AvailabilityResponseMessage() {
	}

	/**
	 * Builds response object, initializing values from data found in request.
	 */
	public AvailabilityResponseMessage(AvailabilityRequestMessage req, List<FileAvailability> list) {
		setQueryID(req.getQueryID());
		this.filesAvailability= list;
	}

	/**
	 * Build response object from existing XML document.
	 */
	public AvailabilityResponseMessage(Element root) {
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
		XMLElement elem = (XMLElement) raw;
		Attribute attr;
		ContentID contentId;
		boolean reached;
		int i;
		long l;

		if (elem.getName().equals(tagID)) {
			filesAvailability.add(new FileAvailability(elem));
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

		for (int i = 0; i < filesAvailability.size(); i++) {
			FileAvailability f = filesAvailability.get(i);

			e = doc.createElement(tagID, f.getHash());       	
			doc.appendChild(e);
			f.appendSegment(doc , e);
			
		}
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
	 * Sets the query ID of this response.
	 */
	public void setQueryID(int qid) {
		this.qid = qid;
	}

	/**
	 * Returns query ID of this response.
	 */
	public int getQueryID() {
		return qid;
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

	public List<FileAvailability> getFilesAvailability() {
		return filesAvailability;
	}
}
