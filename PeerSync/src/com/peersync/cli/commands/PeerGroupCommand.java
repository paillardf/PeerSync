package com.peersync.cli.commands;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.exceptions.BasicPeerGroupException;
import com.peersync.network.PeerSync;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.group.SyncPeerGroup;
import com.peersync.tools.Constants;

public class PeerGroupCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("manage peergroup");
		ValueArgument a;
		BooleanArgument b;
		OperatorNode root = createOperatorNode(Operator.XOR_ONE,null);

		// NEW GROUP
		b = new BooleanArgument(CREATE,null,"create a new peergroup");
		OperatorNode andNewPeerGroup = createOperatorNode(Operator.AND,b,root);


		a = new ValueArgument(NAME,"-n","peergroup name");
		createArgumentNode(a,andNewPeerGroup);




		a = new ValueArgument(DESCRIPTION,"-d","peergroup description");
		createArgumentNode(a,andNewPeerGroup);



		// IMPORT GROUP
		b = new BooleanArgument(IMPORT,null,"import a peergroup from a invitation file");
		OperatorNode andImportPeerGroup = createOperatorNode(Operator.AND,b,root);

		a = new ValueArgument(PATH,"-f","path to the invitation file");
		createArgumentNode(a,andImportPeerGroup);

		a = new ValueArgument(PASSWORD,"-p","invitation file encryption password");
		createArgumentNode(a,andImportPeerGroup);


		// EXPORT
		b = new BooleanArgument(EXPORT,null,"export a peergroup invitation file");
		OperatorNode andExport = createOperatorNode(Operator.AND,b,root);




		a = new ValueArgument(PEERGROUP,"-pg","name of the peergroup to export");
		createArgumentNode(a,andExport);


		a = new ValueArgument(PASSWORD,"-p","key encryption password");
		createArgumentNode(a,andExport);

	
		a = new ValueArgument(PATH,"-f","save file path");
		createArgumentNode(a,andExport);


		//Start stop 
		OperatorNode xorStartStop = createOperatorNode(Operator.XOR_ONE,true,"To start or stop a shared folder",root);
		a = new ValueArgument(START,null,"start a peergroup");
		createArgumentNode(a,xorStartStop);
		
		a = new ValueArgument(STOP,null,"stop a peergroup");
		createArgumentNode(a,xorStartStop);
		
		
		b = new BooleanArgument(LIST,"-l","list peergroups");
		OperatorNode orList = createOperatorNode(Operator.OR,b,root);



		a = new ValueArgument(PEERGROUP,"-pg","filter by peergroup");
		createArgumentNode(a,orList);
		
		
		
		setRootParser(root);
	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(IMPORT,queryString)!=null){
			try {
				PeerSync.getInstance().importPeerGroup(cleanFilePath(getArgumentValue(PATH,queryString)), getArgumentValue(PASSWORD,queryString).toCharArray());
			} catch (IOException e) {
				println(e.getMessage());
			}
		}else if(getArgumentValue(EXPORT,queryString)!=null){
			SyncPeerGroup pg = DataBaseManager.getInstance().getPeerGroup(getArgumentValue(PEERGROUP,queryString));
			if(pg==null){
				println("Peergroup Name invalid");
			}
			try {
				PeerSync.getInstance().exportPeerGroup(pg.getPeerGroupID(), cleanFilePath(getArgumentValue(PATH,queryString)), getArgumentValue(PASSWORD,queryString).toCharArray());
			} catch (UnrecoverableKeyException | KeyStoreException
					| NoSuchAlgorithmException | URISyntaxException
					| IOException e) {
				println(e.getMessage());
			}
		}else if(getArgumentValue(CREATE,queryString)!=null){
			PeerSync.getInstance().createPeerGroup( getArgumentValue(NAME,queryString), getArgumentValue(DESCRIPTION,queryString));
		}else if(getArgumentValue(LIST,queryString)!=null){
			ArrayList<BasicPeerGroup> list = PeerSync.getInstance().getPeerGroupManager().getPeerGroupList();
			println("\t\tNom\t\t|\t\t\t\tDescription\t\t\t\t");
			for (int i = 0; i < list.size(); i++) {
				println(formatString(list.get(i).getPeerGroupName(),35,true)+" "+formatString(list.get(i).getStatus().toString(), 20, true)+" "+formatString(list.get(i).getDescription(),68,true));
			}
		}else if(getArgumentValue(START,queryString)!=null){
			BasicPeerGroup pg = DataBaseManager.getInstance().getPeerGroup(getArgumentValue(START,queryString));
			if(pg==null){
				println("Peergroup Name invalid");
			}
			try {
				if(pg.getStatus()==Thread.State.BLOCKED)
				{
				try {
					pg.initialize(PeerSync.getInstance().getPeerGroupManager().getNetPeerGroup());
				} catch (Exception e) {
					println(e.getMessage());
				}
				PeerSync.getInstance().getPeerGroupManager().getPeerGroup(pg.getPeerGroupID()).start();
				}
				else
					println("Peergroup already started");
			} catch (BasicPeerGroupException e) {
				println(e.getMessage());
			}
			
		}else if(getArgumentValue(STOP,queryString)!=null){
			SyncPeerGroup pg = DataBaseManager.getInstance().getPeerGroup(getArgumentValue(STOP,queryString));
			if(pg==null){
				println("Peergroup Name invalid");
			}
			try {
				PeerSync.getInstance().getPeerGroupManager().getPeerGroup(pg.getPeerGroupID()).stop();
			} catch (BasicPeerGroupException e) {
				println(e.getMessage());
			}
			
		}
		else
			println("Invalid Parameters");

	}






}

