package com.peersync.models;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;




public class EventsStack  {

	private ArrayList<Event> m_Events = new ArrayList<Event>();




	
	
	
	
	

	public EventsStack() 
	{




	}
	
	public ArrayList<Event> getEvents()
	{
		return m_Events;
	}

	
	public void clear()
	{
		m_Events.clear();
	}




	/*public void createEventsFromScan(String UUID,Map<String,String> createdFiles,Map<String,String> updatedFiles,Set<String> deletedFiles)
	{
		m_Events.clear();
		for (String t : deletedFiles)
		{
			try
			{
				Event e = new Event(t,null,ACTION_DELETE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		for(Entry<String, String> entry : createdFiles.entrySet()) {
			try
			{
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_CREATE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		for(Entry<String, String> entry : updatedFiles.entrySet()) {
			try
			{
				Event e = new Event(entry.getKey(),entry.getValue(),ACTION_UPDATE,"Nicolas",UUID);
				addEvent(e);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		save();
	}*/
	
	
	public void addAll(EventsStack es)
	{
		m_Events.addAll(es.getEvents());
	}
	
	public void setAllEventsToSync()
	{
		for (Event e : m_Events)
		{
			e.setStatus(Event.STATUS_UNSYNC);

		}
	}
	
	public void addEvent(Event e)
	{
		m_Events.add(e);
	}

	


	public void save()
	{
		save(System.currentTimeMillis());

	}

	public void save(long d) {


		for (Event e : m_Events)
		{
			//if(e.getDate().after(d))
			e.save();

		}


	}




}
