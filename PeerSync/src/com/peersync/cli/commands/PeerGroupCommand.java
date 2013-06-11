package com.peersync.cli.commands;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.network.PeerSync;
import com.peersync.network.group.BasicPeerGroup;
import com.peersync.network.group.SyncPeerGroup;

public class PeerGroupCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("manage peergroup");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);

		// NEW GROUP
		OperatorNode andNewPeerGroup = new OperatorNode(Operator.AND);
		root.appendChild(andNewPeerGroup);

		ValueArgument a;
		ArgumentNode n ;
		a = new ValueArgument(CREATE,null,"create a new peergroup");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNewPeerGroup.appendChild(n);


		a = new ValueArgument(NAME,"-n","peergroup name");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNewPeerGroup.appendChild(n);




		a = new ValueArgument(DESCRIPTION,"-d","peergroup description");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andNewPeerGroup.appendChild(n);



		// IMPORT GROUP

		OperatorNode andImportPeerGroup = new OperatorNode(Operator.AND);
		root.appendChild(andImportPeerGroup);


		a = new ValueArgument(IMPORT,null,"import a peergroup from a invitation file");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andImportPeerGroup.appendChild(n);


		a = new ValueArgument(PATH,"-f","path to the invitation file");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andImportPeerGroup.appendChild(n);

		a = new ValueArgument(PASSWORD,"-p","invitation file encryption password");
		n = new ArgumentNode(a);
		if(allArguments.addArgument((AbstractArgument)a))
			andImportPeerGroup.appendChild(n);


		// EXPORT
		{

			OperatorNode andExport = new OperatorNode(Operator.AND);
			root.appendChild(andExport);


			a = new ValueArgument(EXPORT,null,"export a peergroup invitation file");
			n = new ArgumentNode(a);
			if(allArguments.addArgument((AbstractArgument)a))
				andExport.appendChild(n);

			
			a = new ValueArgument(PEERGROUP,"-pg","name of the peergroup to export");
			n = new ArgumentNode(a);
			if(allArguments.addArgument((AbstractArgument)a))
				andExport.appendChild(n);
			

			a = new ValueArgument(PATH,"-f","save file path");
			n = new ArgumentNode(a);
			if(allArguments.addArgument((AbstractArgument)a))
				andExport.appendChild(n);

			a = new ValueArgument(PASSWORD,"-p","key encryption password");
			n = new ArgumentNode(a);
			if(allArguments.addArgument((AbstractArgument)a))
				andExport.appendChild(n);
		}

		setRootParser(root);
	}



	@Override
	public void requestHandler(String queryString) {

		if(getArgumentValue(IMPORT,queryString)!=null){
			try {
				PeerSync.getInstance().importPeerGroup(getArgumentValue(PATH,queryString), getArgumentValue(PASSWORD,queryString).toCharArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(getArgumentValue(EXPORT,queryString)!=null){
			SyncPeerGroup pg = DataBaseManager.getInstance().getPeerGroup(getArgumentValue(PEERGROUP,queryString));
			if(pg==null){
				println("Peergroup Name invalid");
			}
			try {
				PeerSync.getInstance().exportPeerGroup(pg.getPeerGroupID(), getArgumentValue(PATH,queryString), getArgumentValue(PASSWORD,queryString).toCharArray());
			} catch (UnrecoverableKeyException | KeyStoreException
					| NoSuchAlgorithmException | URISyntaxException
					| IOException e) {
				e.printStackTrace();
			}
		}else if(getArgumentValue(CREATE,queryString)!=null){
			PeerSync.getInstance().createPeerGroup( getArgumentValue(NAME,queryString), getArgumentValue(DESCRIPTION,queryString));
		}else if(getArgumentValue(LIST,queryString)!=null){
			ArrayList<BasicPeerGroup> list = PeerSync.getInstance().getPeerGroupManager().getPeerGroupList();
			println("\t\tNom\t\t|\t\t\t\tDescription\t\t\t\t");
			for (int i = 0; i < list.size(); i++) {
				println(formatString(list.get(i).getPeerGroupName(),35,true)+" "+formatString(list.get(i).getStatus().toString(), 20, true)+" "+formatString(list.get(i).getDescription(),68,true));
			}
		}
		else
			println("Invalid Parameters");

	}






}

