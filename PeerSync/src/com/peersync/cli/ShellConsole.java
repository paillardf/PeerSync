package com.peersync.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.peersync.cli.jlineCustom.FileCompleter;



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
		System.out.println("Commande "+command);
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

		//FileCompleter fc = new FileCompleter();
//		List<CharSequence> candidates = new LinkedList<CharSequence>();
//		fc.complete("C:\\Program Files\\", 2, candidates);
//		
//		for(CharSequence s : candidates)
//		{
//			System.out.println(s);
//		}
		
		ConsoleReader reader;
		try {
			
			
			
			reader = new ConsoleReader();


			reader.setPrompt("PeerSyncShell> ");
			reader.setCompletionHandler(new com.peersync.cli.jlineCustom.CandidateListCompletionHandler());
			
			
			final List<Completer> completors = Arrays.asList(
				    new StringsCompleter(availableCommands),
				    new FileCompleter());

			reader.addCompleter(new com.peersync.cli.jlineCustom.ArgumentCompleter(completors));
			
			//reader.addCompleter(new ArgumentCompleter(new StringsCompleter("foo", "bar", "baz")));
			//reader.addCompleter(new ArgumentCompleter(new FileNameCompleter()));

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
