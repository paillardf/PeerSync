package com.peersync;

import java.util.Observable;


public class NetworkStats extends Observable{

	private boolean connectNetPeerGroup = false;
	private boolean connectMyPeerGroup = false;
	private boolean connectNetPeerGroupRDV = false;
	private boolean connectMyPeerGroupRDV = false;
	private boolean isMyPeerGroupRDV = false;
	
	public NetworkStats(){
		
	}

	public boolean isConnectMyPeerGroupRDV() {
		return connectMyPeerGroupRDV;
	}

	public void setConnectMyPeerGroupRDV(boolean connectMyPeerGroupRDV) {
		this.connectMyPeerGroupRDV = connectMyPeerGroupRDV;
	}

	public boolean isConnectNetPeerGroupRDV() {
		return connectNetPeerGroupRDV;
	}

	public void setConnectNetPeerGroupRDV(boolean connectNetPeerGroupRDV) {
		this.connectNetPeerGroupRDV = connectNetPeerGroupRDV;
	}

	public boolean isConnectMyPeerGroup() {
		return connectMyPeerGroup;
	}

	public void setConnectMyPeerGroup(boolean connectMyPeerGroup) {
		this.connectMyPeerGroup = connectMyPeerGroup;
	}

	public boolean isConnectNetPeerGroup() {
		return connectNetPeerGroup;
	}

	public void setConnectNetPeerGroup(boolean connectNetPeerGroup) {
		this.connectNetPeerGroup = connectNetPeerGroup;
	}

	public boolean isMyPeerGroupRDV() {
		return isMyPeerGroupRDV;
	}

	public void setMyPeerGroupRDV(boolean isMyPeerGroupRDV) {
		this.isMyPeerGroupRDV = isMyPeerGroupRDV;
	}
	
	
	
	
	

}
