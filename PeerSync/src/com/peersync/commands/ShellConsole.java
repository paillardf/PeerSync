package com.peersync.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;



public class ShellConsole {
	// TODO : thread d'écoute
	private static ShellConsole instance;
	private boolean running;

	public final static String BEGIN = "^";
	public final static String SPACES = "[\\s]+";
	public final static String FACSPACES = "[\\s]*";
	public final static String ARG = "(\\S+)";
	public final static String END = "$";
	public final static String JOKER = "(.*)";
	
	public static ShellConsole getShellConsole() 
	{
		if(instance==null)
			instance = new ShellConsole();
		return instance;

	}
	
	private ShellConsole()
	{
		running=false;
	}
	
	public void start()
	{
		if(!running)
		{
			running=true;
			mainloop();
			
		}
	}
	
	public void stop()
	{
		running=false;
	}
	
	private void commandRoute(String command)
	{
		String pattern = BEGIN+FACSPACES+ARG+FACSPACES+JOKER+END;
		Pattern p = Pattern.compile(pattern);
		java.util.regex.Matcher m = p.matcher(command);
		if (m.matches()) {
			String commandName=m.group(1);
			if(commandName.length()>0)
			{
				commandName=commandName.substring(0,1).toUpperCase() + commandName.substring(1);
				try {
					AbstractCommand c = (AbstractCommand)Class.forName("com.peersync.commands."+commandName).newInstance();
					c.exec(" "+m.group(2)+" "); // Ajout d' " " pour faciliter le parsing
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					System.out.println("Commande "+command+" invalide");
				}
			}
			
				
			
		}
	}
	
	private void mainloop()
	{
		
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (running) {
			// read what the user entered
			System.out.print("PeerSyncShell>");
			try {
				commandLine = console.readLine();
				if(!commandLine.equals(""))
				{
					commandRoute(commandLine);

				}





			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

}
