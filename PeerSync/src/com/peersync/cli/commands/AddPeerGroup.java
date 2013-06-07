package com.peersync.cli.commands;

import java.io.IOException;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.network.PeerSync;

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
				e.printStackTrace();
			}

		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {

		String passwd = getArgumentValue("password",queryString);
		String filepath = getArgumentValue("filePath",queryString);
		filepath = cleanFilePath(filepath);
		
		String description = getArgumentValue("description",queryString);
		String name = getArgumentValue("name",queryString);
		if(passwd!=null && filepath!=null)
		{
			
			try {
				PeerSync.getInstance().importPeerGroup(filepath, passwd.toCharArray());
			} catch (IOException e) {
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

