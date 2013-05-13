package com.peersync.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Shell extends Thread{

	/**
	 * @param args
	 */
	public void run()
	{

		mainloop();



	}

	public void commandTreatement(String command)
	{


		String pattern = "^addSharedFolder[\\s]+(\\S*)[\\s]+(\\S*)$";
		Pattern p = Pattern.compile(pattern);
		java.util.regex.Matcher m = p.matcher(command);
		if (m.matches()) {

			String rootPath = m.group(1);
			String peerGroup = m.group(2);
			System.out.println(rootPath+"  "+peerGroup);
		}




	}

	private void mainloop()
	{
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			// read what the user entered
			System.out.print("PeerSyncShell>");
			try {
				commandLine = console.readLine();
				if(!commandLine.equals(""))
				{
					commandTreatement(commandLine);

				}





			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

}
