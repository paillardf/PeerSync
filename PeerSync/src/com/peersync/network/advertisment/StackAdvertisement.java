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
import java.io.IOException;
import java.io.InputStream;
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
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;

import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.network.PeerSync;

public class StackAdvertisement extends Advertisement {

	public static final String Name = "FilesVersionAdvertisement";

	// Advertisement elements, tags and indexables
	public final static String AdvertisementType = "jxta:FilesVersionAdvertisement";

	private ID AdvertisementID = ID.nullID;

	public final static String ShareFolderTAG = "sharefolderID";
	private final static String StackTAG = "stackID";
	private final static String PeerIdTAG = "peerID";
	private final static String PeerGroupIDTAG = "peerGroupID";
	private final static String StackLastUpdateTAG = "last_update";

	private ArrayList<SharedFolderVersion> shareFolderList;

	private String peerId;

	private PeerGroupID peerGroupId;


	private final static String[] IndexableFields = { ShareFolderTAG , StackTAG};

	public StackAdvertisement(ArrayList<SharedFolderVersion> shareFolder,PeerGroupID peerGroupId,PeerID peerId) {
		this.shareFolderList = shareFolder;
		this.peerGroupId = peerGroupId;
		this.peerId = peerId.toString();
		
		
	}
	private StackAdvertisement() {
		shareFolderList = new ArrayList<SharedFolderVersion>();
	}


	public StackAdvertisement(Element Root) {
		this();
		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;
		Enumeration folderList = MyTextElement.getChildren();

		while (folderList.hasMoreElements()) {

			TextElement folderElement = (TextElement) folderList.nextElement();


			if(folderElement.getName().compareTo(ShareFolderTAG)==0){
				SharedFolderVersion shareFolder = new SharedFolderVersion(folderElement.getValue());

				Enumeration stackList = (Enumeration) folderElement.getChildren();

				while (stackList.hasMoreElements()) {

					TextElement stackElement = (TextElement) stackList.nextElement();

					if(stackElement.getName().compareTo(StackTAG)==0){

						long lastUpdate = Long.parseLong(((TextElement) stackElement.getChildren(StackLastUpdateTAG).nextElement()).getValue());

						shareFolder.addStackVersion(new StackVersion(stackElement.getValue(), lastUpdate));
					}
				}
				this.shareFolderList.add(shareFolder);
			}else if(folderElement.getName().compareTo(PeerIdTAG)==0){
				peerId = folderElement.getValue();
			}else if(folderElement.getName().compareTo(PeerGroupIDTAG)==0){
				try {
					peerGroupId = (PeerGroupID) IDFactory.fromURI(new URI(folderElement.getValue()));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//	private void addShareFolder(SharedFolderVersion shareFolder){
	//		this.shareFolderList.add(shareFolder);
	//	}

	public ArrayList<SharedFolderVersion> getShareFolderList(){
		return shareFolderList;
	}
	@Override
	public Document getDocument(MimeMediaType TheMimeMediaType) {

		// Creating document
		StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
				TheMimeMediaType, AdvertisementType);

		Element peerIdElement = TheResult.createElement(PeerIdTAG, getPeerId());
		TheResult.appendChild(peerIdElement);

		Element peerGroupIdElement = TheResult.createElement(PeerGroupIDTAG, peerGroupId.toString());
		TheResult.appendChild(peerGroupIdElement);

		for (SharedFolderVersion shareFolder : shareFolderList) {
			Element shareFolderElement  = TheResult.createElement(ShareFolderTAG, shareFolder.getUID());
			TheResult.appendChild(shareFolderElement);

			for (StackVersion stackVersion : shareFolder.getStackVersionList()) {

				Element stackElement = TheResult.createElement(StackTAG, stackVersion.getUID());
				shareFolderElement.appendChild(stackElement);
				Element stackLastUpdate = TheResult.createElement(StackLastUpdateTAG, ""+stackVersion.getLastUpdate());
				stackElement.appendChild(stackLastUpdate);
			}
		}

		return TheResult;

	}

	public void SetID(ID TheID) {
		AdvertisementID = TheID;
	}



	@Override
	public synchronized ID getID() {
		if (AdvertisementID == ID.nullID) {
			byte[] seed;
			try {
				
				
				seed = getAdvertisementType().getBytes("UTF-8");
				InputStream in = new ByteArrayInputStream(getPeerId().getBytes("UTF-8"));
				AdvertisementID = IDFactory.newCodatID((PeerGroupID) peerGroupId, seed, in);
			} catch (IOException e) {
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
	public StackAdvertisement clone() throws CloneNotSupportedException {

		StackAdvertisement Result =
				(StackAdvertisement) super.clone();

		Result.AdvertisementID = this.AdvertisementID;
		Result.shareFolderList = (ArrayList<SharedFolderVersion>) this.shareFolderList.clone();
		Result.setPeerId(this.getPeerId());
		Result.peerGroupId = this.peerGroupId;
		return Result;

	}

	@Override
	public String getAdvType() {

		return AdvertisementType;

	}

	public static String getAdvertisementType() {
		return AdvertisementType;
	}    

	public void setPeerId(String peerID) {
		peerId=peerID;

	}

	public String getPeerId() {
		return peerId;

	}

	public static class Instantiator implements AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return StackAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance(ArrayList<SharedFolderVersion> shareFolderList, PeerGroupID peerG, PeerID peerID) {
			return new StackAdvertisement(shareFolderList, peerG, peerID);
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
