package com.peersync.cli.commands;

import java.io.File;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.network.PeerSync;
import com.peersync.network.group.SyncPeerGroup;

public class AddSharedFolder extends AbstractCommand {


	public AddSharedFolder()
	{
		setDescription("Ajoute ou met a jour un dossier partage");
		OperatorNode root = new OperatorNode(Operator.AND);
		{
			ValueArgument a;
			try {
				a = new ValueArgument("path","-p","Chemin (absolu) du dossier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				a = new ValueArgument("name","-n","Nom du dossier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("peerGroupName","-pg","PeerGroup du sharedFolder");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {


		String absPath = getArgumentValue("path",queryString);
		absPath = cleanFilePath(absPath);
		String pgName = getArgumentValue("peerGroupName",queryString);
		String name = getArgumentValue("name",queryString);
		if(absPath!=null && pgName!=null && name!=null)
		{

			File f = new File(absPath);
			if(f.exists() && f.isDirectory())
			{
				DataBaseManager db = DataBaseManager.getInstance();
				SyncPeerGroup peerGroup = db.getPeerGroup(pgName);
				if(peerGroup!=null)
				{
					PeerSync.getInstance().addShareFolder(peerGroup.getPeerGroupID(), absPath, name);

				}
				else
					println("Invalid peerGroup");
			}
			else
				println("Invalid Parameters");
		}	
	}






}

