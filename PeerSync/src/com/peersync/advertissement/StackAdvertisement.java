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

package com.peersync.advertissement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;

import com.peersync.data.StackVersion;

public class StackAdvertisement extends Advertisement {
    
    public static final String Name = "FilesVersionAdvertisement";

    // Advertisement elements, tags and indexables
    public final static String AdvertisementType = "jxta:FilesVersionAdvertisement";
    
    private ID AdvertisementID = ID.nullID;
    
    private final static String IDTag = "advID";
    private final static String StackTAG = "Stack";
    private final static String UIDTAG = "UID";
    private final static String LastUpdateTAG = "LastUpdate";

	private ArrayList<StackVersion> stackList;

   //private ArrayList<StackVersion> stackList;
    
    private final static String[] IndexableFields = { IDTag };

    public StackAdvertisement(ArrayList<StackVersion> stackList) {
    	this.stackList = stackList;

    }

    
    public StackAdvertisement() {
    	stackList = new ArrayList<StackVersion>();
	}

    
    public StackAdvertisement(Element Root) {
        this();
        // Retrieving the elements
        TextElement MyTextElement = (TextElement) Root;

        Enumeration TheElements = MyTextElement.getChildren();
        
        
        while (TheElements.hasMoreElements()) {
            
            TextElement TheElement = (TextElement) TheElements.nextElement();
            
            
            String TheElementName = TheElement.getName();
            if(TheElementName.compareTo(StackTAG)==0){
            	Enumeration stackEnum = (Enumeration) TheElement.getChildren();
            	long lastUpdate = 0;
            	String UID = null;
            	while (stackEnum.hasMoreElements()) {
            		
            		TextElement stackElement = (TextElement) stackEnum.nextElement();
            		String theElementName = stackElement.getName();
            		String value = stackElement.getTextValue();
            		
            		 if (theElementName.compareTo(UIDTAG)==0) {
            			 UID = theElementName;
            			 UID = theElementName;
            		 }else if(theElementName.compareTo(LastUpdateTAG)==0){
            			 lastUpdate = Long.parseLong(value);
            		 }
            		
            	}
            	if(lastUpdate!=0&&UID!=null){
            		stackList.add(new StackVersion(UID, lastUpdate));
            	}
            	
            	
            	
            	
            }else if(TheElementName.compareTo(IDTag)==0){

                try {
                    
                    URI ReadID = new URI(TheElement.getValue());
                    AdvertisementID = IDFactory.fromURI(ReadID);
                    return;
                    
                } catch (URISyntaxException Ex) {
                    
                    // Issue with ID format
                    Ex.printStackTrace();
                    
                } catch (ClassCastException Ex) {
                    
                    // Issue with ID type
                    Ex.printStackTrace();
                    
                }
            }
            
        }  

    }
    
    public void addStackVersion(StackVersion stackVersion){
    	this.stackList.add(stackVersion);
    }
    
    public ArrayList<StackVersion> getStackVersionArray(){
    	return stackList;
    }
	@Override
    public Document getDocument(MimeMediaType TheMimeMediaType) {
        
        // Creating document
        StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
                TheMimeMediaType, AdvertisementType);
        
        
        for (StackVersion stackVersion : stackList) {
        	Element stackValue;
        	Element stackElement  = TheResult.createElement(StackTAG, stackVersion.getUID());;
        	
        	stackValue= TheResult.createElement(UIDTAG, stackVersion.getUID());
        	stackElement.appendChild(stackValue);
        	
        	stackValue= TheResult.createElement(LastUpdateTAG, stackVersion.getLastUpdate());
        	stackElement.appendChild(stackValue);
        	
        	TheResult.appendChild(stackElement);
       
		}
     
        return TheResult;
        
    }

    public void SetID(ID TheID) {
        AdvertisementID = TheID;
    }

    @Override
    public ID getID() {
        return AdvertisementID;
    }

    @Override
    public String[] getIndexFields() {
        return IndexableFields;
    }

   
    @Override
    public StackAdvertisement clone() throws CloneNotSupportedException {
        
        StackAdvertisement Result =
                (StackAdvertisement) super.clone();

        Result.AdvertisementID = this.AdvertisementID;
        Result.stackList = (ArrayList<StackVersion>) this.stackList.clone();
        
        return Result;
        
    }
    
    @Override
    public String getAdvType() {
        
        return StackAdvertisement.class.getName();
        
    }
    
    public static String getAdvertisementType() {
        return AdvertisementType;
    }    
    
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        public String getAdvertisementType() {
            return StackAdvertisement.getAdvertisementType();
        }

        public Advertisement newInstance(ArrayList<StackVersion> stackVersioList) {
            return new StackAdvertisement(stackVersioList);
        }

        public Advertisement newInstance(net.jxta.document.Element root) {
            return new StackAdvertisement(root);
        }

		@Override
		public Advertisement newInstance() {
			 return new StackAdvertisement();
		}
        
    }

}
