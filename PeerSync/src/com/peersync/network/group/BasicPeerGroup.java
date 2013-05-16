package com.peersync.network.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.rendezvous.RendezVousService;

import com.peersync.events.ScanService;
import com.peersync.exceptions.BasicPeerGroupException;
import com.peersync.models.PeerGroupEvent;
import com.peersync.network.behaviour.AbstractBehaviour;
import com.peersync.tools.KeyStoreManager;
import com.peersync.tools.Log;

public class BasicPeerGroup  implements Observer{

	private final static String TAG ="BasicPeerGroup";
	private String peerGroupName;
	private PeerGroupID peerGroupId;
	private PeerGroup peerGroup;
	protected ArrayList<AbstractBehaviour> behaviourList = new ArrayList<AbstractBehaviour>();
	private GroupThread thread;
	private Thread.State statue = Thread.State.BLOCKED;
	private PeerGroup netPeerGroup;
	private NetworkConfigurator conf;

	public BasicPeerGroup(PeerGroup netPeerGroup, NetworkConfigurator conf, PeerGroupID psepeergroupid, String peerGroupName) {
		this.peerGroupName = peerGroupName;
		this.peerGroupId=psepeergroupid;
		this.netPeerGroup=netPeerGroup;
		this.conf = conf;
	}


	public void start() throws BasicPeerGroupException {
		if(statue == Thread.State.BLOCKED){
			throw new BasicPeerGroupException("Group has to be initialized first");
		}else if(statue == Thread.State.NEW){
			thread.start();	
			statue = Thread.State.RUNNABLE;
		}else if(statue == Thread.State.RUNNABLE){
			throw new BasicPeerGroupException("Group thread is already running");
		}else{

		}
	}

	public void stop() throws BasicPeerGroupException{
		if(statue == Thread.State.RUNNABLE){
			statue= Thread.State.TERMINATED;
			thread.interrupt();
		}else if(statue == Thread.State.BLOCKED){
			throw new BasicPeerGroupException("Group thread is already stopped");
		}else{
			throw new BasicPeerGroupException("Group thread hasn't been started");
		}
	}

	public  void initialize() throws PeerGroupException, IOException, ProtocolNotSupportedException, BasicPeerGroupException{
		if(statue == Thread.State.BLOCKED){
				PeerGroup myLocalGroup = createNewPeerGroup();
				joinGroup(myLocalGroup);
				thread = new GroupThread();
				initializeBehaviour();
				statue = Thread.State.NEW;
		}else if(statue == Thread.State.RUNNABLE){
			throw new BasicPeerGroupException("Group thread is running");
		}else if(statue == Thread.State.TERMINATED){
			throw new BasicPeerGroupException("Group is already initialized");
		}else if(statue == Thread.State.TERMINATED){
			throw new BasicPeerGroupException("Group thread is currently stopping");
		}
	}
	private void initializeBehaviour() {
		for (AbstractBehaviour b : behaviourList) {
			b.initialize();
		} 
		
		
	}


	private PeerGroup createNewPeerGroup() throws IOException, PeerGroupException {
		PeerGroup tempPeerGroup = null;

		// Build the Module Impl Advertisemet we will use for our group.
		ModuleImplAdvertisement pseImpl =  GroupUtils.createAllPurposePeerGroupWithPSEModuleImplAdv();

		DiscoveryService disco = getNetPeerGroup().getDiscoveryService();
		disco.publish(pseImpl, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);

		PeerGroupAdvertisement pse_pga = null;

		pse_pga = GroupUtils.build_psegroup_adv(pseImpl, peerGroupName, peerGroupId);
		disco.publish(pse_pga, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);
		tempPeerGroup  = initPeerGroup(pse_pga);
		return tempPeerGroup;
	}
	private PeerGroup initPeerGroup(PeerGroupAdvertisement adv) throws PeerGroupException {

		PeerGroup peerGroup = null;
		peerGroup = getNetPeerGroup().newGroup(adv);
		PSEMembershipService memberShip = (PSEMembershipService) peerGroup.getMembershipService();
		memberShip.init(peerGroup, memberShip.getAssignedID(), memberShip.getImplAdvertisement());
		return peerGroup;
	}

	private void joinGroup(PeerGroup myLocalGroup) throws PeerGroupException, ProtocolNotSupportedException {
		PSEMembershipService membership =	(PSEMembershipService)myLocalGroup.getMembershipService();
		StringAuthenticator memberAuthenticator;
		AuthenticationCredential application = new AuthenticationCredential(myLocalGroup, "StringAuthentication", null);
		memberAuthenticator = (StringAuthenticator) membership.apply(application);
		memberAuthenticator.setAuth1_KeyStorePassword(KeyStoreManager.MyKeyStorePassword);
		memberAuthenticator.setAuth2Identity(myLocalGroup.getPeerGroupID());
		memberAuthenticator.setAuth3_IdentityPassword(KeyStoreManager.MyKeyStorePassword);
		if (!memberAuthenticator.isReadyForJoin()) {
			Log.d(TAG,"Authenticator is not complete");
		}
		Credential MyCredential = membership.join(memberAuthenticator);
		Log.d(TAG,"Group has been joined");
		peerGroup = myLocalGroup;
		peerGroup.getRendezVousService().setAutoStart(true, 30000);
	}


	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof ScanService){
			if(((String)arg).equals(getNetPeerGroup().getPeerID().toString())){
				notifyPeerGroupBehaviour(new PeerGroupEvent(PeerGroupEvent.STACK_UPDATE,getPeerGroupID() ,null ));

			}
		}

	}

	public void notifyPeerGroupBehaviour(PeerGroupEvent e) {
		for (AbstractBehaviour behaviour : behaviourList) {
			behaviour.notifyPeerGroup(e);
		}

	}

	public NetworkConfigurator getConf() {
		return conf;
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


	public String getPeerGroupName() {
		return peerGroupName;
	}

	

	private class GroupThread extends Thread{

		private long timeToSleep = 0;

		@Override
		public void run() {
			try {
				while (statue == Thread.State.RUNNABLE) {

					for (AbstractBehaviour behaviour : behaviourList) {
						if(behaviour.hasToRun()){
							behaviour.run();
						}
						timeToSleep = timeToSleep == 0 ? behaviour.getNextExecutionTime() : Math.min(timeToSleep, behaviour.getNextExecutionTime());
					}
					timeToSleep= timeToSleep-System.currentTimeMillis();
					if(timeToSleep>0){
						sleep(timeToSleep);
					}
					timeToSleep = 0;
				}
			} catch (InterruptedException e) {

			}
			Log.d(TAG, "Group "+peerGroupName+" Stop");
			statue = Thread.State.BLOCKED;
			thread=null;
			peerGroup.stopApp();
		}

	}
}
