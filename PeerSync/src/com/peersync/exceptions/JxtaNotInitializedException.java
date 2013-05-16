package com.peersync.exceptions;

public class JxtaNotInitializedException extends Exception{
	
	public JxtaNotInitializedException(String message) {
		super(message+" Jxta is not properly initialized");
	}
	

}
