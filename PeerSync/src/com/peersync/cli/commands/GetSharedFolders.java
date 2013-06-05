package com.peersync.cli.commands;

import java.util.ArrayList;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFolder;

public class GetSharedFolders extends AbstractCommand {


	public GetSharedFolders()
	{
		setDescription("Obtient la liste des dossiers partages");
		OperatorNode root = new OperatorNode(Operator.XOR);
		{
			ValueArgument a;
			try {
				a = new ValueArgument("peerGroupId","-pgid","PeerGroupID dont on veut obtenir les dossiers partages.\n Si absent, recuperation de l'ensemble des dossiers partages.");
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
	

		String pgid = getArgumentValue("peerGroupId",queryString);
		DataBaseManager db = DataBaseManager.getInstance();
		if(pgid!=null)
		{
			ArrayList<SharedFolder> sfl = db.getSharedFolders(pgid);
			if(sfl.size()>0)
			{
				println("    ID    \t    PeerGroupID    \t    Path");
				for( SharedFolder sf : sfl)
				{
					println(sf.getUID()+"\t"+sf.getPeerGroupUID()+"\t"+sf.getAbsFolderRootPath());
				}
			}
			else
				println("No shared folder in this peerGroup");
		}
		else
		{
			ArrayList<SharedFolder> sfl = db.getAllSharedDirectories();
			if(sfl.size()>0)
			{
				println("    ID    \t    PeerGroupID    \t    Path");
				for( SharedFolder sf : sfl)
				{
					println(sf.getUID()+"\t"+sf.getPeerGroupUID()+"\t"+sf.getAbsFolderRootPath());
				}
			}
			else
				println("No shared folders");
		}


	}






}

