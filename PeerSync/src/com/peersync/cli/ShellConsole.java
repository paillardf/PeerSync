package com.peersync.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;



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

	private Set<String> availableCommands = new HashSet<String>();

	private String packageName=null;


	public void listAvailableCommands()
	{
		List<Class> ac = ClassesLister.getInstance().getClasses(packageName);


		for(Class c : ac)
		{
			String cmd = c.getSimpleName();
			cmd=cmd.substring(0,1).toLowerCase() + cmd.substring(1); 
			availableCommands.add(cmd);
		}

	}




	public static ShellConsole getShellConsole() 
	{
		if(instance==null)
			instance = new ShellConsole();
		return instance;

	}



	private ShellConsole()
	{
		running=false;
		setPackageName("com.peersync.cli.commands");
		listAvailableCommands();
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
					AbstractCommand c = (AbstractCommand)Class.forName(getPackageName()+"."+commandName).newInstance();
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

		ConsoleReader reader;
		try {
			reader = new ConsoleReader();


			reader.setPrompt("PeerSyncShell> ");

			List<Completer> completors = new LinkedList<Completer>();

			completors.add(new StringsCompleter(availableCommands));

			for (Completer c : completors) {
				reader.addCompleter(c);
			}

			String line;
			PrintWriter out = new PrintWriter(reader.getOutput());

			while ((line = reader.readLine()) != null) {
				commandRoute(line);
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//		String commandLine;
		//		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		////		while (running) {
		////			// read what the user entered
		////			System.out.print("PeerSyncShell>");
		////		}

		//				console.
		//				int car = console.read();
		//				if(car ==9)
		//					System.out.print("t");
		//				System.out.println(car);

		//				commandLine = console.readLine();
		//				if(!commandLine.equals(""))
		//				{
		//					commandRoute(commandLine);
		//
		//				}









	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}




}
