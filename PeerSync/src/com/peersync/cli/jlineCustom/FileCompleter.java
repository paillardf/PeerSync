package com.peersync.cli.jlineCustom;
import static jline.internal.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;

import jline.console.completer.Completer;
import jline.internal.Configuration;

import com.peersync.tools.Log;

public class FileCompleter 

implements Completer
{
	// TODO: Handle files with spaces in them
	//Meilleur gestion de Windows (winRegexp, changement des / en \ dans la completion)
	// TODO : g�rer les noms de fichiers comportant des '

	private static final boolean OS_IS_WINDOWS;
	private static final String winRegexp =  "^[a-zA-Z]:.*$";

	static {
		String os = Configuration.getOsName();
		OS_IS_WINDOWS = os.contains("windows");
	}


	public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
		// buffer can be null
		checkNotNull(candidates);

		if (buffer == null) {
			buffer = "";
		}

		String ubuffer = buffer;
		if (OS_IS_WINDOWS) {
			buffer = buffer.replace('/', '\\');
		}
		
		
		if(buffer.startsWith("'"))
			buffer = buffer.replaceFirst("'", "");
		if(!buffer.endsWith("\\'") && buffer.endsWith("'"))
			buffer = buffer.substring(0,buffer.length()-1);
		
		buffer=buffer.replace("\\'", "'");
		String translated = buffer;

		File homeDir = getUserHome();

		boolean test = translated.matches(winRegexp);


		// Special character: ~ maps to the user's home directory
		if (translated.startsWith("~" + separator())) {
			translated = homeDir.getPath() + translated.substring(1);
		}
		else if (translated.startsWith("~")) {
			translated = homeDir.getParentFile().getAbsolutePath();
		}
		else if (!((translated.startsWith(separator()) && !OS_IS_WINDOWS)  || (OS_IS_WINDOWS && translated.matches(winRegexp)) )) {
			String cwd = getUserDir().getAbsolutePath();
			translated = cwd + separator() + translated;
		}

		File file = new File(translated);
		final File dir;

		if (translated.endsWith(separator())) {
			dir = file;
		}
		else {
			dir = file.getParentFile();
		}

		File[] entries = dir == null ? new File[0] : dir.listFiles();



		return matchFiles(ubuffer,buffer, translated, entries, candidates);
	}

	protected String separator() {
		return File.separator;
	}

	protected File getUserHome() {
		return Configuration.getUserHome();
	}

	protected File getUserDir() {
		return new File(".");
	}

	protected int matchFiles(String untouchedBuffer,final String buffer, final String translated, final File[] files, final List<CharSequence> candidates) {
		if (files != null) {



			int matches = 0;

			// first pass: just count the matches
			for (File file : files) {
				if (file.getAbsolutePath().startsWith(translated)) {
					matches++;
				}
			}
			for (File file : files) {
				if (file.getAbsolutePath().startsWith(translated)) {
					CharSequence name = file.getName() + (matches == 1 && file.isDirectory() ? separator() : "");
					if(!render(file, name).toString().startsWith(buffer))
					{
						String tmp = new String();
						if(OS_IS_WINDOWS)
							tmp = buffer.substring(0,buffer.lastIndexOf("\\")+1);
						else
							tmp = buffer.substring(0,buffer.lastIndexOf("/")+1);
						String res = tmp+render(file, name).toString();
						if(matches==1)
							candidates.add(quoteIfWhitespaces(res));
						else
							candidates.add(res);
					}	
					else
					{
						String res =render(file, name).toString();
						if(matches==1)
							candidates.add(quoteIfWhitespaces(res));
						else
							candidates.add(res);
					}
				}
			}

			if(matches==0)
			{
				candidates.add(buffer); 
				return 0;
			}
		}
		else
			candidates.add(untouchedBuffer); 
		
		
		return 0;

		//		final int index = buffer.lastIndexOf(separator());
		//

		//		return index + separator().length();
	}

	protected String quoteIfWhitespaces(String s)
	{
		s=s.replace("'", "\\'");
		if(s.contains(" "))
				return "'"+s+"' ";
		else
			return s;

	}
	
	
	
	protected CharSequence render(final File file, final CharSequence name) {
		return name;
	}
}

