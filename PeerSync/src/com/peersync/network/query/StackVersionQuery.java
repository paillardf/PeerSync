package com.peersync.network.query;

import java.util.ArrayList;
import java.util.Enumeration;

import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import com.peersync.data.DataBaseManager;
import com.peersync.data.SyncUtils;
import com.peersync.models.Event;
import com.peersync.models.EventsStack;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.network.advertisment.StackAdvertisement;
import com.peersync.network.group.MyPeerGroup;
import com.peersync.tools.Log;

public class StackVersionQuery implements QueryHandler{
	private static final String QueryType = "stackVersion:StackVersionQuery";
	
	private  static final String NAME = "StackVersionQuery";
	private final static String ShareFolderTAG = "sharefolderID";
	private final static String StackTAG = "stackID";
	private final static String StackLastUpdateTAG = "last_update";
	private MyPeerGroup myPeerGroup;
	
	
	
	public StackVersionQuery(MyPeerGroup peerGroup ) {
		this.myPeerGroup = peerGroup;
	}

	public void sendQuery(ArrayList<SharedFolderVersion> sharedFolderVersionList, String peerID){
	
		StructuredDocument doc = (StructuredTextDocument)
		StructuredDocumentFactory.newStructuredDocument(new MimeMediaType( "text/xml" ),QueryType);
		
		for (SharedFolderVersion shareFolder : sharedFolderVersionList) {
			Element shareFolderElement  = doc.createElement(ShareFolderTAG, shareFolder.getUID());
			doc.appendChild(shareFolderElement);

			for (StackVersion stackVersion : shareFolder.getStackVersionList()) {

				Element stackElement = doc.createElement(StackTAG, stackVersion.getUID());
				shareFolderElement.appendChild(stackElement);
				Element stackLastUpdate = doc.createElement(StackLastUpdateTAG, ""+stackVersion.getLastUpdate());
				stackElement.appendChild(stackLastUpdate);
			}
		}
		
		
		ResolverQuery query = new ResolverQuery(doc);
		query.setHandlerName(NAME);
		myPeerGroup.getNetPeerGroup().getResolverService().sendQuery(peerID, query);
		Log.d(StackAdvertisement.Name, "envoi d'une requete: "+query.toString());
		
	}
	
	
	@Override
	public int processQuery(ResolverQueryMsg query) {
		ResolverResponse responseMsg;
		Log.d(StackAdvertisement.Name, "reception d'une requete: "+query.toString());
		//parse the query Message
		StructuredTextDocument doc = (StructuredTextDocument)
				StructuredDocumentFactory.newStructuredDocument(new MimeMediaType("text/xml"), query.getQuery() );
		Enumeration folderList = doc.getChildren();
		ArrayList<SharedFolderVersion> shareFolderList = new ArrayList<SharedFolderVersion>();

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
				shareFolderList.add(shareFolder);
			}
		}
		
		
		// Build the response
		
		StructuredDocument resp = (StructuredTextDocument)
		StructuredDocumentFactory.newStructuredDocument(new MimeMediaType( "text/xml" ),QueryType);
		DataBaseManager db = DataBaseManager.getDataBaseManager();
		for (SharedFolderVersion sharedFolderVersion : shareFolderList) {
			
			EventsStack eventsStack = db.getEventsToSync(sharedFolderVersion);
			if(eventsStack.getEvents().size()>0){
				Element shareFolderElement = resp.createElement(ShareFolderTAG, sharedFolderVersion.getUID());
				resp.appendChild(shareFolderElement);
				for (Event e : eventsStack.getEvents()) {
					e.attacheToXml(shareFolderElement, resp);
				}
			}
			
			
		}
				
		
		responseMsg = new ResolverResponse(resp);
		responseMsg.setHandlerName(NAME);
		responseMsg.setQueryId(query.getQueryId());
		myPeerGroup.getPeerGroup().getResolverService().sendResponse(query.getSrcPeer().toString(), responseMsg);
		return ResolverService.OK;
	}

	@Override
	public void processResponse(ResolverResponseMsg response) {
		try {
			Log.d(StackAdvertisement.Name, "r�ception d'une r�ponse: "+response.toString());
			StructuredTextDocument doc = (StructuredTextDocument)
					StructuredDocumentFactory.newStructuredDocument(new MimeMediaType("text/xml"),	response.getResponse() );
			Enumeration elements = doc.getChildren();
			
			EventsStack eventsStack = new EventsStack();
			while (elements.hasMoreElements()) {
				TextElement shareFolderElement = (TextElement)	elements.nextElement();
				
				if(shareFolderElement.getName()==ShareFolderTAG){
					String shareFolderId = shareFolderElement.getValue();
					Enumeration eventElements = shareFolderElement.getChildren();
					
					while (eventElements.hasMoreElements()) {
						TextElement eventElement = (TextElement)	eventElements.nextElement();
						if(eventElement.getName().compareTo(Event.EVENTID_TAG)==0){
							//new Event(shareFolderId, date, filepath, isFile, newHash, oldHash, action, owner, status)
							eventsStack.addEvent(
								new Event(shareFolderId,
								Long.parseLong(((TextElement) eventElement.getChildren(Event.DATE_TAG).nextElement()).getValue()),
								((TextElement) eventElement.getChildren(Event.PATH_TAG).nextElement()).getValue(),
								Integer.parseInt(((TextElement) eventElement.getChildren(Event.ISFILE_TAG).nextElement()).getValue()),
								((TextElement) eventElement.getChildren(Event.NEWHASH_TAG).nextElement()).getValue(), 
								((TextElement) eventElement.getChildren(Event.OLDHASH_TAG).nextElement()).getValue(),
								Integer.parseInt(((TextElement) eventElement.getChildren(Event.ACTION_TAG).nextElement()).getValue()),
								((TextElement) eventElement.getChildren(Event.OWNER_TAG).nextElement()).getValue(),
								Integer.parseInt(((TextElement) eventElement.getChildren(Event.STATUS_UNSYNC).nextElement()).getValue())));
						}
					}
					
					eventsStack.save();
				}
			}
		}
		catch (Exception e){
			// ignore
		}
		
	}
}
