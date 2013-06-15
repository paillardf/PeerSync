package com.peersync.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class Log {

	//	public static boolean DEBUG = true;
	//private static String DEFAULT_FILTER = "DEFAULT";

	public static final String LEVEL_INFORM = Log.class.getName()+"."+"LEVEL_INFORM";
	public static final String LEVEL_DEBUG = Log.class.getName()+"."+"LEVEL_DEBUG";
	public static final String LEVEL_WARN = Log.class.getName()+"."+"LEVEL_WARN";
	public static final String LEVEL_SEVERE = Log.class.getName()+"."+"LEVEL_SEVERE";




	public static void i(String log){
		i(log, null);

	}

	public static void i(String log, String filter){
		log(LEVEL_INFORM , log, filter);
	}

	public static void d(String log){
		d(log, null);
	}

	public static void d(String log, String filter){
		log(LEVEL_DEBUG , log, filter);
	}

	public static void w(String log){
		w(log, null);
	}

	public static void w(String log, String filter){
		log(LEVEL_WARN , log, filter);
	}

	public static void s(String log){
		s(log, null);
	}

	public static void s(String log, String filter){
		log(LEVEL_SEVERE , log, filter);



	}


	private static void log(String levelDebug, final String log , final String filter){
		StackTraceElement stackTrace = null;
		if(filter!=null)
			stackTrace = Thread.currentThread().getStackTrace()[3];
		else
			stackTrace = Thread.currentThread().getStackTrace()[4];
		final String className = stackTrace.getFileName();         
		String classPackage = stackTrace.getClassName();     
		boolean enable = System.getProperty(classPackage, "true").equals("true");
		enable = System.getProperty(levelDebug, "true").equals("true")&&enable;
		if(filter!=null)
			enable = System.getProperty(filter, "true").equals("true")&&enable;

		if(enable){
			final String methodName = stackTrace.getMethodName();
			final int lineNumber = stackTrace.getLineNumber();



			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//	if(DEBUG)
					DebugWindows.getInstance().addLog(className, methodName +"() l"+ lineNumber ,log);
				}
			});

		}
	}




	static class DebugWindows extends JFrame{

		private static DebugWindows windows;
		private JTabbedPane Onglets;

		private DebugWindows(){
			super(Constants.getInstance().PEERNAME);
			windows=this;

			windows.setSize(400, 400);
			windows.setLocationRelativeTo(null);
			windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



			Onglets = new JTabbedPane();
			//JTextPane txt = new JTextPane();
			//JScrollPane jsp = new JScrollPane(txt);
			//Onglets.addTab(DEFAULT, null, jsp, null); 

			this.getContentPane().add(Onglets);

			windows.setVisible(true);
		}

		private void addLog(String tag, String methode,  String v){


			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");
			Date date = new Date();
			for (int i =0; i< Onglets.getComponentCount(); i++) {
				if(Onglets.getTitleAt(i).compareTo(tag)==0){
					JTextPane pane = (JTextPane) ((JScrollPane) Onglets.getComponent(i)).getViewport().getView();
					pane.setText(pane.getText()+"\n"+dateFormat.format(date) + methode +": "+ v);
					return;
				}

			}

			JTextPane txt = new JTextPane();
			JScrollPane jsp = new JScrollPane(txt);
			txt.setText(dateFormat.format(date)  + methode +": "+v);
			Onglets.addTab(tag, null, jsp, null); 
		}

		private static DebugWindows getInstance(){
			if(windows==null)
				new DebugWindows();
			return windows;
		}


	}




	public static boolean isDebug() {
		return System.getProperty(LEVEL_DEBUG, "true").equals("true");
	}






}
