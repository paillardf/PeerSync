package com.peersync.cli;

import java.util.Map.Entry;

public abstract class AbstractCommand {

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



	public final void exec(String queryString)
	{
		if(parse(queryString))
			requestHandler(queryString);
		else
			help();
	}

	public abstract void requestHandler(String queryString);
	
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
		for(Entry<String, AbstractArgument> entry : allArguments.getArguments().entrySet())
		{
			print("\t");
			AbstractArgument arg = entry.getValue();
			if(arg.getShortcut()!=null)
				print(arg.getShortcut()+", ");
			print(entry.getKey());
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






	public boolean parse(String queryString)
	{
	StringRef sref = new StringRef(queryString);
		return rootParser.parse(sref);


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
	
	public static String formatString(String s,int size,boolean truncBegin)
	{
		String res;
		if(s.length()-size-3<0)
		{
			res= s;
		}
		else
		{
			if(truncBegin)
				res = "..."+s.substring(s.length()-size-3);
			else
				res = s.substring(0,s.length()-size-3)+"...";

		}

		int cpt=res.length();
		for(;cpt<size;cpt++)
		{
			if(cpt%2==0)
				res=" "+res;
			else
				res+=" ";

		}
		return res;

	}
	
	public String getArgumentValue(String argName,String queryString)
	{
		AbstractArgument arg = allArguments.getArgumentByName(argName);
		if(arg!=null)
			return arg.getValue(queryString);
		else
			return null;
	}

}
