package com.peersync.cli;


// Juste pour le polymorphisme
public abstract class Node {
	public enum Operator
	{
		// Peut être plutot juste AND, OR et XOR avec des options (nbmin, nbmax?)
		AND,
		OR, //0..*
		XOR,//0..1
		OR_ONE_MIN,//1..*
		XOR_ONE;//1



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
			case OR_ONE_MIN :
				return "|";
			case XOR_ONE :
				return "X";
			default :
				return null;
			}
		}

	}





}
