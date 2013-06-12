package com.peersync.cli.commands;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;

public class SandboxCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("test");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);

		//New Group
		OperatorNode andNewPeerGroup = new OperatorNode(Operator.AND);

		root.appendChild(andNewPeerGroup);



		ValueArgument a;
		BooleanArgument b;
		ArgumentNode n ;
		a = new ValueArgument(CREATE,null,"create a new peergroup");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNewPeerGroup.appendChild(n);


		a = new ValueArgument(NAME,"-n","peergroup name");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
		{
			andNewPeerGroup.appendChild(n);

		}

		//start
		b = new BooleanArgument(START,"-st","peergroup name");
		allArguments.addArgument((AbstractArgument)b);
		OperatorNode andStart = new OperatorNode(Operator.AND,b);
		andStart.appendChild(n);
		root.appendChild(andStart);
		//if(allArguments.addArgument((AbstractArgument)b))
		///	andStart.appendChild(n);
		//else
		//	System.out.println("fail");



		setRootParser(root);
	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(START,queryString)!=null){
			println("start "+getArgumentValue(NAME,queryString));
		}else if(getArgumentValue(CREATE,queryString)!=null){
			println("create "+getArgumentValue(NAME,queryString));
		}
		else
			println("Invalid Parameters");

	}






}

