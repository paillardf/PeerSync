package com.peersync.network.content.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.jxta.id.ID;



public class SmartFileInfo {



	private HashMap<Long,Integer> indexDirectory = new HashMap<Long,Integer >();
	private HashMap<Integer,Long> beginsDirectory = new HashMap<Integer,Long >(); // \o/
	private ArrayList< HashSet<ID> > providers = new ArrayList<HashSet<ID>>();

	private String hash;

	public SmartFileInfo(String hash)
	{
		this.hash=hash;
	}

	public SmartFileInfo(SmartFileInfo smartFileInfo) {
		hash = smartFileInfo.hash;
		providers=(ArrayList<HashSet<ID>>)smartFileInfo.getProviders().clone();
		indexDirectory=(HashMap<Long,Integer>)smartFileInfo.getIndexDirectory().clone();
		beginsDirectory=(HashMap<Integer,Long>)smartFileInfo.getBeginsDirectory().clone();
	}


	public SegmentToDownload getBestChoice(FileAvailability fa,ID pipeID)
	{
		SmartFileInfo masked = mask(fa);
		return masked.getBestChoice(pipeID);
	}
	
	public SegmentToDownload getBestChoice(FileAvailability fa)
	{
		SmartFileInfo masked = mask(fa);
		return masked.getBestChoice();
	}

	public SegmentToDownload getBestChoice(ID pipeID)
	{
		ArrayList<Integer> resIntermediaire = new ArrayList<Integer>();
		ArrayList<Integer> resFinal = new ArrayList<Integer>();
		SegmentToDownload sd = null;
		int min = getMinProviders(pipeID);
		if(min>0)
		{
			for(int i=0;i<providers.size();i++)
			{

				if(providers.get(i).size()==min && i+1<providers.size())
					resIntermediaire.add(i);
			}
			long maxSize = 0;
			for(int secondPass: resIntermediaire)
			{
				long begin = beginsDirectory.get(secondPass);
				long length = beginsDirectory.get(secondPass+1)-begin;
				if(length>maxSize)
					maxSize=length;


			}

			for(int thirdPass: resIntermediaire)
			{
				long begin = beginsDirectory.get(thirdPass);
				long length = beginsDirectory.get(thirdPass+1)-begin;
				if(length==maxSize)
					resFinal.add(thirdPass);


			}
			if(resFinal.size()>0){
				Random rnd = new Random();
				int choice = rnd.nextInt(resFinal.size());
				choice = resFinal.get(choice);
				long begin = beginsDirectory.get(choice);
				long length = beginsDirectory.get(choice+1)-begin;
				BytesSegment bs = new BytesSegment(begin, length);
				sd = new SegmentToDownload(hash,bs,providers.get(choice));
			}
			
		}

		return sd;

	}
	
	
	public SegmentToDownload getBestChoice()
	{
		ArrayList<Integer> resIntermediaire = new ArrayList<Integer>();
		ArrayList<Integer> resFinal = new ArrayList<Integer>();
		SegmentToDownload sd = null;
		int min = getMinProviders();
		if(min>0)
		{
			for(int i=0;i<providers.size();i++)
			{

				if(providers.get(i).size()==min && i+1<providers.size())
					resIntermediaire.add(i);
			}
			long maxSize = 0;
			for(int secondPass: resIntermediaire)
			{
				long begin = beginsDirectory.get(secondPass);
				long length = beginsDirectory.get(secondPass+1)-begin;
				if(length>maxSize)
					maxSize=length;


			}

			for(int thirdPass: resIntermediaire)
			{
				long begin = beginsDirectory.get(thirdPass);
				long length = beginsDirectory.get(thirdPass+1)-begin;
				if(length==maxSize)
					resFinal.add(thirdPass);


			}


			int choice = (int)(Math.random() * (resFinal.size()));
			choice = resFinal.get(choice);
			long begin = beginsDirectory.get(choice);
			long length = beginsDirectory.get(choice+1)-begin;
			BytesSegment bs = new BytesSegment(begin, length);
			sd = new SegmentToDownload(hash,bs,providers.get(choice));
		}

		return sd;

	}




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
	
	
	public void display(FileAvailability fa)
	{
		SmartFileInfo masked = mask(fa);
		masked.display();
	}


