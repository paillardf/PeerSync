package com.peersync.network.content.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.jxta.document.Attributable;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;
import net.jxta.impl.document.DOMXMLDocument;
import net.jxta.impl.document.DOMXMLElement;
import net.jxta.impl.document.LiteXMLElement;

public class FileAvailability {
	private static Logger LOG =
			Logger.getLogger(FileAvailability.class.getName());

	private String hash;
	private ArrayList<BytesSegment> segments = new ArrayList<BytesSegment>();


	private static final String tagOffset = "avOffset";
	private static final String tagLength = "avLength";
	private static final String tagAvailability = "FileAvailability";
	
	//TODO : voir la gestion du tag root et du hash
	private static final String tagRoot = "root";



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
				addSegment(Long.parseLong(elemAv.getAttribute(tagOffset).getValue()),
						Long.parseLong(elemAv.getAttribute(tagLength).getValue()));
			}
		}
	}


	public void addSegment(long offset, long length){


		BytesSegment tmp = new BytesSegment(offset,length);
		BytesSegment tmp2 = null;



		for(int i=0;i<segments.size();i++)
		{
			if((tmp2 =segments.get(i).tryToMerge(tmp))!=null)
			{
				do
				{
					tmp=tmp2;
					segments.remove(i);
				}while(segments.size()>i && (tmp2=segments.get(i).tryToMerge(tmp))!=null);
				segments.add(i,tmp);
				return;

			}
			else if(segments.get(i).isSupTo(tmp))
			{
				segments.add(i,tmp);
				return;
			}

		}
		segments.add(tmp);



	}

	
	public void substract(BytesSegment bs){

		int i=0;
		while(i<segments.size())
		{
		
			ArrayList<BytesSegment> tmp = segments.get(i).tryToSubstract(bs);
			if(tmp!=null )
			{
				segments.remove(i);
				for(int j=0;j<tmp.size();j++)
				{
					BytesSegment tmpBs = tmp.get(j);

					if(!tmpBs.isEmpty())
					{
						if(j>0)
							i++;
						segments.add(i,tmpBs);

					}
				}
			}
			i++;
		}
		

	}
	

	public void substract(FileAvailability fa){
		ArrayList<BytesSegment> otherSegment= fa.getSegments();
		int otherSegmentCursor = 0;
		int i=0;
		while(i<segments.size())
		{
			while(otherSegmentCursor+1< otherSegment.size() && !otherSegment.get(otherSegmentCursor).isSupTo(segments.get(i)))
			{
				otherSegmentCursor++;
			}
			ArrayList<BytesSegment> tmp = segments.get(i).tryToSubstract(otherSegment.get(otherSegmentCursor));
			if(tmp!=null )
			{
				segments.remove(i);
				for(int j=0;j<tmp.size();j++)
				{
					BytesSegment bs = tmp.get(j);

					if(!bs.isEmpty())
					{
						if(j>0)
							i++;
						segments.add(i,bs);

					}
				}
			}
			i++;
		}
		

	}







	public ArrayList<BytesSegment> getSegments() {
		// TODO Auto-generated method stub
		return segments;
	}

	public String getHash() {
		return hash;
	}

	public StructuredDocument toXML()
	{
		StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, tagRoot);
		for (int j = 0; j < segments.size(); j++) {
			BytesSegment bs = segments.get(j);
			XMLElement avE = (XMLElement)doc.createElement(tagAvailability);  
			doc.appendChild(avE); 
			avE.addAttribute(tagOffset, ""+bs.offset);
			avE.addAttribute(tagLength,""+ bs.length);
			
		}
		
		//appendSegment(doc, doc);
		return doc;
	}
	
	public void appendSegment(StructuredDocument doc, Element parent) {
		for (int j = 0; j < segments.size(); j++) {
			BytesSegment bs = segments.get(j);
			XMLElement avE = (XMLElement)doc.createElement(tagAvailability);  
			parent.appendChild(avE); 
			avE.addAttribute(tagOffset, ""+bs.offset);
			avE.addAttribute(tagLength,""+ bs.length);
			
		}         

	}



}
