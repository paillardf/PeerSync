package com.commands;

abstract class AbstractCommand {
	
	// Set Shortcut(v�rifier que pas de doublons)
	// Set Nom (v�rifier que pas de doublons)
	// List arguments (list d'objet, tous args)
	// Groupes OR (list obj) --> ex start, stop et restart ou exclusif
	// Groupes AND (list obj) pour des args interd�pendants
	public AbstractCommand()
	{
		
		
	}
	
	
	
	abstract public void exec(String QueryString);
	
	
	abstract public void help();
	
	public void println(String disp)
	{
		System.out.println(disp);
	}
	

}
