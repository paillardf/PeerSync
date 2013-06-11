package com.peersync.cli.commands;

import java.io.File;
import java.util.ArrayList;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerSync;
import com.peersync.network.group.SyncPeerGroup;

public class ShareFolderCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("manage sharefolder");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);

		// NEW Share Folder
		OperatorNode andNew = new OperatorNode(Operator.AND);
		root.appendChild(andNew);

		ValueArgument a;
		ArgumentNode n ;
		a = new ValueArgument(CREATE,null,"create a new sharefolder");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNew.appendChild(n);


		a = new ValueArgument(PATH,"-f","path of the folder");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNew.appendChild(n);




		a = new ValueArgument(NAME,"-n","name of the share folder");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNew.appendChild(n);



		// List share folder

		OperatorNode andList = new OperatorNode(Operator.AND);
		root.appendChild(andList);


		a = new ValueArgument(LIST,"-l","list the sharefolder");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andList.appendChild(n);


		OperatorNode orOption = new OperatorNode(Operator.OR);

		a = new ValueArgument(PEERGROUP,"-pg","filter by peergroup");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			orOption.appendChild(n);

		andList.appendChild(orOption);

		
		setRootParser(root);
	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(CREATE,queryString)!=null){
			File f = new File(getArgumentValue(PATH,queryString));
			if(f.exists() && f.isDirectory())
			{
				DataBaseManager db = DataBaseManager.getInstance();
				SyncPeerGroup peerGroup = db.getPeerGroup(getArgumentValue(NAME,queryString));
				if(peerGroup!=null)
				{
					PeerSync.getInstance().addShareFolder(peerGroup.getPeerGroupID(), getArgumentValue(PATH,queryString), getArgumentValue(NAME,queryString));

				}
				else
					println("Invalid peerGroup");
			}
			else
				println("Invalid folder path");
		}else if (getArgumentValue(LIST,queryString)!=null){
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
		else
			println("Invalid Parameters");

	}






}

