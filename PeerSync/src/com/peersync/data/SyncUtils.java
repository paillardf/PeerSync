package com.peersync.data;

import java.io.File;
import java.util.ArrayList;

import com.peersync.models.ClassicFile;
import com.peersync.models.Event;
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
		
		SharedFolderVersion sfv  = new SharedFolderVersion(advSFV.getUID(), advSFV.getName());
		
		for (StackVersion stackV : advSFV.getStackVersionList()) {
			
			StackVersion ownedStackV = ownedSFV.getStackVersion(stackV.getUID());
			if(ownedStackV.getLastUpdate()<stackV.getLastUpdate())
				sfv.addStackVersion(ownedStackV);
		}
		
		return sfv;
		
		
	}
	
	public static void startIntelligentSync(final String peerGroupID, final String peerID){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				DataBaseManager.exclusiveAccess.lock();
				DataBaseManager db = DataBaseManager.getInstance();
				
				ArrayList<ClassicFile> folders = db.getUnsyncFolder(peerGroupID);
				for (ClassicFile classicFile : folders) {
					new File(classicFile.getAbsFilePath()).mkdirs();
					db.updateEventStatus(classicFile.getRelFilePath(), classicFile.getFileHash(), classicFile.getSharedFolderUID(), Event.STATUS_LOCAL_OK);

				}
				
				
				ArrayList<FileWithLocalSource> files = db.getFilesWithLocalSource(peerGroupID);
				
				for (FileWithLocalSource fileWithLocalSource : files) {
					File f = new File(fileWithLocalSource.getLocalSourcePath());
					FileUtils.copy(f, new File(fileWithLocalSource.getAbsFilePath()));
					db.updateEventStatus(fileWithLocalSource.getRelFilePath(), fileWithLocalSource.getFileHash(), fileWithLocalSource.getSharedFolderUID(), Event.STATUS_LOCAL_OK);
				}
				
				ArrayList<ClassicFile> filesToRemove = db.getFilesToRemove(peerGroupID);
				
				for (ClassicFile fileToRemove : filesToRemove) {
					File f = new File(fileToRemove.getAbsFilePath());
					boolean res = FileUtils.deleteFile(f);
					//Ici pb : le hash est nul ... Je pense pas que ça pose de vrais pb, mais on identifie mal l'event du coup
					db.updateEventStatus(fileToRemove.getRelFilePath(), fileToRemove.getFileHash(), fileToRemove.getSharedFolderUID(), res ? Event.STATUS_LOCAL_OK : Event.STATUS_LOCAL_CONFLICT);
				}
				
				// TODO : vérifier, car en pratique ne fonctionne pas sur le dossier (conflit) --> temporiser ?
				ArrayList<ClassicFile> foldersToRemove = db.getFoldersToRemove(peerGroupID);
				for (ClassicFile folderToRemove : foldersToRemove) {
					File f = new File(folderToRemove.getAbsFilePath());
					boolean res = FileUtils.deleteFile(f);
					//Ici pb : le hash est nul ... Je pense pas que ça pose de vrai pb, mais on identifie mal l'event du coup
					db.updateEventStatus(folderToRemove.getRelFilePath(), folderToRemove.getFileHash(), folderToRemove.getSharedFolderUID(), res ? Event.STATUS_LOCAL_OK : Event.STATUS_LOCAL_CONFLICT);
					if(!res)
					{
						Event e = new Event(folderToRemove.getSharedFolderUID(),System.currentTimeMillis(), folderToRemove.getFileHash(),-1,0,null,null,Event.ACTION_CREATE,  peerID ,Event.STATUS_LOCAL_OK);
						e.save();
					}
				}
				
				DataBaseManager.exclusiveAccess.unlock();
			}
		}).run();
		
		
	}

	
}
