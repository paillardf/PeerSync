package com.peersync.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class DbliteConnection {
	private static final String DATABASE_INFO = "DATABASE_INFO";
	private static final String INFO_FIELD = "INFO_FIELD";
	private static final String INFO_VALUE = "INFO_VALUE";
	
	
	private static final String FIELD_VERSION = "VERSION";
	private Connection m_connection;

	protected abstract void onCreate() throws SQLException;
	protected abstract void onUpdate() throws SQLException;
	protected abstract void onDelete() throws SQLException;

	protected DbliteConnection(String path, int version) 
	{
		try {
			openConnection(path,version);
		} catch (ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
			System.exit(-1);
		}


	}




	protected void openConnection(String dbPath, int version) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");

		// create a database connection

		File f = new File(dbPath);
		boolean exist = f.exists();
		if(!exist)
			new File(f.getParent()).mkdirs();
		
//		Properties properties = new Properties();
//		properties.setProperty("PRAGMA foreign_keys", "ON");
		m_connection = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
		update("PRAGMA foreign_keys = ON;");
		if(!exist){
			
			createBDD(version);
		}else{ //VERIFICATION DATABASE UPDATE
			
			ResultSet rs = query("Select "+INFO_VALUE+" from "+DATABASE_INFO+" where "+INFO_FIELD+"='"+FIELD_VERSION+"'");
			rs.next();
			if(Integer.parseInt(rs.getString(INFO_VALUE))<version)
				updateBDD(version);
		}



	}

	private void updateBDD(int version) throws SQLException {
		onUpdate();
		update("Update "+DATABASE_INFO+" set "+INFO_VALUE+"='"+version+"' where "+DATABASE_INFO+"='"+FIELD_VERSION+"'");
		
	}
	private void createBDD(int version) throws SQLException {
		update("create table "+DATABASE_INFO+"("+INFO_FIELD+" text, "+INFO_VALUE+" text, PRIMARY KEY ("+INFO_FIELD+"))");
		update("insert into "+DATABASE_INFO+" values ('"+FIELD_VERSION+"', '"+version+"')");
		onCreate();
		
	}
	public void finalize()
	{
		closeConnection();

	}

	protected ResultSet query(String query) throws SQLException{
		Statement statement = m_connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec
		return  statement.executeQuery(query);
	}
	
	protected int update(String update) throws SQLException{
		Statement statementUpdate = m_connection.createStatement();
		statementUpdate.setQueryTimeout(30); 
		return  statementUpdate.executeUpdate(update);
	}

	protected void closeConnection()
	{
		try
		{
			if(m_connection != null)
				m_connection.close();
		}
		catch(SQLException e)
		{
			// connection close failed.
			System.err.println(e);
		}

	}



}
