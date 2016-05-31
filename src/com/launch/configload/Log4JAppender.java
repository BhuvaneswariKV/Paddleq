package com.launch.configload;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.apache.log4j.*;

import com.gopaddle.utils.ConfigReader;



public class Log4JAppender extends WriterAppender  implements Runnable{ 

	public static final String consoleName = "go Paddle";
	static Logger log=Logger.getLogger(Log4JAppender.class);
	  
	 static MessageConsole myConsole;
	 static MessageConsoleStream stream ;
	 static ConfigReader con=ConfigReader.getConfig();
	 static String pathResource;
	
	/**
	  * creates the Log4JAppender. 
	  */ 
	 public Log4JAppender() { 
			 
	 } 
	 //prints the log from File  
	 public static MessageConsole findConsole(String name) { 
	  ConsolePlugin plugin = ConsolePlugin.getDefault(); 
	  IConsoleManager conMan = plugin.getConsoleManager(); 
	  IConsole[] existing = conMan.getConsoles(); 
	  for (int i = 0; i < existing.length; i++) 
	   if (name.equals(existing[i].getName())) 
	    return (MessageConsole) existing[i]; 
	  // no console found, so create a new one 
	  MessageConsole myConsole = new MessageConsole(name, null); 
	  conMan.addConsoles(new IConsole[] { myConsole }); 
	  MessageConsoleStream out = myConsole.newMessageStream();
	  out.println("Hello from Generic console sample action");
	  displayConsole(out);
	  try {
		out.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  	}
	  return myConsole; 
	 }
	 //Display the log to console
	 public static void displayConsole(MessageConsoleStream stream){
		try
		 {
			ConfigReader con=ConfigReader.getConfig();
			pathResource=con.Get("##ROOTPATH")+"logs/log.log";
			log.info("Path resource"+pathResource);
			InputStream inputstream =new FileInputStream(pathResource);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
			String strLine;
			while(true){
				strLine=br.readLine();
				if (strLine == null)
			     {
			       Thread.sleep(1*1000);
			     } 
				 else
				 {
					 stream.println(strLine);
					 System.out.println("Hai:::"+strLine);
				 }
			}
		 }catch (FileNotFoundException fe){					
			log.error("File Not Found",fe);
		 }catch (IOException e){
			log.error("IOEXception occured:", e);
		 } catch (InterruptedException e) {
					// TODO Auto-generated catch block
			e.printStackTrace();
		 	}
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
	 myConsole= findConsole(consoleName);
	 stream =myConsole.newMessageStream();		
	 WriterAppender writeAppender=new WriterAppender(new SimpleLayout(),stream);
		
	}
	}
