package com.peersync.network.content.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jxta.id.ID;

import com.peersync.data.DataBaseManager;
import com.peersync.models.ClassicFile;
import com.peersync.models.Event;
import com.peersync.models.FileToDownload;
import com.peersync.network.content.message.DataRequestMessage;
import com.peersync.tools.Constants;
import com.peersync.tools.FileUtils;


public class FilesInfoManager {


	private Map<String, SmartFileInfo> remoteFilesAvailability = new HashMap<String, SmartFileInfo>();

	private Map<String, DownloadingFile> localFileAvailability = new HashMap<String, DownloadingFile>();

	class DownloadingFile{
		FileAvailability realFileAvailability;
		FileAvailability futurFileAvailability;

		public DownloadingFile(String hash) {
			realFileAvailability = new FileAvailability(hash);
			futurFileAvailability = new FileAvailability(hash);
		}
	}

	private DataBaseManager dataBase;


	public FilesInfoManager() {
		dataBase = DataBaseManager.getInstance();
	}


	public void addFilesAvailability(List<FileAvailability> fAvList, ID owner){

		for (int i = 0; i < fAvList.size(); i++) {
			addFileAvailability(fAvList.get(i), owner);
		}
	}

	public void addFileAvailability(FileAvailability fAv, ID owner){
		synchronized (fAv) {
			String hash = fAv.getHash();
			if(!remoteFilesAvailability.containsKey(hash)){
				remoteFilesAvailability.put(hash, new SmartFileInfo(hash));
			}
			SmartFileInfo sFileInfo = remoteFilesAvailability.get(hash);
			sFileInfo.addFileAvailability(fAv, owner);
		}

	}


	public void writeDownloadedSegment(String hash, long offset, int length, byte[] data)  {

		DownloadingFile fAv = localFileAvailability.get(hash);
		synchronized (fAv) {
			if(fAv!=null){

				try{
					File myFile = new File (Constants.TEMP_PATH+Constants.getInstance().PEERNAME+"\\"+hash+".tmp");
					//Create the accessor with read-write access.
					RandomAccessFile accessor = new RandomAccessFile (myFile, "rws");
					if(!myFile.exists()){
						accessor.setLength(dataBase );
					}


					accessor.seek(offset);
					accessor.write(data, 0, length);
					accessor.close(); 
					
					fAv.realFileAvailability.addSegment(offset, length);
					dataBase.saveFileAvailability(fAv.realFileAvailability);
					if(fAv.realFileAvailability.getSegments().size()==1){
						BytesSegment segment = fAv.realFileAvailability.getSegments().get(0);
						if(segment.offset == 0 && segment.length == dataBase.getFileSize()){ // FICHIER COMPLET
							DataBaseManager.exclusiveAccess.lock();
							
							DataBaseManager db = DataBaseManager.getInstance();
							ArrayList<ClassicFile> files = db.getFilesToSyncConcernByThisHash(hash);
							for (ClassicFile classicFile : files) {
								
								if(FileUtils.copy(myFile, new File(classicFile.getAbsFilePath())))
									db.updateEventStatus(classicFile.getRelFilePath(), hash, classicFile.getSharedFolderUID(), Event.STATUS_OK);
							}
							DataBaseManager.exclusiveAccess.unlock();
							myFile.delete();
						}
					}
					
					
					
				}catch(IOException e){
					//FIXME
					fAv.futurFileAvailability.substract(new BytesSegment(offset, length));
				}
			}
		}

	}


	public void bookFileSegment(DataRequestMessage message) {
		String hash = message.getHash();
		DownloadingFile fAv = localFileAvailability.get(hash);
		synchronized (fAv) {
			fAv.futurFileAvailability.addSegment(message.getOffset(), message.getLength());
		}

	}

	public void cancelBookFileSegment(DataRequestMessage message) {
		String hash = message.getHash();
		DownloadingFile fAv = localFileAvailability.get(hash);
		synchronized (fAv) {
			fAv.futurFileAvailability.substract(new BytesSegment(message.getOffset(), message.getLength()));
		}
	}


	public SegmentToDownload getBestFileAvailability(String sharedFolderUID, ID pipeID) {

		synchronized (localFileAvailability){
			ArrayList<FileToDownload> listFile = getFilesToDownload(sharedFolderUID);

			for (FileToDownload fileToDownload : listFile) {
				String hash = fileToDownload.getFileHash();
				SmartFileInfo fAv = remoteFilesAvailability.get(hash);
				if(fAv==null){
					continue;
				}
				if(!localFileAvailability.containsKey(hash)){
					localFileAvailability.put(hash, new DownloadingFile(hash));
				}
				DownloadingFile localFAv = localFileAvailability.get(hash);
				SegmentToDownload bestChoice = fAv.getBestChoice(localFAv.futurFileAvailability, pipeID);
				if(bestChoice!=null){
					localFAv.futurFileAvailability.addSegment(bestChoice.getSegment().offset, bestChoice.getSegment().length);
					return bestChoice;
				}
					
			}
			return null;	
		}
	}


	public ArrayList<FileToDownload> getFilesToDownload(String sharedFolderUID) {
		return dataBase.getFilesToDownloadForASharedFolder(sharedFolderUID);
	}

}