	public void addFileAvailability(FileAvailability fa,ID id)
	{
		if(fa.getHash().equals(hash))
		{
			for(BytesSegment bs : fa.getSegments())
			{
				long beginSegment = bs.offset;
				long endSegment = beginSegment+bs.length;

				long previousBegin = getPreviousBegin(beginSegment);
				long previousEnd = getPreviousBegin(endSegment);
				HashSet<ID> tmp = new HashSet<ID>();
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

		
				}

			}
		}

	}

	// Pour le clonage
	private HashMap<Integer,Long> getBeginsDirectory() {
		return beginsDirectory;
	}

	private HashMap<Long,Integer> getIndexDirectory() {
		return indexDirectory;
	}

	private ArrayList< HashSet<ID> > getProviders() {
		return providers;
	}

	private void addAProvider(int index,ID id)
	{
		HashSet<ID> toAdd = providers.get(index);
		if(id!=null)
		{
			toAdd.add(id);
			providers.set(index,toAdd );
		}
	}

	private void addProviders(long previousBegin,long begin,HashSet<ID> ids)
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
		HashSet<ID> toAdd = new HashSet<ID>();
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
		HashMap<Integer, Long> tmp = new HashMap<Integer, Long>();
		if(!indexDirectory.containsKey(begin))
		{
			for(Entry<Long,Integer> entry : indexDirectory.entrySet()) {
				if(entry.getValue()>=index)
				{
					beginsDirectory.remove(entry.getValue());
					int newIndex = entry.getValue()+1;
					entry.setValue(newIndex);
					tmp.put(newIndex, entry.getKey());
				}


			}


			indexDirectory.put(begin,index);
			beginsDirectory.put(index, begin);
		}
		beginsDirectory.putAll(tmp);
		


	}

	private SmartFileInfo mask(FileAvailability fa)
	{
		SmartFileInfo result = new SmartFileInfo(this);
		if(fa.getHash().equals(hash))
		{
			//Pour tous les segments de fa, on met supprime tous les intervalles disponibles de smarties
			for(BytesSegment bs : fa.getSegments())
			{
				long beginSegment = bs.offset;
				long endSegment = beginSegment+bs.length;
				HashMap<Long, Integer> resultIndexDirectory = result.getIndexDirectory();

				long previousBegin = result.getPreviousBegin(beginSegment);
				long previousEnd = result.getPreviousBegin(endSegment);
				HashSet<ID> tmp = new HashSet<ID>();
				if(previousEnd!=-1)
					tmp= result.cloneIDs(resultIndexDirectory.get(previousEnd));
				result.addProviders(previousBegin,beginSegment,new HashSet<ID>());
				ArrayList<HashSet<ID>> t = result.getProviders();
				for(int i=resultIndexDirectory.get(beginSegment)+1;i<result.getProviders().size() && i<=resultIndexDirectory.get(previousEnd);i++)
				{
					result.eraseIndex(i);
				}
				//Traitement du dernier
				previousEnd = result.getPreviousBegin(endSegment);
				result.addProviders(previousEnd,endSegment,tmp);
			}
		}
		return result;
	}


	private void eraseIndex(int index)
	{
		long value = beginsDirectory.get(index);
		beginsDirectory.remove(index);
		indexDirectory.remove(value);
		
		providers.remove(index);
		for(Entry<Long,Integer> entry : indexDirectory.entrySet()) {
			
			if(entry.getValue()>index)
			{
				beginsDirectory.remove(entry.getValue());
				int newIndex = entry.getValue()-1;
				entry.setValue(newIndex);
				beginsDirectory.put(newIndex, entry.getKey());
			}
		}
		
	}

	private HashSet<ID> cloneIDs(int index)
	{
		HashSet<ID> returnValue = (HashSet<ID>)providers.get(index).clone();
		return returnValue;
	}

	private long getPreviousBegin(long begin)
	{
		long result=Long.MIN_VALUE;
		if(indexDirectory.containsKey(begin))
			return begin;
		else
		{
			for(Entry<Long,Integer> entry : indexDirectory.entrySet()) {
				if(entry.getKey()<begin && entry.getKey()>result)
					result=entry.getKey();
				else
					return result==Long.MIN_VALUE?-1:result;

			}
			return result==Long.MIN_VALUE?-1:result;
		}

	}



	private int getMinProviders(ID pipeID)
	{
		int result = Integer.MAX_VALUE;
		for(Set<ID> csid : providers)
		{
			if(csid.size()<result && csid.size()>0 && csid.contains(pipeID))
				result= csid.size();
		}
		return result==Integer.MAX_VALUE?-1:result;

	}
	

	private int getMinProviders()
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
