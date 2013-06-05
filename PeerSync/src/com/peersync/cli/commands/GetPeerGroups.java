package com.peersync.cli.commands;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.BooleanArgument;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFolder;
import com.peersync.network.PeerSync;
import com.peersync.network.group.SyncPeerGroup;

public class GetPeerGroups extends AbstractCommand {


	public GetPeerGroups()
	{
		setDescription("Obtient la liste des peer groups ");
		OperatorNode root = new OperatorNode(Operator.OR);
		OperatorNode and = new OperatorNode(Operator.AND);
		root.appendChild(and);
		{
			ValueArgument a;
			try {
				a = new ValueArgument("name","-n","Nom du peerGroup");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				a = new ValueArgument("filePath","-f","Emplacement du fichier d'inviation");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			try {
				a = new ValueArgument("password","-p","Mot de passe de chiffrage du fichier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BooleanArgument b;
			try {
				b = new BooleanArgument("export","-e","Export de la configuration");
				ArgumentNode n = new ArgumentNode(b);
				if(allArguments.addArgument((AbstractArgument)b))
					and.appendChild(n);

			} catch (Exception e) {
				e.printStackTrace();
			}



		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {
		String export = getArgumentValue("export",queryString);
		String name = getArgumentValue("name",queryString);
		String filepath = getArgumentValue("filePath",queryString);
		String passwd = getArgumentValue("password",queryString);
		
		if(export!=null && name!=null && filepath!=null && passwd!=null)
		{
			DataBaseManager db = DataBaseManager.getInstance();
			SyncPeerGroup pg = db.getPeerGroup(name);
			if(pg!=null)
			{
				try {
					PeerSync.getInstance().exportPeerGroup(pg.getPeerGroupID(), filepath, passwd.toCharArray());
					println("OKKK");
				} catch (UnrecoverableKeyException | KeyStoreException
						| NoSuchAlgorithmException | URISyntaxException
						| IOException e) {
					e.printStackTrace();
				}
				
			}
			else
				println("Invalid PeerGroup");
		}
		else
		{

			DataBaseManager db = DataBaseManager.getInstance();

			ArrayList<SyncPeerGroup> pgs = db.getPeerGroups();
			if(pgs.size()>0)
			{
				println("\t\tNom\t\t|\t\t\t\tDescription\t\t\t\t");
				for( SyncPeerGroup pg : pgs)
				{
					println(formatString(pg.getPeerGroupName(),35,true)+" "+formatString(pg.getDescription(),68,true));
				}
			}
			else
				println("No PeerGroup");
		}



	}






}

