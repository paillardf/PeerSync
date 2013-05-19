package com.peersync.cli;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.peersync.cli.Node.Operator;

public class ArgumentsList {
	
	
	private Set<String> namesAndShortcutsUsed = new HashSet<String>();

	private Map<String,AbstractArgument> arguments = new Hashtable<String,AbstractArgument>() ;
	
	public boolean addArgument(AbstractArgument a)
	{
		if(namesAndShortcutsUsed.contains(a.getName()) || namesAndShortcutsUsed.contains(a.getShortcut()))
			return false;
		else
		{
			namesAndShortcutsUsed.add(a.getName());
			if(a.getShortcut()!=null)
				namesAndShortcutsUsed.add(a.getShortcut());
			arguments.put(a.getName(), a);
		}
		return true;
			
	}
	
	public ArgumentsList()
	{
		 for (Operator o : Operator.values()) // Réserve les symboles utilisés par les opérateurs
		 {
			 namesAndShortcutsUsed.add(o.getDisplayedOperator());
		 }
	}

	public AbstractArgument getArgumentByName(String name)
	{
		return arguments.get(name);
	}
	
	public Map<String,AbstractArgument> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String,AbstractArgument> arguments) {
		this.arguments = arguments;
	}
	

	

}
