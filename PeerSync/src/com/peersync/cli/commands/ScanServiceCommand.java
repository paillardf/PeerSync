package com.peersync.cli.commands;

import java.io.IOException;

import com.peersync.cli.AbstractCommand;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.exceptions.JxtaNotInitializedException;

public class ScanServiceCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("manage scanservice");
		OperatorNode root = createOperatorNode(Operator.XOR_ONE,null);

	

		ValueArgument a;
		
		a = new ValueArgument(START,"-st","start the scan service");
		createArgumentNode(a,root);


		a = new ValueArgument(STOP,"-sp","stop the scan service");
		createArgumentNode(a,root);




		a = new ValueArgument(RESTART,"-r","restart the scan service");
		createArgumentNode(a,root);
		
		setRootParser(root);


	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(START,queryString)!=null){
			try {
				com.peersync.events.ScanService.getInstance().startService();
				println("Service started");
			} catch (IOException | JxtaNotInitializedException e) {
				e.printStackTrace();
			}
		}else if (getArgumentValue(RESTART,queryString)!=null){
			com.peersync.events.ScanService.getInstance().stopService();
			println("Service stopped");
			try {
				com.peersync.events.ScanService.getInstance().startService();
				println("Service started");
			} catch (IOException | JxtaNotInitializedException e) {
				e.printStackTrace();
			}
			
		}else if (getArgumentValue(STOP,queryString)!=null){
				com.peersync.events.ScanService.getInstance().stopService();
				println("Service stopped");
		}
		else
			println("Invalid Parameters");

	}






}

