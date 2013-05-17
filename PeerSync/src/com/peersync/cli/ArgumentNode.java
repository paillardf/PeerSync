package com.peersync.cli;



public class ArgumentNode extends Node{
	
	private AbstractArgument argument;
	
	public ArgumentNode(AbstractArgument a)
	{
		setArgument(a);
	}

	public AbstractArgument getArgument() {
		return argument;
	}

	public void setArgument(AbstractArgument argument) {
		this.argument = argument;
	}
	
	
	

}
