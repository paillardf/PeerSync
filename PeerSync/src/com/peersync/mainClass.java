package com.peersync;

import com.peersync.data.DataBaseManager;


import com.peersync.events.EventsManagerThread;
import com.peersync.models.Event;
import com.peersync.models.FileToSync;
import com.peersync.models.FileToSyncList;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class mainClass {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        Statement stat = conn.createStatement();
        stat.executeUpdate("drop table if exists people;");
        stat.executeUpdate("create table people (name, occupation);");
        PreparedStatement prep = conn.prepareStatement(
            "insert into people values (?, ?);");

        prep.setString(1, "Gandhi");
        prep.setString(2, "politics");
        prep.addBatch();
        prep.setString(1, "Turing");
        prep.setString(2, "computers");
        prep.addBatch();
        prep.setString(1, "Wittgenstein");
        prep.setString(2, "smartypants");
        prep.addBatch();

        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);

        ResultSet rs = stat.executeQuery("select * from people;");
        while (rs.next()) {
            System.out.println("name = " + rs.getString("name"));
            System.out.println("job = " + rs.getString("occupation"));
        }
        rs.close();
        conn.close();
    }
  }
//
//public class mainClass {
//	public static void main(String[] args){
//
//
//		EventsManagerThread.getEventsManagerThread().start();
//		
//		//DataBaseManager.getDataBaseManager().checkEventsIntegrity();
//		
////		FileToSyncList fsl = new FileToSyncList();
////		fsl.reload();
////		for (FileToSync fs : fsl.getFilesWithLocalSource())
////		{
////			System.out.println(fs.getRelFilePath()+"  "+fs.getLocalSource());
////		}
////		System.out.println("Down");
////		for (FileToSync fs : fsl.getFilesToDownload())
////		{
////			System.out.println(fs.getRelFilePath()+"  "+fs.getLocalSource());
////		}
//		
//
//		//DataBaseManager.getDataBaseManager().getStackVersionList("UUID2");
//
//		//System.out.println(DataBaseManager.getDataBaseManager().getSharedFolderOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\truc\\trez\\1"));
//
////		Event e = new Event("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png","newhash","oldhash",2,"Toto");
////		e.save();
//		
//		
////		Event ret = DataBaseManager.getDataBaseManager().getLastEventOfAFile("C:\\Users\\Nicolas.leleu\\Documents\\testTX2\\bele.png");
////		if (ret!=null)
////			System.out.println(ret.getDate());
//
//	}
//}
//
