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

import java.util.Map;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.impl.membership.pse.PSEMembershipService;
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
    /*
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

    private final static ModuleSpecID PSE_SAMPLE_MSID = (ModuleSpecID) ID.create(
            URI.create("urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE0000000133BF5414AC624CC8AD3AF6AEC2C8264306"));
    /*
    public  static ModuleImplAdvertisement createAllPurposePeerGroupWithPSEModuleImplAdv(PeerGroup base) {
        ModuleImplAdvertisement newGroupImpl;

        try {
            newGroupImpl = base.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception unlikely) {
            // getAllPurposePeerGroupImplAdvertisement() doesn't really throw expections.
            throw new IllegalStateException("Could not get All Purpose Peer Group Impl Advertisement.");
        }

        newGroupImpl.setModuleSpecID(PSE_SAMPLE_MSID);
        newGroupImpl.setDescription("PSE Sample Peer Group Implementation");

        // FIXME bondolo Use something else to edit the params.
        StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());

        Map services = params.getServices();

        ModuleImplAdvertisement aModuleAdv = (ModuleImplAdvertisement) services.get(PeerGroup.membershipClassID);

        services.remove(PeerGroup.membershipClassID);

        ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
                ModuleImplAdvertisement.getAdvertisementType());

        implAdv.setModuleSpecID(PSEMembershipService.pseMembershipSpecID);
        implAdv.setCompat(aModuleAdv.getCompat());
        implAdv.setCode(PSEMembershipService.class.getName());
        implAdv.setUri(aModuleAdv.getUri());
        implAdv.setProvider(aModuleAdv.getProvider());
        implAdv.setDescription("PSE Membership Service");

        // Add our selected membership service to the peer group service as the
        // group's default membership service.
        services.put(PeerGroup.membershipClassID, implAdv);

        // Save the group impl parameters
        newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));

        return newGroupImpl;
    }

    
  public  static PeerGroupAdvertisement build_psegroup_adv(PeerGroupID peerGroupID, String peerGroupName, X509Certificate invitationCertChain[], PrivateKey privateKey, String password) {
        PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());

        newPGAdv.setPeerGroupID(peerGroupID);
        newPGAdv.setModuleSpecID(Outils.createAllPurposePeerGroupWithPSEModuleImplAdv().getModuleSpecID());
        newPGAdv.setName(peerGroupName);
        newPGAdv.setDescription("Created by PSE Sample!");

        PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());

       // pseConf.setCertificateChain(invitationCertChain);
		pseConf.setPrivateKey(privateKey, (password+"lkhb").toCharArray());//tEncryptedPrivateKey(invitationPrivateKey, invitationCertChain[0].getPublicKey().getAlgorithm());

        XMLDocument pseDoc = (XMLDocument) pseConf.getDocument(MimeMediaType.XMLUTF8);

        newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);

        return newPGAdv;
    }*/
    
    static ModuleImplAdvertisement build_psegroup_impl_adv(PeerGroup base) {
        ModuleImplAdvertisement newGroupImpl;

        try {
            newGroupImpl = base.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception unlikely) {
            // getAllPurposePeerGroupImplAdvertisement() doesn't really throw expections.
            throw new IllegalStateException("Could not get All Purpose Peer Group Impl Advertisement.");
        }

//        newGroupImpl.setModuleSpecID(PSE_SAMPLE_MSID);
        newGroupImpl.setDescription("PSE Sample Peer Group Implementation");

        // FIXME bondolo Use something else to edit the params.
        StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());

        Map services = params.getServices();
        
        Object val = services.get(PeerGroup.membershipClassID);

        ModuleImplAdvertisement aModuleAdv = (ModuleImplAdvertisement) services.get(PeerGroup.membershipClassID);

        services.remove(PeerGroup.membershipClassID);

        ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
                ModuleImplAdvertisement.getAdvertisementType());

        implAdv.setModuleSpecID(PSEMembershipService.pseMembershipSpecID);
        implAdv.setCompat(aModuleAdv.getCompat());
        implAdv.setCode(PSEMembershipService.class.getName());
        implAdv.setUri(aModuleAdv.getUri());
        implAdv.setProvider(aModuleAdv.getProvider());
        implAdv.setDescription("PSE Membership Service");

        // Add our selected membership service to the peer group service as the
        // group's default membership service.
        services.put(PeerGroup.membershipClassID, implAdv);

        // Save the group impl parameters
        newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));

        return newGroupImpl;
    }
}
