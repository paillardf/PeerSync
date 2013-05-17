package com.peersync.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassesLister {


	private static ClassesLister instance=null;

	public static ClassesLister getInstance()
	{
		if(instance==null)
			instance = new ClassesLister();
		return instance;

	}

	private ClassesLister()
	{


	}

	/**
	 * Cette m�thode permet de lister toutes les classes d'un package donn�
	 * 
	 * @param pckgname Le nom du package � lister
	 * @return La liste des classes
	 */
	public List<Class> getClasses(String pckgname) {
		// Cr�ation de la liste qui sera retourn�e
		ArrayList<Class> classes = new ArrayList<Class>();

		// On r�cup�re toutes les entr�es du CLASSPATH
		String [] entries = System.getProperty("java.class.path")
				.split(System.getProperty("path.separator"));

		// Pour toutes ces entr�es, on verifie si elles contiennent
		// un r�pertoire ou un jar
		for (int i = 0; i < entries.length; i++) {
			try {
				if(entries[i].endsWith(".jar")){

					classes.addAll(traitementJar(entries[i], pckgname));

				}else{
					classes.addAll(traitementRepertoire(entries[i], pckgname));
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return classes;
	}

	/**
	 * Cette m�thode retourne la liste des classes pr�sentes
	 * dans un r�pertoire du classpath et dans un package donn�
	 * 
	 * @param repertoire Le r�pertoire o� chercher les classes
	 * @param pckgname Le nom du package
	 * @return La liste des classes
	 */
	private Collection<Class> traitementRepertoire(String repertoire, String pckgname) throws ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();

		// On g�n�re le chemin absolu du package
		StringBuffer sb = new StringBuffer(repertoire);
		String[] repsPkg = pckgname.split("\\.");
		for (int i = 0; i < repsPkg.length; i++) {
			sb.append(System.getProperty("file.separator") + repsPkg[i]);
		}
		File rep = new File(sb.toString());

		// Si le chemin existe, et que c'est un dossier, alors, on le liste
		if(rep.exists() && rep.isDirectory()){
			// On filtre les entr�es du r�pertoire
			FilenameFilter filter = new DotClassFilter();
			File[] liste = rep.listFiles(filter );

			// Pour chaque classe pr�sente dans le package, on l'ajoute � la liste
			for (int i = 0; i < liste.length; i++) {
				classes.add(Class.forName(pckgname + "." + liste[i].getName().split("\\.")[0]));
			}
		}

		return classes;
	}

	/**
	 * Cette m�thode retourne la liste des classes pr�sentes dans un jar du classpath et dans un package donn�
	 *
	 * @param repertoire Le jar o� chercher les classes
	 * @param pckgname Le nom du package
	 * @return La liste des classes
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private Collection<Class> traitementJar(String jar, String pckgname) throws IOException, ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();

		JarFile jfile = new JarFile(jar);
		String pkgpath = pckgname.replace(".", "/");


		// Pour chaque entr�e du Jar
		for (Enumeration<JarEntry> entries = jfile.entries(); entries.hasMoreElements();) {
			JarEntry element = entries.nextElement();

			// Si le nom de l'entr�e commence par le chemin du package et finit par .class
			if(element.getName().startsWith(pkgpath)
					&& element.getName().endsWith(".class")){

				String nomFichier = element.getName().substring(pckgname.length() + 1);

				classes.add(Class.forName(pckgname + "." + nomFichier.split("\\.")[0]));

			}

		}

		return classes;
	}

	/**
	 * Cette classe permet de filtrer les fichiers d'un r�pertoire. Il n'accepte que les fichiers .class.
	 */
	private class DotClassFilter implements FilenameFilter{

		public boolean accept(File arg0, String arg1) {
			return arg1.endsWith(".class");
		}


	}

}
