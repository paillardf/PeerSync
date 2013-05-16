package com.peersync.tools;

import java.util.prefs.Preferences;

public class PreferencesManager{

	private static final String PORT = "port";

	private static PreferencesManager instance;

	private Preferences pref;

	private PreferencesManager(){
		
		pref = Preferences.userRoot().node(Constants.getInstance().PREFERENCES_PATH());
		

	}

	public static PreferencesManager getInstance(){

		if(instance==null){
			instance = new PreferencesManager();
		}
		return instance;
	}

//	public void setPort(int i) {
//		pref.putInt(PORT, i);
//	}
//	
//	public int getPort() {
//		return pref.getInt(PORT, 9044);
//	}
	
	

}
