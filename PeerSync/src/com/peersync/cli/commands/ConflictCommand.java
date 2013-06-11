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
import com.peersync.network.PeerSync;

public class ConflictCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("manage conflict");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);

		// List conflict
		OperatorNode andList = new OperatorNode(Operator.AND);
		root.appendChild(andList);

		ValueArgument a;
		ArgumentNode n ;
		a = new ValueArgument(LIST,null,"list conflicts");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andList.appendChild(n);


		// detail conflict
		OperatorNode andDetail = new OperatorNode(Operator.AND);
		root.appendChild(andDetail);

		a = new ValueArgument(DETAIL,"-d","details on a conflict");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andDetail.appendChild(n);

		a = new ValueArgument(NUMBER,"-n","number of a conflict");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andDetail.appendChild(n);



		// detail conflict


		OperatorNode resolv = new OperatorNode(Operator.AND);
		root.appendChild(resolv);

		a = new ValueArgument(SOLVE,"-s","solve a conflict");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			resolv.appendChild(n);


		OperatorNode xorResolve = new OperatorNode(Operator.XOR_ONE);
		resolv.appendChild(xorResolve);



		OperatorNode andResolve = new OperatorNode(Operator.AND);
		xorResolve.appendChild(andResolve);

		a = new ValueArgument(NUMBER,"-n","conflict number");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andResolve.appendChild(n);

		a = new ValueArgument(CHOICE,"-c","resolve choice number");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andResolve.appendChild(n);

		OperatorNode andResolveForce = new OperatorNode(Operator.AND);
		xorResolve.appendChild(andResolveForce);

		a = new ValueArgument(FORCE,"-f","resole all conflict by choising your own file");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andResolveForce.appendChild(n);


		setRootParser(root);

	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(LIST,queryString)!=null){

			HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
			int cpt=0;
			println("Numero conflit\t|\t\t\tChemin du fichier\t\t\t");
			for(Entry<String, ArrayList<Event>> entry : events.entrySet())
			{
				cpt++;

				println("\t"+cpt+"\t "+formatString(entry.getKey(),65,true));
			}


		}else if(getArgumentValue(DETAIL,queryString)!=null){
			HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
			try
			{
				int numberConflict = Integer.parseInt(getArgumentValue(NUMBER,queryString));
				if(numberConflict>0 && numberConflict<=events.size())
				{
					int cpt=0;
					int i=0;
					for(Entry<String, ArrayList<Event>> entry : events.entrySet())
					{
						cpt++;


						if(cpt==numberConflict)
						{

							println("File in conflict : "+entry.getKey());
							println("Version number\t|\t\t\tEvent creator\t\t\t| Conflict type");
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

			}
			catch(NumberFormatException e)
			{

			}
		}else if(getArgumentValue(SOLVE,queryString)!=null){
			if(getArgumentValue(FORCE,queryString)!=null){


				HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
				for(Entry<String, ArrayList<Event>> entry : events.entrySet())
				{
					Event res = forceSolveConflict(entry.getValue());
					if(res!=null)
						DataBaseManager.getInstance().saveEvent(res);
				}



			}else{
				HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
				try
				{
					int numberConflict = Integer.parseInt(getArgumentValue("numeroConflit",queryString));
					if(numberConflict>0 && numberConflict<=events.size())
					{
						int cpt=0;
						int numberVersion= Integer.parseInt(getArgumentValue("version",queryString));

						for(Entry<String, ArrayList<Event>> entry : events.entrySet())
						{
							cpt++;


							if(cpt==numberConflict && numberVersion>0 && numberVersion<=entry.getValue().size())
							{
								Event res = getSolveur(entry.getValue().get(numberConflict-1));
								DataBaseManager.getInstance().saveEvent(res);
							}
						}
					}
				}
				catch(NumberFormatException e)
				{

				}
			}

		}
		else
			println("Invalid Parameters");

	}


	private Event forceSolveConflict(ArrayList<Event> list)
	{
		Event res=null;
		long maxDate = Long.MIN_VALUE;
		for(Event e : list)
		{

			if(e.getOwner().equals(PeerSync.getInstance().getConf().getPeerID().toString()))
			{
				res = new Event(e);
				break;
			}

			if(e.getDate()>maxDate)
			{
				maxDate=e.getDate();
				res = new Event(e);
			}
		}
		return getSolveur(res);


	}

	/**	
	 * 
	 * 
	 * @return L'event a insere en BDD pour resoudre le conflit (en se basant sur @param e)
	 */
	private Event getSolveur(Event e)
	{
		Event res = new Event(e);
		res.setDate(System.currentTimeMillis());
		res.setStatus(Event.STATUS_LOCAL_FORCEOK);
		return res;


	}



}

