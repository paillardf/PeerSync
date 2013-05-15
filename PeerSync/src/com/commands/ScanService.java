package com.commands;

public class ScanService extends AbstractCommand {

	// start, stop restart
	public void exec(String QueryString)
	{
		println("TOTO");
	}


	public void help()
	{
		println("NAME");
        println("     cat  - ");
        println(" ");
        println("SYNOPSIS");
        println(" ");
        println("     cat [-p] <objectName>");
        println(" ");
        println("DESCRIPTION");
        println(" ");
        println("'cat' displays on stdout the contents of objects stored in");
        println("environment variables. 'cat' knows how to display a limited");
        println("(but growing) set of JXTA object types : ");
        println("  - Advertisement " );
        println("  - Credentials " );
        println("  - Document " );
        println("  - StructuredDocument " );
        println("  - Message " );
        println("  - PeerInfoResponseMessage " );
        println(" ");
        println("If you are not sure, try to cat the object anyway -- 'cat'");
        println("will try to display the object as best it can.");
        println(" ");
        println("OPTIONS");
        println(" ");
        println("    [-p]   Pretty display");
        println(" ");
        println("EXAMPLE");
        println(" ");
        println("    JXTA> importfile -f /home/tra/myfile myfile");
        println("    JXTA> cat -p myfile");
        println(" ");
        println("This command imports the file '/home/tra/myfile' into the");
        println("'myfile' environment variable and displays it on stdout.");
        println(" ");
        println("SEE ALSO");
        println("    more env" );
	}

}
