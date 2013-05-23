
package com.peersync.network.content.listener;

import java.util.EventObject;

import net.jxta.content.ContentProvider;
import net.jxta.id.ID;

/**
 * Interface provided to notify interested parties of ContentProvider
 * events which may be useful to them.  This can be used by the 
 * application to centralize monitoring hooks, allowing the application
 * to (for example) maintain asynchronously updated lists of current
 * shares and/or automatically perform remote publishing of new
 * advertisements.
 * <p/>
 * Implementors should take note that even if a listener is added to a
 * single provider, events may arrive from multiple providers.  This is
 * due to the fact that some providers (the one to which the listener was
 * added in this scenario) may be aggregations of multiple underlying
 * provider implementations, as is the case with the ContentService
 * implementation.  Unless the event source is explicitly checked the
 * consumer should not make any assumptions relating to the arrival order
 * of the events, etc..
 */
public class ClientEvent extends EventObject {

	private ID clientPipe;

	public ClientEvent(ContentProvider provider, ID id) {
		super(provider);
		this.clientPipe = id;
	}

    
  

}

