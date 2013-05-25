package com.peersync.network.content.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jxta.id.ID;

import com.peersync.data.DataBaseManager;
import com.peersync.network.content.model.FileAvailability;


public class FilesInfoManager {


	private Map<String, SmartFileInfo> smartList = new HashMap<String, SmartFileInfo>();

	private DataBaseManager dataBase;

	public FilesInfoManager() {
		dataBase = DataBaseManager.getInstance();
	}


	public void addFilesAvailability(List<FileAvailability> fAv, ID owner){
		
		for (int i = 0; i < fAv.size(); i++) {
			
		}
	}
	
	public void addFileAvailability(FileAvailability fAv, ID owner){
		String hash = fAv.getHash();
		if(!smartList.containsKey(hash)){
			smartList.put(hash, new SmartFileInfo(hash));
		}
		SmartFileInfo sFileInfo = smartList.get(hash);
		sFileInfo.addFileAvailability(fAv, owner);
	}


	public void writeDownloadedSegment(String hash, long offset, int length) {
		// TODO Auto-generated method stub
		
	}

}
