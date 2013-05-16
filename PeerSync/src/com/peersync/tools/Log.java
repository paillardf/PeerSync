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

	public static boolean DEBUG = true;
	private static String DEFAULT = "DEFAULT";

	public static void d(String s){
		d(DEFAULT , s);
	}

	public static void d(final String tag , final String s){
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if(DEBUG)
        			DebugWindows.getInstance().addLog(tag, s);
            }
        });
		
	}

	public static void e(String name, String string) {
		// TODO Auto-generated method stub

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

		public void addLog(String tag, String v){
			
			
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss - ");
			Date date = new Date();
			for (int i =0; i< Onglets.getComponentCount(); i++) {
				if(Onglets.getTitleAt(i).compareTo(tag)==0){
					JTextPane pane = (JTextPane) ((JScrollPane) Onglets.getComponent(i)).getViewport().getView();
					pane.setText(pane.getText()+"\n"+dateFormat.format(date) + v);
					return;
				}

			}

			JTextPane txt = new JTextPane();
			JScrollPane jsp = new JScrollPane(txt);
			txt.setText(dateFormat.format(date) +v);
			Onglets.addTab(tag, null, jsp, null); 
		}

		public static DebugWindows getInstance(){
			if(windows==null)
				new DebugWindows();
			return windows;
		}


	}






}
