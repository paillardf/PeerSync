package com.peersync.data;

import java.io.File;
import java.util.ArrayList;

import com.peersync.models.FileWithLocalSource;
import com.peersync.models.SharedFolderVersion;
import com.peersync.models.StackVersion;
import com.peersync.tools.FileUtils;

public class SyncUtils {


	public static ArrayList<SharedFolderVersion> compareShareFolderVersion(ArrayList<SharedFolderVersion> sharefolderVersionList){
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

	private static SharedFolderVersion getNeededStackVersion(SharedFolderVersion advSFV,SharedFolderVersion ownedSFV){
		
		SharedFolderVersion sfv  = new SharedFolderVersion(advSFV.getUID());
		
		for (StackVersion stackV : advSFV.getStackVersionList()) {
			
			StackVersion ownedStackV = ownedSFV.getStackVersion(stackV.getUID());
			if(ownedStackV.getLastUpdate()<stackV.getLastUpdate())
				sfv.addStackVersion(ownedStackV);
		}
		
		return sfv;
		
		
	}
	
	public static void startIntelligentSync(final String peerGroupID){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				DataBaseManager.exclusiveAccess.lock();
				DataBaseManager db = DataBaseManager.getInstance();
				
				ArrayList<String> folders = db.getUnsyncFolder(peerGroupID);
				for (String path : folders) {
					new File(path).mkdirs();
				}
				
				
				ArrayList<FileWithLocalSource> files = db.getFilesWithLocalSource(peerGroupID);
				
				for (FileWithLocalSource fileWithLocalSource : files) {
					File f = new File(fileWithLocalSource.getLocalSourcePath());
					FileUtils.copy(f, new File(fileWithLocalSource.getAbsFilePath()));
				}
				DataBaseManager.exclusiveAccess.unlock();
			}
		});
		
		
	}

	
}
