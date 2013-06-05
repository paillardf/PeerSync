package com.peersync.cli.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.Event;

public class GetConflict extends AbstractCommand {


	public GetConflict()
	{
		setDescription("Obtient des informations sur les fichiers en conflits");
		OperatorNode root = new OperatorNode(Operator.XOR);
		{

			ValueArgument a;
			try {
				a = new ValueArgument("sharedFolderID","-sf","UID d'un shared Folder");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("file","-f","Chemin d'un fichier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("numero","-n","Numero du conflit");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {
		boolean stop = false;
		AbstractArgument argSF = allArguments.getArgumentByName("sharedFolderID");
		AbstractArgument argNumero = allArguments.getArgumentByName("numero");
		String sf = argSF.getValue(queryString);
		HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
		try
		{
			int numberConflict = Integer.parseInt(argNumero.getValue(queryString));
			if(numberConflict>0 && numberConflict<=events.size())
			{
				int cpt=0;
				int i=0;
				for(Entry<String, ArrayList<Event>> entry : events.entrySet())
				{
					cpt++;
					
					
					if(cpt==numberConflict)
					{
						
						println("Fichier en conflit : "+entry.getKey());
						println("Numero version\t|\t\t\tResponsable de l'event\t\t\t| Type conflit");
						for(Event e : entry.getValue())
						{
							i++;
							char action= 'C';
							switch (e.getAction())
							{ 
							case Event.ACTION_CREATE :
								action = 'C';
								break;
							case Event.ACTION_DELETE :
								action = 'D';
								break;
							case Event.ACTION_UPDATE :
								action = 'U';
								break;
							}
							println("\t"+i+"\t "+formatString(e.getOwner(),70,true)+" "+action);
						}

						break;
					}
				}
			}
			stop = true;

		}
		catch(NumberFormatException e)
		{

		}




		if(!stop)
		{
			if(sf!=null)
			{

				println("OK");
			}
			else
			{

				int cpt=0;
				println("Numero conflit\t|\t\t\tChemin du fichier\t\t\t");
				for(Entry<String, ArrayList<Event>> entry : events.entrySet())
				{
					cpt++;

					println("\t"+cpt+"\t "+formatString(entry.getKey(),65,true));
				}

			}
		}

	}


	private String formatString(String s,int size,boolean truncBegin)
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






}

