package com.peersync.network.content.model;

import java.util.ArrayList;

public class BytesSegment{
	public BytesSegment(long offset, long length) {
		this.offset = offset;
		this.length = length;
	}
	public long offset;
	public long length;

	/**@return BitesSegment mergé si possible
	 * @return null sinon
	 *
	 */
	public BytesSegment tryToMerge(BytesSegment other)
	{

		long b1 = offset;
		long e1=b1+length;
		long b2=other.offset;
		long e2=b2+other.length;

		long bResult = -1;
		long eResult = -1;

		if(contains(other) )
		{
			bResult = b1<=b2?b1:b2;
			eResult= e1>=e2?e1:e2;
			return new BytesSegment(bResult,eResult);
		}
		return null;


	}

	//liste BytesSegment
	public ArrayList<BytesSegment> tryToSubstract(BytesSegment other)
	{

		long b1 = offset;
		long e1=b1+length;
		long b2=other.offset;
		long e2=b2+other.length;

		long bResult = -1;
		long eResult = -1;

		ArrayList<BytesSegment> returnValue = new ArrayList<BytesSegment>();

		if(contains(other))
		{
			if(b2<=b1 && e2>=e1) //recouvrement
			{
				returnValue.add(new BytesSegment(bResult,eResult));
				return returnValue;
			}
			else if( b1<=b2 && e2<=e1) // Chevauchement interne
			{

				bResult = b1;
				eResult= b2;
				if(bResult!=eResult)
				{
					eResult=eResult-bResult;
					returnValue.add(new BytesSegment(bResult,eResult));
				}

				bResult = e2;
				eResult= e1;
				if(bResult!=eResult)
				{
					eResult=eResult-bResult;
					returnValue.add(new BytesSegment(bResult,eResult));
				}
				return returnValue;
			}

			else if( b2<e1 && e2>=e1) // Chevauchement à droite
			{
				bResult = b1;
				eResult= b2;
				eResult=eResult-bResult;
				returnValue.add(new BytesSegment(bResult,eResult));
				return returnValue;
			}

			else if( b1<e2 && e1>=e2)// Chevauchement à gauche
			{
				bResult = e2;
				eResult= e1;
				eResult=eResult-bResult;
				returnValue.add(new BytesSegment(bResult,eResult));
				return returnValue;
			}




		}
		return null;


	}




	public boolean isEmpty()
	{
		return (offset==-1 && length==-1);
	}


	public boolean contains(BytesSegment other)
	{
		long b1 = offset;
		long e1=b1+length;
		long b2=other.offset;
		long e2=b2+other.length;


		return ((b1>=b2 && e2>=b1) || (b2>=b1 && e1>=b2) );

	}

	public boolean isSupTo(BytesSegment other)
	{
		long b1 = offset;
		long e1=b1+length;
		long b2=other.offset;
		long e2=b2+other.length;


		return (e1>b2);

	}





}
