package com.peersync.cli.commands;

import java.io.File;
import java.io.IOException;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

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

public class AddPeerGroup extends AbstractCommand {


	public AddPeerGroup()
	{
		setDescription("Ajoute ou met a jour un peerGroup");
		OperatorNode root = new OperatorNode(Operator.XOR_ONE);
		OperatorNode andAddManually = new OperatorNode(Operator.AND);
		OperatorNode andAddInvitation = new OperatorNode(Operator.AND);
		root.appendChild(andAddManually);
		root.appendChild(andAddInvitation);
		{
			ValueArgument a;

			try {
				a = new ValueArgument("name","-n","Nom du dossier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					andAddManually.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("description","-d","Description");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					andAddManually.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			try {
				a = new ValueArgument("filePath","-f","Chemin du fichier d'invitation");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					andAddInvitation.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = new ValueArgument("password","-p","Mot de passe de chiffrage de l'invitation");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					andAddInvitation.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {

		String passwd = getArgumentValue("password",queryString);
		String filepath = getArgumentValue("filePath",queryString);
		
		String description = getArgumentValue("description",queryString);
		String name = getArgumentValue("name",queryString);
		if(passwd!=null && filepath!=null)
		{
			
			try {
				PeerSync.getInstance().importPeerGroup(filepath, passwd.toCharArray());
				println("OKKK");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(description!=null && name!=null)
		{
			PeerSync.getInstance().createPeerGroup(name,description);

		}
		else
			println("Invalid Parameters");

	}






}

