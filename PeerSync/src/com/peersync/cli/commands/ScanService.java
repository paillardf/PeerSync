package com.peersync.cli.commands;

import java.io.File;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.events.EventsManager;
import com.peersync.models.SharedFolder;


public class ScanService extends AbstractCommand {


	public ScanService()
	{
		setDescription("Demarre, arrete ou redemarre le service de scan");
		OperatorNode root = new OperatorNode(Operator.XOR);
		{
			BooleanArgument a;
			try {
				a = new BooleanArgument("start","-st","Demarre le service");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		{
			BooleanArgument a;
			try {
				a = new BooleanArgument("stop","-sp","Arrete le service");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		{
			BooleanArgument a;
			try {
				a = new BooleanArgument("restart","-r","Redemarre le service");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {
		AbstractArgument argStart = allArguments.getArgumentByName("start");
		AbstractArgument argStop = allArguments.getArgumentByName("stop");
		AbstractArgument argRestart = allArguments.getArgumentByName("restart");

		{
			String argValue = argStart.getValue(queryString);
			if(argValue!=null)
			{
				EventsManager.getEventsManager().startService();
				println("Service started");
			}

		}
		{
			String argValue = argStop.getValue(queryString);
			if(argValue!=null)
			{
				EventsManager.getEventsManager().stopService();
				println("Service stopped");
			}

		}
		{
			String argValue = argRestart.getValue(queryString);
			if(argValue!=null)
			{
				EventsManager.getEventsManager().stopService();
				EventsManager.getEventsManager().startService();
				println("Service restarted");
			}

		}
		


	}





}
