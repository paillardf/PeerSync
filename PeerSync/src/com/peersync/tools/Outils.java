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

package com.peersync.tools;

import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLElement;
import net.jxta.impl.access.pse.PSEAccessService;
import net.jxta.impl.content.ContentServiceImpl;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.peergroup.CompatibilityUtils;
import net.jxta.impl.peergroup.StdPeerGroup;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ModuleImplAdvertisement;


public class Outils {

    
    public static final void GoToSleep(long Duration) {
        
        long Delay = System.currentTimeMillis() + Duration;

        while (System.currentTimeMillis()<Delay) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException Ex) {
                // We don't care
            }
        }
        
    }
    
    public static ModuleImplAdvertisement createAllPurposePeerGroupImplAdv() {

        ModuleImplAdvertisement implAdv = CompatibilityUtils.createModuleImplAdvertisement(
            PeerGroup.allPurposePeerGroupSpecID, StdPeerGroup.class.getName(),
            "General Purpose Peer Group");

        // Create the service list for the group.
        StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv();

        // set the services
        paramAdv.addService(PeerGroup.endpointClassID, PeerGroup.refEndpointSpecID);
        paramAdv.addService(PeerGroup.resolverClassID, PeerGroup.refResolverSpecID);
        paramAdv.addService(PeerGroup.membershipClassID, PeerGroup.refMembershipSpecID);
        paramAdv.addService(PeerGroup.accessClassID, PeerGroup.refAccessSpecID);

        // standard services
        paramAdv.addService(PeerGroup.discoveryClassID, PeerGroup.refDiscoverySpecID);
        paramAdv.addService(PeerGroup.rendezvousClassID, PeerGroup.refRendezvousSpecID);
        paramAdv.addService(PeerGroup.pipeClassID, PeerGroup.refPipeSpecID);
        paramAdv.addService(PeerGroup.peerinfoClassID, PeerGroup.refPeerinfoSpecID);

        paramAdv.addService(PeerGroup.contentClassID, ContentServiceImpl.MODULE_SPEC_ID);

        // Insert the newParamAdv in implAdv
        XMLElement paramElement = (XMLElement) paramAdv.getDocument(MimeMediaType.XMLUTF8);
        implAdv.setParam(paramElement);

        return implAdv;

    }
    
    public static ModuleImplAdvertisement createAllPurposePeerGroupWithPSEModuleImplAdv() {

        ModuleImplAdvertisement implAdv = CompatibilityUtils.createModuleImplAdvertisement(
            PeerGroup.allPurposePeerGroupSpecID, StdPeerGroup.class.getName(),
            "General Purpose Peer Group with PSE Implementation");

        // Create the service list for the group.
        StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv();

        // set the services
        paramAdv.addService(PeerGroup.endpointClassID, PeerGroup.refEndpointSpecID);
        paramAdv.addService(PeerGroup.resolverClassID, PeerGroup.refResolverSpecID);
        paramAdv.addService(PeerGroup.membershipClassID, PSEMembershipService.pseMembershipSpecID);
        paramAdv.addService(PeerGroup.accessClassID, PSEAccessService.PSE_ACCESS_SPEC_ID);

        // standard services
        paramAdv.addService(PeerGroup.discoveryClassID, PeerGroup.refDiscoverySpecID);
        paramAdv.addService(PeerGroup.rendezvousClassID, PeerGroup.refRendezvousSpecID);
        paramAdv.addService(PeerGroup.pipeClassID, PeerGroup.refPipeSpecID);
        paramAdv.addService(PeerGroup.peerinfoClassID, PeerGroup.refPeerinfoSpecID);

        paramAdv.addService(PeerGroup.contentClassID, ContentServiceImpl.MODULE_SPEC_ID);

        // Insert the newParamAdv in implAdv
        XMLElement paramElement = (XMLElement) paramAdv.getDocument(MimeMediaType.XMLUTF8);
        implAdv.setParam(paramElement);

        return implAdv;

    }

  

}
