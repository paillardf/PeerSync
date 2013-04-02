package com.peersync.models;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;




public class EventsStack  {

	private Vector<Event> m_Events = new Vector<Event>();





	private static final int ACTION_CREATE = 1;
	private static final int ACTION_UPDATE = 2;
	private static final int ACTION_DELETE = 3;

	public EventsStack() 
	{




	}





	public void createEventsFromScan(String UUID,Map<String,String> createdFiles,Map<String,String> updatedFiles,Set<String> deletedFiles)
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
