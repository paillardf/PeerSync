package com.peersync.events;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbliteConnection {
	private Connection m_connection;
	private String m_dbPath;

	public DbliteConnection(String dbPath) throws ClassNotFoundException, SQLException {

		m_dbPath = new String();
		openConnection(dbPath);

	}

	public Connection getConnection() {
		return m_connection;
	}



	private void openConnection(String dbPath) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");
		if(m_dbPath!=dbPath)
		{

			closeConnection();
			// create a database connection
			m_connection = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
			m_dbPath = dbPath;
		}

	}

	public void finalize()
	{
		closeConnection();

	}

	private void closeConnection()
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
