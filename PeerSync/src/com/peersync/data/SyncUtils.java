package com.peersync.data;

import java.util.ArrayList;

import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;

public class SyncUtils {

	private static SyncUtils instance;
	
	public static SyncUtils getInstance(){
		if(instance==null)
			instance = new SyncUtils();
		return instance;
	}
	
	private SyncUtils(){
		
	}
	
	public ArrayList<SharedFolderVersion> compareShareFolderVersion(ArrayList<SharedFolderVersion> sharefolderVersionList){
		DataBaseManager db = DataBaseManager.getInstance();
		
		ArrayList<SharedFolderVersion> shareFolderVList = new ArrayList<SharedFolderVersion>();
		
		for (SharedFolderVersion sharedFolderVersion : sharefolderVersionList) {
			SharedFolderVersion myShareFolderVersion = db.getSharedFolderVersion(sharedFolderVersion.getUID());
			
			SharedFolderVersion sfv = getNeededStackVersion(sharedFolderVersion, myShareFolderVersion);
			
			if(sfv.size()>0)
				shareFolderVList.add(sfv);

		}
		
		return shareFolderVList;
	}

	private SharedFolderVersion getNeededStackVersion(SharedFolderVersion advSFV,SharedFolderVersion ownedSFV){
		
		SharedFolderVersion sfv  = new SharedFolderVersion(advSFV.getUID());
		
		for (StackVersion stackV : advSFV.getStackVersionList()) {
			
			StackVersion ownedStackV = ownedSFV.getStackVersion(stackV.getUID());
			if(ownedStackV.getLastUpdate()<stackV.getLastUpdate())
				sfv.addStackVersion(ownedStackV);
		}
		
		return sfv;
		
		
	}

	
}
