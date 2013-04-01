package com.peersync.events;


import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.peersync.models.ShareFolder;

public class DirectoryReader {

	private Map<String,String> m_oldMap;
	private Set<String> m_filesOk = new HashSet<String>();
	private Set<String> m_directoriesOk = new HashSet<String>();
	private Map<String,String> m_newFiles = new Hashtable<String,String>();
	private Map<String,String> m_updatedFiles = new Hashtable<String,String>();
	private Set<String> m_deletedFiles = new HashSet<String>();


	public DirectoryReader() {

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


	public void scanDifferences(Map<String,String> om,ShareFolder directory)
	{
		m_oldMap = om;
		m_filesOk.clear();
		m_newFiles.clear();
		m_updatedFiles.clear();
		m_deletedFiles.clear();
		m_directoriesOk.clear();
		listAllFiles(directory);
		searchDeletedFiles();


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
					if(entry.getKey().contains(it.next()))
					{
						found = true;
						m_filesOk.add(entry.getKey());
					}
				}
				if(!found)
				{
					m_deletedFiles.add(entry.getKey());
					System.out.println("A SUPPR : name = " + entry.getKey()+"   hash : "+entry.getValue());
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

	private List<File> listAllFiles(ShareFolder dir)
	{
		List<File> files = new LinkedList<File>();


		File dirFile = new File(dir.getAbsFolderRootPath());
		files.addAll(listAllFilesInADir(dirFile.getAbsolutePath()));


		return files;

	}

	private List<File> listAllFilesInADir(String dirString)
	{
		List<File> files = new LinkedList<File>();


		File dir = new File(dirString);

		if(dir.exists())
		{

			File[] content = dir.listFiles();
			if(content != null)
			{
				for (File file : content)
				{
					String hash = DirectoryReader.calculateHash(file);
					if(m_oldMap.containsKey(file.getAbsolutePath()) && m_oldMap.get(file.getAbsolutePath()).equals(hash))
					{
						if(file.isDirectory())
							m_directoriesOk.add(file.getAbsolutePath());
						else
							m_filesOk.add(file.getAbsolutePath());
					}
					else
					{
						if(!m_oldMap.containsKey(file.getAbsolutePath()))
						{
							m_newFiles.put(file.getAbsolutePath(),hash);
						}
						else
						{
							m_updatedFiles.put(file.getAbsolutePath(),hash);
						}
						if(file.isDirectory())
						{
							files.add(file);
							files.addAll(listAllFilesInADir(file.getAbsolutePath()));
						}
						else 
						{
							files.add(file);
						}
					}
				}
			}
		}
		return files;
	}


} 
