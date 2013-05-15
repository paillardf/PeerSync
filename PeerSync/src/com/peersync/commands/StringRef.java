package com.peersync.commands;

public class StringRef
{
	public StringRef(String s)
	{
		setData(s);
	}
	
	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}