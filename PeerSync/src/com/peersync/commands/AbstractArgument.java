package com.peersync.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import org.apache.derby.impl.sql.compile.QueryTreeNode;


abstract class AbstractArgument {
	
	protected String shortcut;
	protected String name;
	protected String description;
	
	public String getRegexName() {
		return ShellConsole.SPACES+name+ShellConsole.SPACES;
	}

	public String getRegexShortcut() {
		return ShellConsole.SPACES+shortcut+ShellConsole.SPACES;
	}
	
	public String removeArgument(String queryString)
	{
		
		Pattern regexName = Pattern.compile(getRegexName());
		Matcher mName = regexName.matcher(queryString);
		
		Pattern regexShortcut = Pattern.compile(getRegexShortcut());
		Matcher mShortcut = regexShortcut.matcher(queryString);
		if(mName.find()) 
			queryString=queryString.replaceFirst(getRegexName(), " ");
		else if(mShortcut.find()) 
			queryString=queryString.replaceFirst(getRegexShortcut(), " ");
		return queryString;
	}
	
	public String getShortcut() {
		return shortcut;
	}
	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) throws Exception {
		if(name==null)
			throw new Exception("Name can't be null");	
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public AbstractArgument(String name,String shortcut,String description ) throws Exception
	{
		setName(name);
		setShortcut(shortcut);
		setDescription(description);
	}
	
	
	public boolean checkPresence(String queryString)
	{
		
		Pattern regexName = Pattern.compile(getRegexName());
		Matcher mName = regexName.matcher(queryString);
		
		Pattern regexShortcut = Pattern.compile(getRegexShortcut());
		Matcher mShortcut = regexShortcut.matcher(queryString);
		if(mName.find() || mShortcut.find()) 
			return true;
		return false;
		
	}

}
