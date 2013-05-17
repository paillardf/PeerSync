package com.peersync.cli;

import java.util.ArrayList;



public class OperatorNode extends Node{



	private Operator operator;
	private ArrayList<Node> childs;

	public OperatorNode(Operator o)
	{
		setOperator(o);
		childs=new ArrayList<Node>();
	}


	public OperatorNode(Operator o,ArrayList<Node> c)
	{
		this(o);
		setChilds(c);
	}



	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	public ArrayList<Node> getChilds() {
		return childs;
	}

	private void setChilds(ArrayList<Node> childs) {
		this.childs = childs;
	}

	public void appendChild(Node n)
	{
		childs.add(n);
	}
	
	public boolean parse(StringRef queryString)
	{
		return parse(queryString,true);
	}

	private boolean parse(StringRef queryString,boolean master)
	{
		if(operator==Operator.OR)
		{
			for(Node n : childs)
			{
				if(n instanceof OperatorNode)
					((OperatorNode)n).parse(queryString,false);
				else if (n instanceof ArgumentNode)
				{
					AbstractArgument arg = ((ArgumentNode) n).getArgument();
					if(arg.checkPresence(queryString.getData()))
						queryString.setData(arg.removeArgument(queryString.getData()));
				}

			}

		}

		else if(operator==Operator.XOR)
		{
			boolean found = false;
			for(Node n : childs)
			{
				boolean tmp=false;
				if(n instanceof OperatorNode)
					tmp = ((OperatorNode)n).parse(queryString,false);
				else if (n instanceof ArgumentNode)
				{
					AbstractArgument arg = ((ArgumentNode) n).getArgument();
					tmp = arg.checkPresence(queryString.getData());
					if(tmp)
						queryString.setData(arg.removeArgument(queryString.getData()));
				}

				if(found && tmp)
					return false;
				else if(!found && tmp)
					found = true;
			}

		}
		else if(operator==Operator.AND)
		{
			int nbOk = 0;
			int total = childs.size();
			for(Node n : childs)
			{
				if(n instanceof OperatorNode)
				{
					if (((OperatorNode)n).parse(queryString,false))
						nbOk++;
				}
				else if (n instanceof ArgumentNode)
				{
					AbstractArgument arg = ((ArgumentNode) n).getArgument();
					if(arg.checkPresence(queryString.getData()))
					{
						nbOk++;
						queryString.setData(arg.removeArgument(queryString.getData()));
					}
				}


			}
			if(nbOk!=0 && nbOk!=total)
				return false;

		}

		if(master && queryString.getData().replace(" ", "").length()>0)
			return false;
		return true;
	}

	public String toString()
	{
		String res= new String();


		boolean first=true;
		if(childs.size()>0)
		{
			res +="[";
			for(Node n : childs)
			{
				if(!first)
					res+=" "+operator.getDisplayedOperator()+" ";
				if(n instanceof OperatorNode)
					res += ((OperatorNode)n).toString();
				else if (n instanceof ArgumentNode)
				{
					AbstractArgument arg = ((ArgumentNode) n).getArgument();
					if(arg.getShortcut()!=null)
						res+=" "+arg.getShortcut()+" ";
					else
						res+=" "+arg.getName()+" ";
					if(arg instanceof ValueArgument)
						res+=" <value> ";
				}



				first = false;
			}
			res +="]";
		}
		return res;

	}


}
