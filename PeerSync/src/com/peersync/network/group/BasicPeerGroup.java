package com.peersync.network.group;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.membership.pse.PSECredential;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.rendezvous.RendezVousService;

import com.peersync.events.ScanService;
import com.peersync.exceptions.BasicPeerGroupException;
import com.peersync.models.PeerGroupEvent;
import com.peersync.network.behaviour.AbstractBehaviour;
import com.peersync.tools.Log;
import com.peersync.tools.KeyStoreManager;

public class BasicPeerGroup  implements Observer{

	private String peerGroupName;
	private PeerGroupID peerGroupId;
	protected PeerGroup peerGroup;
	protected ArrayList<AbstractBehaviour> behaviourList = new ArrayList<AbstractBehaviour>();
	private GroupThread thread;
	private Thread.State status = Thread.State.BLOCKED;
	private PeerGroup netPeerGroup;
	private String description = "";
	private PSECredential myCredential;

	public BasicPeerGroup(PeerGroupID psepeergroupid, String peerGroupName,String description) {
		this.peerGroupName = peerGroupName;
		this.peerGroupId=psepeergroupid;
		this.description=description;
	}


	public void start() throws BasicPeerGroupException {
		if(status == Thread.State.BLOCKED){
			throw new BasicPeerGroupException("Group has to be initialized first");
		}else if(status == Thread.State.NEW){
			thread.start();	
			status = Thread.State.RUNNABLE;
		}else if(status == Thread.State.RUNNABLE){
			throw new BasicPeerGroupException("Group thread is already running");
		}else{

		}
	}

	public void stop() throws BasicPeerGroupException{
		if(status == Thread.State.RUNNABLE){
			status= Thread.State.TERMINATED;
			thread.interrupt();
			peerGroup.stopApp();
			peerGroup = null;
		}else if(status == Thread.State.BLOCKED){
			throw new BasicPeerGroupException("Group thread is already stopped");
		}else{
			throw new BasicPeerGroupException("Group thread hasn't been started");
		}
	}

	public  void initialize(PeerGroup netPeerGroup) throws PeerGroupException, IOException, ProtocolNotSupportedException, BasicPeerGroupException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, URISyntaxException{
		this.netPeerGroup = netPeerGroup;
		
		if(status == Thread.State.BLOCKED){
				PeerGroup myLocalGroup = createNewPeerGroup();
				joinGroup(myLocalGroup);
				thread = new GroupThread();
				initializeBehaviour();
				status = Thread.State.NEW;
		}else if(status == Thread.State.RUNNABLE){
			throw new BasicPeerGroupException("Group thread is running");
		}else if(status == Thread.State.TERMINATED){
			throw new BasicPeerGroupException("Group is already initialized");
		}else if(status == Thread.State.TERMINATED){
			throw new BasicPeerGroupException("Group thread is currently stopping");
		}
	}
	private void initializeBehaviour() {
		for (AbstractBehaviour b : behaviourList) {
			b.initialize();
		} 
		
		
	}


	private PeerGroup createNewPeerGroup() throws IOException, PeerGroupException, URISyntaxException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		PeerGroup tempPeerGroup = null;

		// Build the Module Impl Advertisemet we will use for our group.
		ModuleImplAdvertisement pseImpl =  GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv();

		DiscoveryService disco = getNetPeerGroup().getDiscoveryService();
		disco.publish(pseImpl, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);

		PeerGroupAdvertisement pse_pga = null;

		
//		KeyStoreManager ks = KeyStoreManager.getInstance();
//		X509Certificate[] cert = {ks.getX509Certificate(peerGroupId.toString())};
//		pse_pga = GroupUtils.build_psegroup_adv( pseImpl,peerGroupId.toString(), peerGroupName,description,cert ,
//				ks.getEncryptedPrivateKey(peerGroupId.toString(), ks.MyKeyStorePassword.toCharArray(), ks.MyKeyStorePassword.toCharArray()) );
		
