package com.peersync.cli.commands;

import java.io.IOException;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.exceptions.JxtaNotInitializedException;


public class ScanService extends AbstractCommand {


	public ScanService()
	{
		setDescription("Demarre, arrete ou redemarre le service de scan");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);
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
		{
			String argValue =  getArgumentValue("start",queryString);
			if(argValue!=null)
			{
				try {
					com.peersync.events.ScanService.getInstance().startService();
					println("Service started");
				} catch (IOException | JxtaNotInitializedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}
		{
			String argValue =  getArgumentValue("stop",queryString);
			if(argValue!=null)
			{
				com.peersync.events.ScanService.getInstance().stopService();
				println("Service stopped");
			}

		}
		{
			String argValue =  getArgumentValue("restart",queryString);
			if(argValue!=null)
			{
				com.peersync.events.ScanService.getInstance().stopService();
				try {
					com.peersync.events.ScanService.getInstance().startService();
					println("Service restarted");
				} catch (IOException | JxtaNotInitializedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}
		


	}





}
