package com.peersync.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.peersync.events.DbliteConnection;

public class ShareFolder extends DbliteConnection{

	private final String UID;
	private final String peerGroupUID;
	private final String asbolutePath;

	private static final String DBEVENTSPATH = "./dblite.db";

	private static final String DBSHAREDFOLDERSTABLE = "sharedFolder";
	private static final String UUIDFIELD = "uuid";
	private static final String ROOTPATHFIELD = "rootAbsolutePath";


	public ShareFolder(String UID, String peerGroupUID) throws ClassNotFoundException, SQLException {
		super(DBEVENTSPATH);
		this.UID = UID;
		this.peerGroupUID = peerGroupUID;
		asbolutePath = getSharedFolderRootPath();
	}

	public ArrayList<StackVersion> getStackVersionList(){
		return null;//TODO 
	}

	public String getUID() {
		return UID;
	}

	public String getPeerGroupUID() {
		return peerGroupUID;
	}

	public static String AbsoluteFromRelativePath(String relative,String baseDir)
	{
		return baseDir.concat(relative);
	}

	public static String RelativeFromAbsolutePath(String absolute,String baseDir)
	{
		return absolute.replace(baseDir, "");
	}

	public String getAbsFolderRootPath()
	{

		return asbolutePath;
	}

	private String getSharedFolderRootPath()
	{
		String res = new String();
		try
		{
			Statement statement = getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select sf."+ROOTPATHFIELD+
					" from "+DBSHAREDFOLDERSTABLE+" sf  where sf."+UUIDFIELD+" ='"+UID+"'");

			while(rs.next())
			{
				// read the result set
				res = rs.getString(ROOTPATHFIELD);
				//		        System.out.println("name = " + rs.getString(FILEPATHFIELD));
				//		        System.out.println("id = " + rs.getString(HASHFIELD));
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		return res;

	}


}
