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

			int nbOk=0;
			for(Node n : childs)
			{
				if(n instanceof OperatorNode)
				{
					if(((OperatorNode)n).parse(queryString,false))
						nbOk++;
					else if(operator==Operator.AND)
						return false;
				}
				else if (n instanceof ArgumentNode )
				{
					AbstractArgument arg = ((ArgumentNode) n).getArgument();
					if(arg.checkPresence(queryString.getData()))
					{
						queryString.setData(arg.removeArgument(queryString.getData()));
						nbOk++;
					}
					else if(operator==Operator.AND)
						return false;
						
				}
				if((operator==Operator.XOR || operator==Operator.XOR_ONE) && nbOk>1)
				{
					return false;
				}

			}
			if((operator==Operator.OR_ONE_MIN || operator==Operator.XOR_ONE)&& nbOk==0)
			{
				return false;
			}


		if(master && queryString.getData().replace(" ", "").length()>0)
		{
			return false;
		}
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
