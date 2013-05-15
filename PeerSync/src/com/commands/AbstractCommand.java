package com.commands;

abstract class AbstractCommand {
	
	// Set Shortcut(vérifier que pas de doublons)
	// Set Nom (vérifier que pas de doublons)
	// List arguments (list d'objet, tous args)
	// Groupes OR (list obj) --> ex start, stop et restart ou exclusif
	// Groupes AND (list obj) pour des args interdépendants
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
