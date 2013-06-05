package com.peersync.network.group;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.access.pse.PSEAccessService;
import net.jxta.impl.content.ContentServiceImpl;
import net.jxta.impl.membership.pse.PSECredential;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.peergroup.CompatibilityUtils;
import net.jxta.impl.peergroup.StdPeerGroup;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public class GroupUtils {

	  private final static ModuleSpecID PSE_SAMPLE_MSID = (ModuleSpecID) ID.create(
	            URI.create("urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE0000000133BF5414AC624CC8AD3AF6AEC2C8264306"));
	  
	  
	  
	public static ModuleImplAdvertisement createAllPurposePeerGroupWithPSEModuleImplAdv() {

		ModuleImplAdvertisement implAdv = CompatibilityUtils.createModuleImplAdvertisement(
				PeerGroup.allPurposePeerGroupSpecID, StdPeerGroup.class.getName(),
				"Peer Group with PSE Implementation");

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
	
//	public static ModuleImplAdvertisement build_psegroup_impl_adv1(PeerGroup base) {
//	        ModuleImplAdvertisement newGroupImpl;
//
//	        try {
//	            newGroupImpl = base.getAllPurposePeerGroupImplAdvertisement();
//	        } catch (Exception unlikely) {
//	            // getAllPurposePeerGroupImplAdvertisement() doesn't really throw expections.
//	            throw new IllegalStateException("Could not get All Purpose Peer Group Impl Advertisement.");
//	        }
//
//	        newGroupImpl.setModuleSpecID(PSE_SAMPLE_MSID);
//	        newGroupImpl.setDescription("PSE Sample Peer Group Implementation");
//
//	        // FIXME bondolo Use something else to edit the params.
//	        StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());
//
//	        Map services = params.getServices();
//	        
//	        
//	        ModuleImplAdvertisement aModuleAdv = CompatibilityUtils.createModuleImplAdvertisement(
//	                PeerGroup.refMembershipSpecID, StdPeerGroup.class.getName(),
//	                "General Purpose Peer Group with PSE Implementation");
//	        
//	        
////	        ModuleImplAdvertisement.getAdvertisementType()
////	        ModuleImplAdvertisement aModuleAdv = (PeerGroup.) services.get(PeerGroup.membershipClassID);
//
//	        services.remove(PeerGroup.membershipClassID);
//
//	        ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
//	                ModuleImplAdvertisement.getAdvertisementType());
//
//	        implAdv.setModuleSpecID(PSEMembershipService.pseMembershipSpecID);
//	        implAdv.setCompat(aModuleAdv.getCompat());
//	        implAdv.setCode(PSEMembershipService.class.getName());
//	        implAdv.setUri(aModuleAdv.getUri());
//	        implAdv.setProvider(aModuleAdv.getProvider());
//	        implAdv.setDescription("PSE Membership Service");
//
//	        // Add our selected membership service to the peer group service as the
//	        // group's default membership service.
//	        services.put(PeerGroup.membershipClassID, implAdv);
//
//	        // Save the group impl parameters
//	        newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));
//
//	        return newGroupImpl;
//	    }

	 
	 
	public static PeerGroupAdvertisement build_psegroup_adv(ModuleImplAdvertisement pseImpl,String peerGroupName,String description,PeerGroupID peerGroupID) {
		PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
				PeerGroupAdvertisement.getAdvertisementType());
		//newPGAdv.getAdvType();
		newPGAdv.setPeerGroupID(peerGroupID);
		newPGAdv.setModuleSpecID(pseImpl.getModuleSpecID());
		newPGAdv.setName(peerGroupName);
		newPGAdv.setDescription(description);
		PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());

		
		XMLDocument pseDoc = (XMLDocument) pseConf.getDocument(MimeMediaType.XMLUTF8);

		newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);

		return newPGAdv;
	}

	public static PeerGroupAdvertisement build_psegroup_adv(ModuleImplAdvertisement pseImpl, String peerGroupID,String name,String description,  X509Certificate[] invitationCertChain, EncryptedPrivateKeyInfo invitationPrivateKey) throws URISyntaxException {
        PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());

			newPGAdv.setPeerGroupID((PeerGroupID) IDFactory.fromURI(new URI(peerGroupID)));
        newPGAdv.setModuleSpecID(pseImpl.getModuleSpecID());
        newPGAdv.setName(name);
        newPGAdv.setDescription(description);

        PSEConfigAdv pseConf = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());

        pseConf.setCertificateChain(invitationCertChain);
        pseConf.setEncryptedPrivateKey(invitationPrivateKey, invitationCertChain[0].getPublicKey().getAlgorithm());

        XMLDocument pseDoc = (XMLDocument) pseConf.getDocument(MimeMediaType.XMLUTF8);

        newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);

        return newPGAdv;
    }

}
