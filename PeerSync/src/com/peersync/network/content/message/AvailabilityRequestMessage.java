package com.peersync.network.content.message;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import com.peersync.network.content.model.FileAvailability;

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
import net.jxta.logging.Logging;
import net.jxta.protocol.PipeAdvertisement;


/**
 * Implements a Sync Content Info Request Message according to the schema:
 *
 * <p/><pre>
 * &lt;xs:element name="InfoRequest" type="InfoRequestType"/>
 *
 * &lt;xs:complexType name="InfoRequestType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="ContentID" type="xs:string"
 *       minOccurs="1" maxOccurs="1000" />
 *     &lt;xs:element name="PipeAdv"  type="jxta:PipeAdvertisement"
 *       minOccurs="1" maxOccurs="1" />
 *     &lt;xs:element name="QID"  type="xs:int"
 *       minOccurs="0" maxOccurs="1" />
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class AvailabilityRequestMessage extends AbstractSyncMessage{
    private static Logger LOG =
            Logger.getLogger(AvailabilityRequestMessage.class.getName());
    public static final String tagRoot = "InfoRequest";
    private static final String tagID = "FileHash";
    private static final String tagQueryID = "QID";
    private static final String tagAdv = "PipeAdv";

    
    private List<String> filesHash = new ArrayList<String>();
    
    
    private PipeAdvertisement adv;
    private ContentID id;

    /**
     * Default constructor.
     */
    public AvailabilityRequestMessage() {
    }

    /**
     * Build request object from existing XML document.
     */
    public AvailabilityRequestMessage(Element root) {
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
        int i;


        if (elem.getName().equals(tagID)) {
            String hash = elem.getTextValue();
            filesHash.add(hash);
           
            return true;
        }  else if (elem.getName().equals(tagQueryID)) {
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
    public Document getDocument(MimeMediaType asMimeType) {
        StructuredTextDocument doc = (StructuredTextDocument)
            StructuredDocumentFactory.newStructuredDocument(asMimeType, tagRoot);
        Element e;

        if (doc instanceof XMLDocument) {
            XMLDocument xmlDoc = (XMLDocument) doc;
            xmlDoc.addAttribute("xmlns:jxta", "http://jxta.org");
        }

        for (int i = 0; i < filesHash.size(); i++) {
        	 e = doc.createElement(tagID, filesHash.get(i));
             doc.appendChild(e);
		}
        
        e = doc.createElement(tagQueryID, Integer.toString(getQueryID()));
        doc.appendChild(e);

        e = doc.createElement(tagAdv, getResponsePipe().toString());
        doc.appendChild(e);

        return doc;
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

	public List<String> getFilesHash() {
		return filesHash;
		
	}
	public void setFilesHash(List<String> filesHash) {
		this.filesHash = filesHash;
	}
}
