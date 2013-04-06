package com.peersync.events;


import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.peersync.data.DataBaseManager;
import com.peersync.models.Event;
import com.peersync.models.EventsStack;
import com.peersync.models.SharedFolder;

public class DirectoryReader {

	private Map<String,String> m_oldMap;
	private Set<String> m_filesOk = new HashSet<String>();
	private Set<String> m_directoriesOk = new HashSet<String>();
	private Map<String,String> m_newFiles = new Hashtable<String,String>();
	private Map<String,String> m_updatedFiles = new Hashtable<String,String>();
	private Set<String> m_deletedFiles = new HashSet<String>();
	private EventsStack m_EventsStack = new EventsStack();
	private SharedFolder currentShareFolder;
	private ArrayList<SharedFolder> shareFolders;


	private static DirectoryReader instance;

	public static DirectoryReader getDirectoryReader()
	{
		if(instance==null)
			instance = new DirectoryReader();
		
		return instance;

	}
	private DirectoryReader() {
		loadDirectoriesToScan();
	}
	
	public void loadDirectoriesToScan()
	{
		shareFolders = DataBaseManager.getDataBaseManager().getAllSharedDirectories();
	}

	public Map<String,String> getNewFilesMap()
	{
		return m_newFiles;
	}

	public Map<String,String> getUpdatedFilesMap()
	{
		return m_updatedFiles;
	}

	public Set<String> getDeletedFilesSet()
	{
		return m_deletedFiles;
	}

	/**
	 *  Calcule le Hash Sha-1 du ficher passé en paramètre.
	 * 	Pour un dossier, ce n'est pas le hash qui est retourné mais sa date de modification.
	 * 	@param f : fichier à hasher
	 * 	@return hash (ou date de modification pour un dossier) 
	 */
	public static String calculateHash(File f)
	{
		String res = new String();
		if(f.isFile())
		{
			try
			{
				MessageDigest md = MessageDigest.getInstance("SHA-1"); 
				DigestInputStream dis = new DigestInputStream(new FileInputStream(f), md);  
				dis.on(true);  

				while (dis.read() != -1){  
					;  
				}  
				byte[] b = md.digest();  
				dis.close();

				for (int j=0;j<b.length;++j) {  

					res+= String.format("%x", b[j]) ;  
				} 
			}catch (Exception ex) {  
				ex.printStackTrace();  
			}
		}
		else if(f.isDirectory())
		{
			res = String.valueOf(f.lastModified());

		}

		return res;
	}


	public void scan()
	{
		for (SharedFolder shareFolder : shareFolders) {
			Map<String,String> currentStack = DataBaseManager.getDataBaseManager().getLastEvents(shareFolder.getUID());
			scanDifferences(currentStack,shareFolder);
			getEventsStack().save();
		}
	}
	
	private void scanDifferences(Map<String,String> om,SharedFolder shareFolder)
	{
		m_oldMap = om;
		m_filesOk.clear();
		m_newFiles.clear();
		m_updatedFiles.clear();
		m_deletedFiles.clear();
		m_directoriesOk.clear();
		m_EventsStack.clear();
		currentShareFolder = shareFolder;
		listAllFiles();
		searchDeletedFiles();
		currentShareFolder = null;


	}

	private void searchDeletedFiles()
	{
		for(Entry<String, String> entry : m_oldMap.entrySet()) {
			if(!m_filesOk.contains(entry.getKey()) && !m_updatedFiles.containsKey(entry.getKey()) && !m_newFiles.containsKey(entry.getKey()) )
			{
				boolean found = false;
				Iterator<String> it = m_directoriesOk.iterator();
				while(!found && it.hasNext())
				{
					String tmp = it.next();
					if(entry.getKey().contains(tmp))
					{
						found = true;
						m_filesOk.add(entry.getKey());
					}
				}
				if(!found)
				{
					m_deletedFiles.add(entry.getKey());
					File f = new File(entry.getKey());
					System.out.println("A SUPPR : name = " + entry.getKey()+"   hash : "+entry.getValue());
					String relFilePath = SharedFolder.RelativeFromAbsolutePath(entry.getKey(), DataBaseManager.getDataBaseManager().getSharedFolderRootPath(currentShareFolder.getUID()));
					m_EventsStack.addEvent(new Event(currentShareFolder.getUID(),relFilePath ,f.isFile()? 1 : 0,null,entry.getValue(),Event.ACTION_DELETE,"Nicolas",Event.STATUS_OK));
				}
			}
		}
		for (String t : m_filesOk)
		{
			System.out.println("FileOk : name = " + t);
		}
		for(Entry<String, String> entry : m_newFiles.entrySet()) {
			System.out.println("FileACREER: name = " + entry.getKey());
		}
		for(Entry<String, String> entry : m_updatedFiles.entrySet()) {
			System.out.println("FileAMAJ: name = " + entry.getKey());
		}




	}


