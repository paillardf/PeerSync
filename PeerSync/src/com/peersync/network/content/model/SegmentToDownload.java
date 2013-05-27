package com.peersync.network.content.model;

import java.util.ArrayList;
import java.util.Set;

import net.jxta.id.ID;


public class SegmentToDownload {

	private String hash;
	private Set<ID> providers;
	private BytesSegment segment;
	
	public SegmentToDownload(String hash,BytesSegment segment,Set<ID> providers)
	{
		this.hash=hash;
		this.segment=segment;
		this.providers=providers;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Set<ID> getProviders() {
		return providers;
	}

	public void setProviders(Set<ID> providers) {
		this.providers = providers;
	}

	public BytesSegment getSegment() {
		return segment;
	}

	public void setSegment(BytesSegment segment) {
		this.segment = segment;
	}

}
