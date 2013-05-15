package com.peersync.commands;

import java.util.ArrayList;

abstract class AbstractCommand {

	// Set Shortcut(vérifier que pas de doublons)
	// Set Nom (vérifier que pas de doublons)
	// List arguments (list d'objet, tous args)
	// Groupes OR (list obj) --> ex start, stop et restart ou exclusif
	// Groupes AND (list obj) pour des args interdépendants

	protected ArgumentsList allArguments = new ArgumentsList();
	protected OperatorNode rootParser;

	private String description;
	private String name;

	public AbstractCommand()
	{
		String name = getClass().getSimpleName();
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		setName(name);


	}



	abstract public void exec(String QueryString);

	public void setRootParser(OperatorNode r)
	{
		rootParser=r;
	}

	public void help()
	{
		println("NAME");
		println(" ");
		println("\t"+getName());
		println(" ");
		println("SYNOPSIS");
		println(" ");
		println("\t"+getName()+" "+rootParser.toString());
		println(" ");
		println("DESCRIPTION");
		println(" ");
		println("\t"+getDescription());
		println(" ");
		println("OPTIONS");
		println(" ");
		//les arguments
		for(AbstractArgument arg : allArguments.getArguments())
		{
			print("\t");
			if(arg.getShortcut()!=null)
				print(arg.getShortcut()+", ");
			print(arg.getName());
			if(arg instanceof ValueArgument)
				print(" <value>");
			println(" : "+arg.getDescription());

		}
		println(" ");

	}

	public void println(String disp)
	{
		System.out.println(disp);
	}


	public void print(String disp)
	{
		System.out.print(disp);
	}






	public boolean checkGroupValidity(String queryString)
	{
	StringRef sref = new StringRef(queryString);
		return rootParser.parse(sref,true);
		//		for (ArgumentsList alo : exclusivesArgumentsGroups)
		//		{
		//			boolean found =false;
		//			for(AbstractArgument arg : alo.getArguments())
		//			{
		//				if(arg.checkPresence(queryString) && !found)
		//					found = true;
		//				else if(arg.checkPresence(queryString) && found)
		//					return false;
		//			}
		//		}
		//		
		//		for (ArgumentsList ala : dependantsArgumentsGroups)
		//		{
		//			int nbOk = 0;
		//			int total = ala.getArguments().size();
		//			for(AbstractArgument arg : ala.getArguments())
		//			{
		//				if(arg.checkPresence(queryString))
		//					nbOk++;
		//				
		//			}
		//			if(nbOk!=0 && nbOk!=total)
		//				return false;
		//				
		//		}
		//		return true;


	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}

}
