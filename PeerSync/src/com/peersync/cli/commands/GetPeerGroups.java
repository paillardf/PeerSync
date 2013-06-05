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
import com.peersync.network.group.SyncPeerGroup;

public class GetPeerGroups extends AbstractCommand {


	public GetPeerGroups()
	{
		setDescription("Obtient la liste des peer groups ");



	}

	@Override
	public void requestHandler(String queryString) {


		DataBaseManager db = DataBaseManager.getInstance();

		ArrayList<SyncPeerGroup> pgs = db.getPeerGroups();
		if(pgs.size()>0)
		{
			println("\t\tNom\t\t|\t\t\t\tDescription\t\t\t\t");
			for( SyncPeerGroup pg : pgs)
			{
				println(formatString(pg.getPeerGroupName(),35,true)+" "+formatString(pg.getPeerGroupName(),68,true));
			}
		}
		else
			println("No PeerGroup");



	}






}

