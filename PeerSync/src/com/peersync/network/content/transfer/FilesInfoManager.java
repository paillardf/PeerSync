package com.peersync.network.content.transfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jxta.id.ID;

import com.peersync.data.DataBaseManager;
import com.peersync.network.content.message.DataRequestMessage;
import com.peersync.network.content.model.BytesSegment;
import com.peersync.network.content.model.FileAvailability;
import com.peersync.tools.Constants;


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

					accessor.seek(offset);
					accessor.write(data, 0, length);
					accessor.close(); 

					fAv.realFileAvailability.addSegment(offset, length);
					dataBase.saveFileAvailability(fAv.realFileAvailability);
				}catch(IOException e){
					//fAv.futurFileAvailability.substract(new BytesSegment(offset, length));
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


	public SegmentToDownload getBestFileAvailability(String hash) {

		SmartFileInfo fAv = remoteFilesAvailability.get(hash);
		if(fAv==null){
			return null;
		}
		synchronized (localFileAvailability) {
			if(!localFileAvailability.containsKey(hash)){
				localFileAvailability.put(hash, new DownloadingFile(hash));
			}
			DownloadingFile localFAv = localFileAvailability.get(hash);
			return fAv.getBestChoice(localFAv.realFileAvailability);
		}
	}

}
