package com.peersync;

import java.util.*; 
import java.util.Map.Entry;
import java.security.DigestInputStream;  
import java.security.MessageDigest;
import java.io.File;  
import java.io.FileInputStream;  
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.peersync.events.EventsManager;

public class mainClass {
	public static void main(String[] args){
		EventsManager em = new EventsManager();
		em.start();
//		try {
//			EventsPile ep = new EventsPile();
//			Map<String,String> SHAFiles = new Hashtable<String,String>();
//			SHAFiles = ep.toMap();
//			List<String> directories = new LinkedList<String>();
//			directories.add("C:\\Users\\Nicolas.leleu\\Documents\\testTX");
//			DirectoryReader d = new DirectoryReader();
//			 d.scanDifferences(SHAFiles,directories);
//			 ep.createEventsFromScan(d.getNewFilesMap(),d.getUpdatedFilesMap(),d.getDeletedFilesSet());
////			for(File file : files)
////				{
////				System.out.println("File : "+file.getAbsolutePath());
////				}
////			for(Entry<String, String> entry : SHAFiles.entrySet()) {
////				System.out.println("name = " + entry.getKey()+"   hash : "+entry.getValue());
////			}
//
//			
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		try {
//			Class.forName("org.sqlite.JDBC");
//		} catch (ClassNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		Connection connection = null;
//	    try
//	    {
//	      // create a database connection
//	      connection = DriverManager.getConnection("jdbc:sqlite:/home/nicolas/Documents/test");
//	      Statement statement = connection.createStatement();
//	      statement.setQueryTimeout(30);  // set timeout to 30 sec.
//
//
//	      ResultSet rs = statement.executeQuery("select * from main");
//	      while(rs.next())
//	      {
//	        // read the result set
//	        System.out.println("name = " + rs.getString("first"));
//	      }
//	    }
//	    catch(SQLException e)
//	    {
//	      // if the error message is "out of memory", 
//	      // it probably means no database file is found
//	      System.err.println(e.getMessage());
//	    }
//	    finally
//	    {
//	      try
//	      {
//	        if(connection != null)
//	          connection.close();
//	      }
//	      catch(SQLException e)
//	      {
//	        // connection close failed.
//	        System.err.println(e);
//	      }
//	    }
//		Map<String,String> SHAFiles = new Hashtable<String,String>();
//
//		
//
//		for(File file : files)
//		{
//			if(file.isFile())
//			{
//				try
//				{
//
//					MessageDigest md = MessageDigest.getInstance("SHA-1"); 
//					DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);  
//					dis.on(true);  
//
//					while (dis.read() != -1){  
//						;  
//					}  
//					byte[] b = md.digest();  
//					dis.close();
//					String sb = new String();
//					for (int j=0;j<b.length;++j) {  
//
//						sb+= String.format("%x", b[j]) ;  
//					}   
//					if(SHAFiles.containsKey(sb))
//					{
//						System.out.println("file "+file.getAbsolutePath()+" and "+SHAFiles.get(sb)+" are equals");
//
//					}
//					else
//					{
//						System.out.println("File : "+file.getAbsolutePath()+" SHA : "+sb);
//						SHAFiles.put(sb,file.getAbsolutePath());
//					}
//				}
//				catch (Exception ex) {  
//					ex.printStackTrace();  
//				}   
//			}

//		}
	}
}
