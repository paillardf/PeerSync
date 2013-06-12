package com.peersync.cli;

import jline.internal.Configuration;

import com.peersync.cli.Node.Operator;

public abstract class AbstractCommand {

	// Set Shortcut(vérifier que pas de doublons)
	// Set Nom (vérifier que pas de doublons)
	// List arguments (list d'objet, tous args)
	// Groupes OR (list obj) --> ex start, stop et restart ou exclusif
	// Groupes AND (list obj) pour des args interdépendants

	
	protected final static String PASSWORD = "password";
	protected final static String DESCRIPTION = "description";
	protected final static String NAME = "name";
	protected final static String PATH = "path";
	protected final static String CREATE = "create";
	protected final static String IMPORT = "import";
	protected final static String EXPORT = "export";
	protected final static String LIST = "list";
	protected final static String PEERGROUP = "peergroup";
	protected final static String DETAIL = "detail";
	protected final static String NUMBER = "number";
	protected final static String SOLVE = "solve";
	protected final static String CHOICE = "choice";
	protected final static String FORCE = "force";
	
	protected final static String START = "start";
	protected final static String STOP = "stop";
	protected final static String RESTART = "restart";
	
	protected ArgumentsList allArguments = new ArgumentsList();
	protected OperatorNode rootParser=null;

	private String description;
	private String name;

	public AbstractCommand()
	{
		String name = getClass().getSimpleName();
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		setName(name);
		try {
			iniCommand();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected abstract void iniCommand() throws Exception;

	public final void exec(String queryString)
	{
		if(parse(queryString))
			requestHandler(queryString);
		else
			help();
	}
	
	public OperatorNode createOperatorNode(Operator op,OperatorNode parent)
	{
		OperatorNode res = new OperatorNode(op);
		if(parent!=null)
			parent.appendChild(res);
		return res;
	}
	
	//Pour construire un node "group leader"
	public OperatorNode createOperatorNode(Operator op,BooleanArgument groupName,OperatorNode parent)
	{
		allArguments.addArgument((AbstractArgument)groupName);
		OperatorNode res = new OperatorNode(op,groupName);
		if(parent!=null)
			parent.appendChild(res);
		return res;
	}
	
	public OperatorNode createOperatorNode(Operator op,boolean groupLeader,String description,OperatorNode parent)
	{
		
		OperatorNode res = new OperatorNode(op,true,description);
		if(parent!=null)
			parent.appendChild(res);
		return res;
	}
	
	
	public ArgumentNode createArgumentNode(AbstractArgument arg,OperatorNode parent)
	{
		allArguments.addArgument((AbstractArgument)arg);
		ArgumentNode res = new ArgumentNode(arg);
		if(parent!=null)
			parent.appendChild(res);
		return res;
	}

	public static String cleanFilePath(String s)
	{
		if(s==null)
			return s;
		String os = Configuration.getOsName();
		boolean OS_IS_WINDOWS = os.contains("windows");
		s=s.replace("\\'", "'");
		if(OS_IS_WINDOWS)
			s=s.replace("/", "\\");
		return s;
	}

	public abstract void requestHandler(String queryString);

	public void setRootParser(OperatorNode r)
	{
		rootParser=r;
	}

	public void help()
	{
		println("NAME");
		println(" ");
		println("\t"+getName());
		println(" ");
		println("SYNOPSIS");
		println(" ");
		println("\t"+getName()+" "+rootParser.toString());
		println(" ");
		println("DESCRIPTION");
		println(" ");
		println("\t"+getDescription());
		println(" ");
		println("OPTIONS");
		println(" ");
		//les arguments
		printArgs(rootParser, 0);



		

		println(" ");

	}

	public void printArg(AbstractArgument arg,int level)
	{
		for(int i=0;i<level;i++)
			print("\t");
		print("\t");
		if(arg.getShortcut()!=null)
			print(arg.getShortcut()+", ");
		print(arg.getName());
		if(arg instanceof ValueArgument)
			print(" <value>");
		println(" : "+arg.getDescription());
	}
	
	public void printArgs(OperatorNode op, int level)
	{

		if(op.getChilds().size()>0)
		{
			for(Node n : op.getChilds())
			{
				
				if(n instanceof OperatorNode)
				{
					if(((OperatorNode) n).getGroupName()!=null)
					{
						println("");
						printArg((AbstractArgument)((OperatorNode) n).getGroupName(),level);
						printArgs((OperatorNode)n, level+1);
						println("");
					}
					else if(((OperatorNode) n).getGroupLeader())
					{
						println("");
						for(int i=0;i<level;i++)
							print("\t");
						print("\t");
						println(((OperatorNode) n).getDescription());
						printArgs((OperatorNode)n, level+1);
						println("");
					}
					else
						printArgs((OperatorNode)n, level);
				}
				else if (n instanceof ArgumentNode)
				{
					printArg(((ArgumentNode)n).getArgument(),level);
				}
			}
		}


	}
	
	public void println(String disp)
	{
		System.out.println(disp);
	}


	public void print(String disp)
	{
		System.out.print(disp);
	}






	public boolean parse(String queryString)
	{
		StringRef sref = new StringRef(queryString);
		if(rootParser!=null)
			return rootParser.parse(sref);
		else
			return true;


	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}

	public static String formatString(String s,int size,boolean truncBegin)
	{
		String res;
		if(s.length()-size-3<0)
		{
			res= s;
		}
		else
		{
			if(truncBegin)
				res = "..."+s.substring(s.length()-size-3);
			else
				res = s.substring(0,s.length()-size-3)+"...";

		}

		int cpt=res.length();
		for(;cpt<size;cpt++)
		{
			if(cpt%2==0)
				res=" "+res;
			else
				res+=" ";

		}
		return res;

	}

	public String getArgumentValue(String argName,String queryString)
	{
		AbstractArgument arg = allArguments.getArgumentByName(argName);
		if(arg!=null)
			return arg.getValue(queryString);
		else
			return null;
	}

}
