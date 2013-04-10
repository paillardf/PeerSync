package com.peersync.tools;

import java.io.File;

public class FileUtils {

	public static boolean copy( File source, File destination ){ //Methode permettant la copie d'un fichier 
		boolean resultat = false; 
		// Declaration des flux 
		java.io.FileInputStream sourceFile=null; 
		java.io.FileOutputStream destinationFile=null; 
		try { 
			// Création du fichier : 
			destination.createNewFile(); 
			// Ouverture des flux 
			sourceFile = new java.io.FileInputStream(source); 
			destinationFile = new java.io.FileOutputStream(destination); 
			// Lecture par segment de 0.5Mo 
			byte buffer[]=new byte[512*1024]; 
			int nbLecture; 
			while( (nbLecture = sourceFile.read(buffer)) != -1 ) { 
				destinationFile.write(buffer, 0, nbLecture); 
			} 

			// Copie réussie 
			resultat = true; 
		} catch( java.io.FileNotFoundException f ) { 
		} catch( java.io.IOException e ) { 
		} finally { 
			// Quoi qu'il arrive, on ferme les flux 
			try { 
				sourceFile.close(); 
			} catch(Exception e) { } 
			try { 
				destinationFile.close(); 
			} catch(Exception e) { } 
		} 
		return( resultat ); 
	}

}
