package com.peersync.network.content.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.jxta.id.ID;

import com.peersync.network.content.model.BytesSegment;
import com.peersync.network.content.model.FileAvailability;


public class Smarties {



	private Map<Long,Integer> indexDirectory = new HashMap<Long,Integer >();
	private Map<Integer,Long> beginsDirectory = new HashMap<Integer,Long >(); // \o/
	private ArrayList< Set<ID> > providers = new ArrayList<Set<ID>>();
	
	private String hash;

	public void display()
	{
		System.out.println("Display Smarties");
		for(int i=0;i<providers.size();i++)
		{
			System.out.println("Providers du segment débutant à "+beginsDirectory.get(i));
			for(ID id : providers.get(i))
			{
				System.out.println(id);
			}
		}
	}


	public void addFileAvailability(FileAvailability fa,ID id)
	{
		for(BytesSegment bs : fa.getSegments())
		{
			long beginSegment = bs.offset;
			long endSegment = beginSegment+bs.length;

			long previousBegin = getPreviousBegin(beginSegment);
			long previousEnd = getPreviousBegin(endSegment);
			Set<ID> tmp = new HashSet<ID>();
			if(previousEnd!=-1)
				tmp= cloneIDs(indexDirectory.get(previousEnd));

			addAProvider(previousBegin,beginSegment,id);



			int firstIndex = indexDirectory.get(beginSegment);
			int i=firstIndex+1;
			//Iteration sur index compris entre begin et end
			for( ;i<providers.size();i++)
			{
				if(beginsDirectory.get(i)>endSegment)
					break;
				else
					addAProvider( i,id);
			}
			//Fin : test si le end est bien pris en compte. Sinon, on ajoute

			if(beginsDirectory.get(i-1)<endSegment)
			{
				long test = getPreviousBegin(endSegment);


				addProviders(test,endSegment,tmp);

				//addAProvider(test,endSegment,null);

				System.out.println("END :"+endSegment+"   "+beginsDirectory.get(i-1));
			}





		}

	}

	private void addAProvider(int index,ID id)
	{
		Set<ID> toAdd = providers.get(index);
		if(id!=null)
		{
			toAdd.add(id);
			providers.set(index,toAdd );
		}
	}

	private void addProviders(long previousBegin,long begin,Set<ID> ids)
	{

		int index;

		if(previousBegin!=begin)
		{
			if(previousBegin==-1)
				index=0;
			else
				index = indexDirectory.get(previousBegin)+1;
			providers.add(index,ids);
			updateTheDirectory(begin,index);
		}
		else
			providers.set(indexDirectory.get(previousBegin), ids);



	}

	private void addAProvider(long previousBegin,long begin,ID id)
	{
		Set<ID> toAdd = new HashSet<ID>();
		int index;
		if(previousBegin!=-1)
		{
			toAdd = cloneIDs(indexDirectory.get(previousBegin));
		}
		if(id!=null)
		{
			toAdd.add(id);
			if(previousBegin!=begin)
			{
				if(previousBegin==-1)
					index=0;
				else
					index = indexDirectory.get(previousBegin)+1;
				providers.add(index,toAdd);
				updateTheDirectory(begin,index);
			}
			else
				providers.set(indexDirectory.get(previousBegin), toAdd);
		}



	}

	private void updateTheDirectory(long begin,int index)
	{
		if(!indexDirectory.containsKey(begin))
		{
			for(Entry<Long,Integer> entry : indexDirectory.entrySet()) {
				if(entry.getValue()>=index)
				{
					beginsDirectory.remove(entry.getValue());
					int newIndex = entry.getValue()+1;
					entry.setValue(newIndex);
					beginsDirectory.put(newIndex, entry.getKey());
				}


			}


			indexDirectory.put(begin,index);
			beginsDirectory.put(index, begin);
		}


	}

	public Set<ID> cloneIDs(int index)
	{
		Set<ID> returnValue = new HashSet<ID>();
		returnValue.addAll(providers.get(index));
		return returnValue;
	}

	public long getPreviousBegin(long begin)
	{
		Set<Long> results=new HashSet<Long>();
		if(indexDirectory.containsKey(begin))
			return begin;
		else
		{
			for(Entry<Long,Integer> entry : indexDirectory.entrySet()) {
				if(entry.getKey()<begin)
					results.add(entry.getKey());

			}
			if(results.size()==0)
				return -1;
			else
				return Collections.max(results);
		}

	}

	public SegmentToDownload getBestChoice()
	{
		ArrayList<Integer> resIntermediaire = new ArrayList<Integer>();
		SegmentToDownload sd = null;
		int min = getMinProviders();
		if(min>0)
		{
			for(int i=0;i<providers.size();i++)
			{

				if(providers.get(i).size()==min && i+1<providers.size())
					resIntermediaire.add(i);
			}
			int choice = (int)(Math.random() * (resIntermediaire.size()));
			BytesSegment bs = new BytesSegment(beginsDirectory.get(choice), beginsDirectory.get(choice+1));
			sd = new SegmentToDownload(hash,bs,providers.get(resIntermediaire.get(choice)));
		}

		return sd;

	}

	public int getMinProviders()
	{
		int result = Integer.MAX_VALUE;
		for(Set<ID> csid : providers)
		{
			if(csid.size()<result && csid.size()>0)
				result= csid.size();
		}
		return result==Integer.MAX_VALUE?-1:result;

	}










}