	private List<File> listAllFiles()
	{
		List<File> files = new LinkedList<File>();
		files.addAll(listAllFilesInADir(currentShareFolder.getAbsFolderRootPath()));
		
		return files;

	}

	private List<File> listAllFilesInADir(String dirString)
	{




		List<File> files = new LinkedList<File>();


		File dir = new File(dirString);

		// Pour l'instant, on retire l'optimisation basée sur la date de modif d'un dossier
		// Quand on modifie un fichier dans un dossier, ça modifie bien la date du dossier parent, mais pas les dates des dossiers parents du parent
		// --> Optimisation impossible
		if(dir.exists())
		{
			String hashDir = DirectoryReader.calculateHash(dir);
			String relFilePath = SharedFolder.RelativeFromAbsolutePath(dir.getAbsolutePath(), DataBaseManager.getDataBaseManager().getSharedFolderRootPath(currentShareFolder.getUID()));
			//boolean toScan = false;
			if(m_oldMap.containsKey(dir.getAbsolutePath()) && !m_oldMap.get(dir.getAbsolutePath()).equals(hashDir))
			{
				m_updatedFiles.put(dir.getAbsolutePath(),hashDir);
				m_EventsStack.addEvent(new Event(currentShareFolder.getUID(), relFilePath,dir.isFile()? 1 : 0,hashDir,null,Event.ACTION_UPDATE,"Nicolas",Event.STATUS_OK));
				//toScan = true;
			}
			else if(!m_oldMap.containsKey(dir.getAbsolutePath()))
			{
				m_newFiles.put(dir.getAbsolutePath(),hashDir);
				m_EventsStack.addEvent(new Event(currentShareFolder.getUID(), relFilePath,dir.isFile()? 1 : 0,hashDir,null,Event.ACTION_CREATE,"Nicolas",Event.STATUS_OK));
				//toScan = true;
			}
			else
				m_filesOk.add(dir.getAbsolutePath());
			//if(toScan)
			//{
				File[] content = dir.listFiles();
				if(content != null)
				{
					for (File file : content)
					{
						String relFilePathFile = SharedFolder.RelativeFromAbsolutePath(file.getAbsolutePath(), DataBaseManager.getDataBaseManager().getSharedFolderRootPath(currentShareFolder.getUID()));
						String hash = DirectoryReader.calculateHash(file);
						if(!file.isDirectory() && m_oldMap.containsKey(file.getAbsolutePath()) && m_oldMap.get(file.getAbsolutePath()).equals(hash))
						{
//							if(file.isDirectory())
//								m_directoriesOk.add(file.getAbsolutePath());
//							else
								m_filesOk.add(file.getAbsolutePath());
						}
						else
						{
							if(file.isDirectory())
							{
								boolean isShareFolder = false;
								for (SharedFolder sf : shareFolders) {
									if(file.getAbsolutePath().compareTo(sf.getAbsFolderRootPath())==0)
										isShareFolder = true;
								}

								if(!isShareFolder){
									files.add(file);
									files.addAll(listAllFilesInADir(file.getAbsolutePath()));
								}
							}
							else 
							{
								files.add(file);

								if(!m_oldMap.containsKey(file.getAbsolutePath()))
								{
									m_newFiles.put(file.getAbsolutePath(),hash);
									m_EventsStack.addEvent(new Event(currentShareFolder.getUID(), relFilePathFile,file.isFile()? 1 : 0,hash,null,Event.ACTION_CREATE,"Nicolas",Event.STATUS_OK));
								}
								else
								{
									m_updatedFiles.put(file.getAbsolutePath(),hash);
									m_EventsStack.addEvent(new Event(currentShareFolder.getUID(), relFilePathFile,file.isFile()? 1 : 0,hash,null,Event.ACTION_UPDATE,"Nicolas",Event.STATUS_OK));
								}
							}

						}
					}
				}
//			}
//			else
//			{
//				System.out.println("On skip "+dir.getAbsolutePath());
//				m_filesOk.add(dir.getAbsolutePath());
//				m_directoriesOk.add(dir.getAbsolutePath());
//			}

		}
		return files;
	}
	public EventsStack getEventsStack() {
		return m_EventsStack;
	}



} 
