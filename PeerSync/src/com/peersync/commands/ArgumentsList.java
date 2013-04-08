package com.peersync.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ArgumentsList {
	
	private Set<String> namesUsed = new HashSet<String>();
	private Set<String> shortcutsUsed = new HashSet<String>();
	private ArrayList<AbstractArgument> arguments = new ArrayList<AbstractArgument>() ;
	
	public boolean addArgument(AbstractArgument a)
	{
		if(namesUsed.contains(a.getName()) || shortcutsUsed.contains(a.getShortcut()))
			return false;
		else
		{
			namesUsed.add(a.getName());
			if(a.getShortcut()!=null)
				shortcutsUsed.add(a.getShortcut());
			arguments.add(a);
		}
		return true;
			
	}
	
	

	public ArrayList<AbstractArgument> getArguments() {
		return arguments;
	}

	public void setArguments(ArrayList<AbstractArgument> arguments) {
		this.arguments = arguments;
	}
	

	

}
