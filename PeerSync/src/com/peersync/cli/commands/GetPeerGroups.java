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

public class GetPeerGroups extends AbstractCommand {


	public GetPeerGroups()
	{
		setDescription("Obtient la liste des peer groups ");
		


	}

	@Override
	public void requestHandler(String queryString) {
	

		String pgid = getArgumentValue("peerGroupName",queryString);
		DataBaseManager db = DataBaseManager.getInstance();
		if(pgid!=null)
		{
			ArrayList<SharedFolder> sfl = db.getSharedFolders(pgid);
			if(sfl.size()>0)
			{
				println("\tNom\t    PeerGroupID    \t    Path");
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

