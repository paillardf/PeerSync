package com.peersync.network.content.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import net.jxta.document.Element;
import net.jxta.document.StructuredDocument;
import net.jxta.document.XMLElement;

public class FileAvailability {
    private static Logger LOG =
            Logger.getLogger(FileAvailability.class.getName());
    
    private String hash;
    private List<BitesSegment> segments = new ArrayList<BitesSegment>();
    
    
    private static final String tagBegin = "avBegin";
    private static final String tagEnd = "avEnd";
    private static final String tagAvailability = "FileAvailability";

    /**
     * Default constructor.
     */
    public FileAvailability(String hash) {
    	this.hash = hash;
    }

    
    public FileAvailability(XMLElement elem) {
    	this.hash = elem.getTextValue();
    	Enumeration elements = elem.getChildren();
		while (elements.hasMoreElements()) {
			XMLElement elemAv = (XMLElement) elements.nextElement();
			if(elemAv.getName().equals(tagAvailability)){
				addSegment(Long.parseLong(elemAv.getAttribute(tagBegin).getValue()),
						Long.parseLong(elemAv.getAttribute(tagEnd).getValue()));
			}
		}
    }
    
    
    public void addSegment(long begin, long end){
    	//TODO
    }

	public List<BitesSegment> getSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHash() {
		return hash;
	}


	public void appendSegment(StructuredDocument doc, Element parent) {
		for (int j = 0; j < segments.size(); j++) {
			BitesSegment bs = segments.get(j);
			XMLElement avE = (XMLElement)doc.createElement(tagAvailability);    
			avE.addAttribute(tagBegin, ""+bs.begin);
			avE.addAttribute(tagEnd,""+ bs.end);
			parent.appendChild(avE);
		}         
		
	}
    
}
