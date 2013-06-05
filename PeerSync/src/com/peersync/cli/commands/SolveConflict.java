package com.peersync.cli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.Event;
import com.peersync.network.PeerSync;

public class SolveConflict extends AbstractCommand {


	public SolveConflict()
	{
		setDescription("Resouds les conflits");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);
		OperatorNode and = new OperatorNode(Operator.AND);
		root.appendChild(and);
		{
			{
				BooleanArgument a;
				try {
					a = new BooleanArgument("forceSolve","-f","Resouds tous les conflits en selectionnant a chaque fois la version de l'utilisateur, ou a defaut la version la plus recente");
					ArgumentNode n = new ArgumentNode(a);
					if(allArguments.addArgument((AbstractArgument)a))
						root.appendChild(n);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ValueArgument a;
			try {
				a = new ValueArgument("numeroConflit","-nc","Numero du conflit");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("version","-v","Version choisie");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {
		String force = getArgumentValue("force",queryString);
		HashMap<String,ArrayList<Event>>  events = DataBaseManager.getInstance().getEventsInConflict();
		if(force!=null)
		{
			for(Entry<String, ArrayList<Event>> entry : events.entrySet())
			{
				Event res = ForceConflict(entry.getValue());
				if(res!=null)
					DataBaseManager.getInstance().saveEvent(res);
			}

		}
		else
		{
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
							Event res = SolveConflict(entry.getValue().get(numberConflict-1));
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




	/**	
	 * 
	 * 
	 * @return L'event a insere en BDD pour resoudre le conflit (en se basant sur @param e)
	 */
	private Event SolveConflict(Event e)
	{
		Event res = new Event(e);
		res.setDate(System.currentTimeMillis());
		res.setStatus(Event.STATUS_LOCAL_FORCEOK);
		return res;


	}


	private Event ForceConflict(ArrayList<Event> list)
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
		return SolveConflict(res);


	}






}

