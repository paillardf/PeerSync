package com.peersync.cli.commands;

import java.io.File;

import com.peersync.cli.AbstractArgument;
import com.peersync.cli.AbstractCommand;
import com.peersync.cli.ArgumentNode;
import com.peersync.cli.Node.Operator;
import com.peersync.cli.OperatorNode;
import com.peersync.cli.ValueArgument;
import com.peersync.data.DataBaseManager;
import com.peersync.models.SharedFolder;

public class AddSharedFolder extends AbstractCommand {


	public AddSharedFolder()
	{
		setDescription("Ajoute un dossier partage");
		OperatorNode root = new OperatorNode(Operator.AND);
		{
			ValueArgument a;
			try {
				a = new ValueArgument("path","-p","Chemin (absolu) du dossier");
				ArgumentNode n = new ArgumentNode(a);
				if(allArguments.addArgument((AbstractArgument)a))
					root.appendChild(n);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setRootParser(root);



	}

	@Override
	public void requestHandler(String queryString) {
		AbstractArgument argPath = allArguments.getArgumentByName("path");

		String absPath = argPath.getValue(queryString);
		if(absPath!=null)
		{

			File f = new File(absPath);
			if(f.exists() && f.isDirectory())
			{
				DataBaseManager db = DataBaseManager.getInstance();
				db.saveSharedFolder(new SharedFolder("5000", "toBeReplaced",absPath ));
			}
			else
				println("Invalid rootPath");
		}	
	}






}

