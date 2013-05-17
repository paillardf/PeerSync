package com.peersync.cli;

import java.util.Hashtable;
import java.util.Map;

// Juste pour le polymorphisme
public abstract class Node {
	public enum Operator
	{
		AND,
		OR,
		XOR;



		public String getDisplayedOperator()
		{
			switch(this)
			{
			case AND :
				return "&";
			case OR :
				return "|";
			case XOR :
				return "X";
			default :
				return null;
			}
		}

	}





}