		pse_pga = GroupUtils.build_psegroup_adv( pseImpl, peerGroupName,description, peerGroupId);
//				
		disco.publish(pse_pga, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);
		getNetPeerGroup().getDiscoveryService().publish(pse_pga);
		getNetPeerGroup().getDiscoveryService().remotePublish(pse_pga);
		tempPeerGroup  = initPeerGroup(pse_pga);
		return tempPeerGroup;
	}
	private PeerGroup initPeerGroup(PeerGroupAdvertisement adv) throws PeerGroupException {

		PeerGroup peerGroup = null;
		peerGroup = getNetPeerGroup().newGroup(adv);
		/*	PSEMembershipService memberShip = (PSEMembershipService) peerGroup.getMembershipService();
		memberShip.init(peerGroup, memberShip.getAssignedID(), memberShip.getImplAdvertisement());
		
		
		memberShip.getCurrentCredentials().notify();
		
		ContentServiceImpl contentService = (ContentServiceImpl) peerGroup.getContentService();
		
		contentService.init(peerGroup, ContentServiceImpl.MODULE_SPEC_ID, contentService.getImplAdvertisement());
		*/
		return peerGroup;
	}

	private void joinGroup(PeerGroup myLocalGroup) throws PeerGroupException, ProtocolNotSupportedException {
		PSEMembershipService membership =	(PSEMembershipService)myLocalGroup.getMembershipService();
		StringAuthenticator memberAuthenticator;
		AuthenticationCredential application = new AuthenticationCredential(myLocalGroup, "StringAuthentication", null);
		membership.resign();
		membership.getPSEConfig().setKeyStorePassword(KeyStoreManager.MyKeyStorePassword.toCharArray());
		memberAuthenticator = (StringAuthenticator) membership.apply(application);
		memberAuthenticator.setAuth1_KeyStorePassword(KeyStoreManager.MyKeyStorePassword);
		memberAuthenticator.setAuth2Identity(myLocalGroup.getPeerGroupID());
		memberAuthenticator.setAuth3_IdentityPassword(KeyStoreManager.MyKeyStorePassword);
		if (!memberAuthenticator.isReadyForJoin()) {
			Log.s("Authenticator is not complete", getPeerGroupID().toString());
		}
		myCredential = (PSECredential) membership.join(memberAuthenticator);
		Log.s("Group has been joined", getPeerGroupID().toString());
		peerGroup = myLocalGroup;
		initPeerGroupParameters();
		
	}
	
	protected void initPeerGroupParameters() throws PeerGroupException{
		//peerGroup.getRendezVousService().startRendezVous();
		peerGroup.getRendezVousService().setAutoStart(true, 35000);
		//TlsTransport t = new TlsTransport();
		//(TlsTransport)peerGroup.getEndpointService().getMessageTransport("jxtatls");
		//t.init(peerGroup, PeerGroup.tlsProtoClassID, null);
		//t.startApp(null);
		
	}

	@Override
	public void update(Observable o, Object arg) {
		if(thread!=null){
			 synchronized (thread) {
				 thread.notifyAll();
             }
		}
		if(o instanceof ScanService){
			if(((String)arg).equals(getPeerGroup().getPeerGroupID().toString())){
				notifyPeerGroupBehaviour(new PeerGroupEvent(PeerGroupEvent.STACK_UPDATE,getPeerGroupID() ,null ));

			}
		}

	}

	public void notifyPeerGroupBehaviour(PeerGroupEvent e) {
		for (AbstractBehaviour behaviour : behaviourList) {
			behaviour.notifyPeerGroup(e);
		}

	}


	public PeerGroupID getPeerGroupID(){
		return peerGroupId;
	}

	public PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}


	public PeerGroup getPeerGroup() {
		return peerGroup;
	}
	public RendezVousService getRendezVousService() {
		return peerGroup.getRendezVousService();
	}

	public DiscoveryService getDiscoveryService() {
		return peerGroup.getDiscoveryService();
	}


	public String getDescription() {
		return description;
	}

	public String getPeerGroupName() {
		return peerGroupName;
	}

	private class GroupThread extends Thread{

		private long timeToSleep = 0;
		private Timer timer = new Timer();

		@Override
		public void run() {
			try {
				while (status == Thread.State.RUNNABLE) {

					for (AbstractBehaviour behaviour : behaviourList) {
						if(behaviour.hasToRun()){
							behaviour.run();
						}
						timeToSleep = timeToSleep == 0 ? behaviour.getNextExecutionTime() : Math.min(timeToSleep, behaviour.getNextExecutionTime());
					}
					timeToSleep= timeToSleep-System.currentTimeMillis();
					if(timeToSleep>0){
						
						timer.schedule(new TimerTask() {
							
							@Override
							public void run() {
								if(thread!=null){
									 synchronized (thread) {
										 thread.notifyAll();
						             }
								}
							}
						}, timeToSleep);
						synchronized (this) {
							wait();
						}
						
						
					}
					timeToSleep = 0;
				}
			} catch (InterruptedException e) {

			}
			Log.d("Group "+peerGroupName+" Stop", getPeerGroupID().toString());
			status = Thread.State.BLOCKED;
			thread=null;
			peerGroup.stopApp();
		}

	}

	public PSECredential getPSECredential() {
		return myCredential;
	}


	public PSEMembershipService getPSEMembershipService() {
		return (PSEMembershipService)peerGroup.getMembershipService();
	}


	public State getStatus() {
		return status;
	}
}
