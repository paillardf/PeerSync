/*
 * Copyright (c) 2010 DawningStreams, Inc.  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must 
 *     include the following acknowledgment: "This product includes software 
 *     developed by DawningStreams, Inc." 
 *     Alternately, this acknowledgment may appear in the software itself, if 
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The name "DawningStreams,Inc." must not be used to endorse or promote
 *     products derived from this software without prior written permission.
 *     For written permission, please contact DawningStreams,Inc. at 
 *     http://www.dawningstreams.com.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *  DAWNINGSTREAMS, INC OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  DawningStreams is a registered trademark of DawningStreams, Inc. in the United 
 *  States and other countries.
 *  
 */

package com.peersync.network.advertisment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.id.CBID.CodatID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;

public class RendezVousAdvertisement extends Advertisement {
    
    public static final String Name = "RendezVousAdvertisement";

    // Advertisement elements, tags and indexables
    public final static String AdvertisementType = "jxta:RendezVousAdvertisement";
    
    private ID AdvertisementID = ID.nullID;

	private PeerGroupID peerGroupId;

	private PeerID peerId;

	private long startDate;
    
   // private final static String IDTAG = "advID";
    public final static String PeerGroupIdTAG = "PeerGroupId";
    private final static String PeerIDTAG = "PeerID";
    private final static String StartDateTAG = "StartDate";

	   
    private final static String[] IndexableFields = { PeerGroupIdTAG , PeerIDTAG};
 
    public RendezVousAdvertisement(Element Root) {
       // Retrieving the elements
        TextElement MyTextElement = (TextElement) Root;

        Enumeration TheElements = MyTextElement.getChildren();
        
        
        while (TheElements.hasMoreElements()) {
            
            TextElement TheElement = (TextElement) TheElements.nextElement();
            
            
            String TheElementName = TheElement.getName();
            if(TheElementName.compareTo(PeerGroupIdTAG)==0){
            	 URI ReadID;
				try {
					ReadID = new URI(TheElement.getValue());
				
            	 //this.peerGroupId = (PeerGroupID) IDFactory.fromURI(ReadID);
            	this.peerGroupId = net.jxta.impl.id.UUID.PeerGroupID.create((URI) ReadID);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
            }else if(TheElementName.compareTo(PeerIDTAG)==0){
            	URI ReadID;
				try {
					ReadID = new URI(TheElement.getValue());
				
            	//this.peerGroupId = (PeerGroupID) IDFactory.fromURI(ReadID);
            	this.setPeerID(net.jxta.impl.id.CBID.PeerID.create((URI) ReadID));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
            }else if(TheElementName.compareTo(StartDateTAG)==0){
                this.setStartDate(Long.parseLong(TheElement.getValue()));
            }
//            else if(TheElementName.compareTo(IDTAG)==0){
//
//                try {
//                    
//                    URI ReadID = new URI(TheElement.getValue());
//                    AdvertisementID = IDFactory.fromURI(ReadID);
//                    return;
//                    
//                } catch (URISyntaxException Ex) {
//                    
//                    // Issue with ID format
//                    Ex.printStackTrace();
//                    
//                } catch (ClassCastException Ex) {
//                    
//                    // Issue with ID type
//                    Ex.printStackTrace();
//                    
//                }
//            }
            
        }  

    }
    
    public EndpointAddress getRendezVousAddress(){
    	return new EndpointAddress("jxta", getPeerID().getUniqueValue().toString(), null, null);
    }
	
	public RendezVousAdvertisement() {
		
	}


	@Override
    public Document getDocument(MimeMediaType TheMimeMediaType) {
        
        // Creating document
        StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
                TheMimeMediaType, AdvertisementType);
        
        Element e  = TheResult.createElement(PeerGroupIdTAG, peerGroupId.toString());
    	TheResult.appendChild(e);
    	e  = TheResult.createElement(PeerIDTAG, getPeerID().toString());
    	TheResult.appendChild(e);
    	e  = TheResult.createElement(StartDateTAG, getStartDate()+"");
    	TheResult.appendChild(e);
    	
        
      
        return TheResult;
        
    }

    public void SetID(ID TheID) {
        AdvertisementID = TheID;
    }


    @Override
    public synchronized ID getID() {
    	if (AdvertisementID == ID.nullID) {
            try {
                // We have not yet built it. Do it now
                byte[] seed = getAdvertisementType().getBytes("UTF-8");
                InputStream in = new ByteArrayInputStream(getPeerID().toString().getBytes("UTF-8"));
                AdvertisementID = IDFactory.newCodatID((PeerGroupID) peerGroupId, seed, in);
            } catch (Exception ez) {
                return ID.nullID;
            }
        }
        return AdvertisementID;
    }

    
    @Override
    public String[] getIndexFields() {
        return IndexableFields;
    }

   
    @Override
    public RendezVousAdvertisement clone() throws CloneNotSupportedException {
        
        RendezVousAdvertisement Result =
                (RendezVousAdvertisement) super.clone();

        Result.AdvertisementID = this.AdvertisementID;
        Result.peerGroupId = this.peerGroupId;
        Result.setPeerID(this.getPeerID());
        Result.setStartDate(this.getStartDate());
        
        return Result;
        
    }
    
    @Override
    public String getAdvType() {
        
        return AdvertisementType;
        
    }
    
    public static String getAdvertisementType() {
        return AdvertisementType;
    }    
    
    public PeerID getPeerID() {
		return peerId;
	}

	public void setPeerID(PeerID peerId) {
		this.peerId = peerId;
	}

	public static class Instantiator implements AdvertisementFactory.Instantiator {

        public String getAdvertisementType() {
            return RendezVousAdvertisement.getAdvertisementType();
        }

      

        public Advertisement newInstance(net.jxta.document.Element root) {
            return new RendezVousAdvertisement(root);
        }

		@Override
		public Advertisement newInstance() {
			 return new RendezVousAdvertisement();
		}
        
    }

	public void setPeerGroupId(PeerGroupID peerGroupID2) {
		peerGroupId = peerGroupID2;;
		
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	
	
	
}
