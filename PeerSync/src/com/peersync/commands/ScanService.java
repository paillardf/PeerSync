package com.peersync.commands;

import com.peersync.commands.Node.Operator;

public class ScanService extends AbstractCommand {


	public ScanService()
	{
		setDescription("Demarre, arrete ou redemarre le service de scan");
		OperatorNode root = new OperatorNode(Operator.AND);
		OperatorNode orGroup = new OperatorNode(Operator.OR);
		root.appendChild(orGroup);
		{
			BooleanArgument a;
			try {
				a = new BooleanArgument("start","-st","Demarre le service");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					orGroup.appendChild(n);

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
					orGroup.appendChild(n);

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
					orGroup.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		{
			ValueArgument a;
			try {
				a = new ValueArgument("test","-t","Juste un test");
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

	// start, stop restart
	public void exec(String queryString)
	{
		queryString=" "+queryString+" ";
		if(checkGroupValidity(queryString))
			println("Cette commande est valide !");
		else
			help();

	}




}
