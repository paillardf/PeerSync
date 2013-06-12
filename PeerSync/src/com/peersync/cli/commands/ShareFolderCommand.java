package com.peersync.cli.commands;

import java.io.File;
import java.util.ArrayList;

import com.peersync.cli.AbstractCommand;
import com.peersync.cli.BooleanArgument;
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
		OperatorNode root = createOperatorNode(Operator.XOR_ONE,null);

		ValueArgument a;
		BooleanArgument b;

		// NEW Share Folder
		b = new BooleanArgument(CREATE,null,"create or update a new sharefolder");
		OperatorNode andNew = createOperatorNode(Operator.AND,b,root);
		
		


		a = new ValueArgument(PATH,"-f","path of the folder");
		createArgumentNode(a,andNew);


		a = new ValueArgument(PEERGROUP,"-pg","peergroup's name");
		createArgumentNode(a,andNew);

		a = new ValueArgument(NAME,"-n","name of the share folder");
		createArgumentNode(a,andNew);



		// List share folder


		b = new BooleanArgument(LIST,"-l","list the sharefolder");
		OperatorNode orList = createOperatorNode(Operator.OR,b,root);



		a = new ValueArgument(PEERGROUP,"-pg","filter by peergroup");
		createArgumentNode(a,orList);

		
		setRootParser(root);
	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(CREATE,queryString)!=null){
			File f = new File(cleanFilePath(getArgumentValue(PATH,queryString)));
			if(f.exists() && f.isDirectory())
			{
				DataBaseManager db = DataBaseManager.getInstance();
				SyncPeerGroup peerGroup = db.getPeerGroup(getArgumentValue(PEERGROUP,queryString));
				if(peerGroup!=null)
				{
					PeerSync.getInstance().addShareFolder(peerGroup.getPeerGroupID(), f.getAbsolutePath(), getArgumentValue(NAME,queryString));

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
					println("\t\tPeerGroupName\t\t|\t\t\tPath\t\t\t");
					for( SharedFolder sf : sfl)
					{
						println(formatString(sf.getName(), 45, true)+formatString(sf.getAbsFolderRootPath(), 80, true));
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
					println("\tPeerGroupName\t|\t\t\tPath\t\t\t");
					for( SharedFolder sf : sfl)
					{
						println(formatString(sf.getName(), 29, true)+sf.getAbsFolderRootPath());
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

