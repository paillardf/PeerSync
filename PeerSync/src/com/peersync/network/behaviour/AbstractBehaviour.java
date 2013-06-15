package com.peersync.network.behaviour;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.document.XMLElement;
import net.jxta.document.XMLSignatureInfo;
import net.jxta.peer.PeerID;

import com.peersync.models.PeerGroupEvent;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.tools.Log;

public abstract class AbstractBehaviour  implements DiscoveryListener, Runnable{


	private Thread.State statue = Thread.State.RUNNABLE;
	protected BasicPeerGroup myPeerGroup;
	protected long nextExecutionTime = 0;

	public AbstractBehaviour(BasicPeerGroup myPeerGroup) {
		this.myPeerGroup = myPeerGroup;
	}


	public void terminated() {
		statue  = Thread.State.TERMINATED;
	};


	@Override
	public void run() {
		nextExecutionTime = action()+System.currentTimeMillis();
	}

	public boolean hasToRun(){
		if(System.currentTimeMillis()>=nextExecutionTime&&statue==Thread.State.RUNNABLE){
			return true;
		}
		return false;
	}

	protected abstract int action();
	public void initialize(){};
	protected abstract void parseAdvertisement(Advertisement advertisement);
	public abstract void notifyPeerGroup(PeerGroupEvent event);



	@Override
	public void discoveryEvent(DiscoveryEvent event) {
		Enumeration<Advertisement> advertisementsEnum = event.getSearchResults();
		secureDiscovery(advertisementsEnum);
	}


	protected void secureDiscovery(Enumeration<Advertisement> advertisementsEnum) {

		if ((advertisementsEnum != null) && advertisementsEnum.hasMoreElements()) {

			while (advertisementsEnum.hasMoreElements()) {
				Advertisement foundAdv = advertisementsEnum.nextElement();
				foundAdv.verify(myPeerGroup.getPSECredential(), true);
				if(foundAdv.isAuthenticated()){
					PeerID peerid = null;
					if(!foundAdv.isCorrectMembershipKey()){
						Log.i( "ADV not CorrectMembershipKey "+ foundAdv.getAdvType());
						XMLSignatureInfo xmlSignatureInfo = null;
						XMLElement advertismentDocument = (XMLElement)foundAdv.getSignedDocument();
						Enumeration eachElem = advertismentDocument.getChildren();

						while (eachElem.hasMoreElements()) {

							XMLElement anElem = (XMLElement) eachElem.nextElement();

							if ("XMLSignatureInfo".equals(anElem.getName())) {
								//  xmlSignatureInfoElement = anElem;
								xmlSignatureInfo = new XMLSignatureInfo(anElem);
								break;
							} 
							//					            else if ("XMLSignature".equals(anElem.getName())) {
							//					                xmlSignature = new XMLSignature(anElem);
							//					            }
						}
						if(xmlSignatureInfo!=null&&xmlSignatureInfo.getPeerID()!=null){
							try {
								peerid = xmlSignatureInfo.getPeerID();
								myPeerGroup.getPSEMembershipService().getPSEConfig().setTrustedCertificate(xmlSignatureInfo.getPeerID() , myPeerGroup.getPSECredential().getCertificate());
							} catch (KeyStoreException | IOException e) {
								e.printStackTrace();
							}
						}
						foundAdv.verify(myPeerGroup.getPSECredential(), true);
					}

					if(foundAdv.isCorrectMembershipKey()){
						parseAdvertisement(foundAdv);
					}else{
						Log.w("PEER "+ peerid+ " advertisment refuse");
						myPeerGroup.getDiscoveryService().remotePublish(foundAdv);
					}


				}else{
					myPeerGroup.getDiscoveryService().remotePublish(foundAdv);
				}

			}
		}

	}


	public long getNextExecutionTime() {
		return nextExecutionTime;
	}


}
