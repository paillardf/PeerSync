package com.peersync.cli.commands;

import com.peersync.cli.AbstractCommand;
import com.peersync.network.PeerSync;

public class ExitCommand extends AbstractCommand {


	@Override
	protected void iniCommand() throws Exception {
		setDescription("Exit the application");
		
	}



	@Override
	public void requestHandler(String queryString) {

		println("Shutting down ...");
		PeerSync.getInstance().exit();
		

	}






}

