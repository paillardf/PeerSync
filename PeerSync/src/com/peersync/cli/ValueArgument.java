package com.peersync.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueArgument extends AbstractArgument{


	public ValueArgument(String name, String shortcut, String description) throws Exception {
		super(name, shortcut, description);

	}


	public String getValue(String queryString)
	{

		Pattern regexName = Pattern.compile(getRegexName());
		Matcher mName = regexName.matcher(queryString);

		Pattern regexShortcut = Pattern.compile(getRegexShortcut());
		Matcher mShortcut = regexShortcut.matcher(queryString);
		if(mName.find())
			return mName.group(1);
		else if(mShortcut.find())
			return mShortcut.group(1);
		else
			return null;

	}



	public String getRegexName() {
		return ShellConsole.SPACES+name+ShellConsole.SPACES+ShellConsole.ARG+ShellConsole.SPACES;
	}


	public String getRegexShortcut() {
		return ShellConsole.SPACES+shortcut+ShellConsole.SPACES+ShellConsole.ARG+ShellConsole.SPACES;
	}
}
