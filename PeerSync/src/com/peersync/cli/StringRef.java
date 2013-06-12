package com.peersync.cli;

public class StringRef
{
	public StringRef(String s)
	{
		setData(s);
	}
	
	public StringRef(StringRef s)
	{
		setData(s.data);
	}
	
	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}