package com.config.plugin;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.TreeSelection;

import com.gopaddle.singlesignon.GoogleAuthorizationSignIn;
import com.gopaddle.ui.DesignUI;
import com.gopaddle.utils.ConfigReader;
import com.launch.configload.ConfigurationManipulation;

public class GopaddleLaunchShortcut implements ILaunchShortcut {

	ConfigReader con=ConfigReader.getConfig();
	String platformVersion;
	String rootPath;
	Logger log;
	private String handle;
	
	
	/*Launch start from here
	 * Read Preconfiguration of launch
	 * Then initiate the UI of the Plugin*/ 
	@Override
	public void launch(ISelection selection, String mode) {
		// TODO Auto-generated method stub
			IResource resource = null;
			
			if (selection instanceof TreeSelection)
	        {
	            Object element = ((TreeSelection) selection).getFirstElement();

	            if (element instanceof IResource)
	            {
	                resource= (IResource) element;
	            }

	            if (element instanceof IJavaElement)
	            {
	                resource=((IJavaElement) element).getResource();
	            }
	  
	        }

			
			//copy the resource name into the Singleton Object
			con.Put("##RESOURCENAME", resource.getName());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			//Read the project
			IProject project = workspace.getRoot().getProject(resource.getName());
			
			if(project.isOpen())
			{
				URL location = GopaddleLaunchShortcut.class.getProtectionDomain().getCodeSource().getLocation();
				String path=location.getFile().toString();
			    //extract_jar_toSystem();
			    rootPath=path;
			    //to show log4j in console window
			    MessageConsole console = new MessageConsole("goPaddle deployment", null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
				MessageConsoleStream stream = console.newMessageStream();

				System.setOut(new PrintStream(stream));
				System.setErr(new PrintStream(stream));
				//custom rootPath enable in log4j.properties file
				System.setProperty("log4jDirectory",rootPath+"logs/log_console.log");
				PropertyConfigurator.configure( rootPath +  "/log4j.properties");
			 	Logger log = Logger.getLogger(GopaddleLaunchShortcut.class);   
		        log.info("Verify path is Correct : "+System.getProperty("log4jDirectory"));
					    
		        try {
				        //For Windows the path contain the \\.So it replace it by / 
				        rootPath=rootPath.replace("\\", "/");
				        con.Put("##ROOTPATH",rootPath);
				        log.info("Verify RootPath : "+con.Get("##ROOTPATH"));
					    //Trigger the Google Single sign on
				        GoogleAuthorizationSignIn googleAuthorizationSignIn=new GoogleAuthorizationSignIn();
				        handle=con.Get("##HANDLE");
						if(handle!=null){
							log.info("Googele Authorization stimulated");			
						      //initiate the configuartion
					        ConfigurationManipulation configurationManipulation=new ConfigurationManipulation();
					        System.out.println("Configuration initiation Done!!!");
						     //Validate the Project contains the .git folder
						    IFile gitConfigExists=configurationManipulation.folderIsExists(resource, ".git", "config");
						    log.info("Selected Resource is :"+resource.getName());
						    log.info("Git validation is completed ");
						    if(gitConfigExists==null)
					        {
					        	configurationManipulation.messgeShow("Config file is not found", "Please Verify Your eclipse project contains the .git folder with config.or your configuration path is someway collapsed..");
					        	log.info("Config file not Found in the project under .git repository");
					        	
					        }
							else if(gitConfigExists.getName().toString()=="config")
						    {
								//check builder exists in the project
					        	configurationManipulation.isBuilderExists(resource);
					        	String builderList[]=configurationManipulation.builders.toArray(new String[configurationManipulation.builders.size()]);
					        	int builderListLength=builderList.length;
					        	if(builderListLength==0)
							    {
								
					        		log.error("Target is not Determined.The Packaging target is unable to find.Gopaddle only allows for whether gradle,maven and ant based packaging Projects.");
					        		configurationManipulation.messgeShow("Target is not Determined", "The Packaging target is unable to find.Gopaddle only allows for whether gradle,maven and ant based packaging Projects.");
					        	}
						      	else
						       	{	
						      		//get the builder as like maven or gradle
					        		String builder = null;
					        		builder=builderList[0];
					        		if(builder!=null)
					        		{
						        			//get target list if ant or gradle
						        			configurationManipulation.getBuilderList(resource,builder);
						        			//convert git url to input given specification(git@example.com)
						        			configurationManipulation.convertStringIntoGitUrl(resource,gitConfigExists);
							        		//check classpath file exists for version of java 
							        		boolean fileExists=configurationManipulation.fileExists(resource,".classpath");
							        	
						        			//get the version of java
											platformVersion=configurationManipulation.isClasspathExists(resource,builder);
											new DesignUI(fileExists,platformVersion,builder,resource);
							        	}
							        	else
							        		configurationManipulation.messgeShow("Packing was not choose", "U didn't choose the any packing.Please Choose atleast one packing");
							        	}
									}   		
							       	else
									{
												log.error("Not supported, Please Verify Your eclipse project contains the .git folder. Currently Gopaddle only supports git projects.Not support the local system projects");
												configurationManipulation.messgeShow("Not supported", "Please Verify Your eclipse project contains the .git folder."
														+ "Currently Gopaddle only supports git projects.Not support the local system projects.");
									}
							
							}
							else
								log.info("Handle is null.So can't proceed....");
						}catch (Exception e) {
								// TODO Auto-generated catch block
							log.error("",e);
						}
			   }
			   else
			   {
				   	Shell shell=new Shell(SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);//added newSWT.APPLICATION_MODAL
					MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK| SWT.CANCEL);
					dialog.setText("project was in closed");
					dialog.setMessage("Please open the current project");
					shell.setFocus();
					dialog.open();
				}
							
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IResource resource = null;
		if (editor instanceof TreeSelection)
        {
            Object element = ((TreeSelection) editor).getFirstElement();

            if (element instanceof IResource)
            {
                resource= (IResource) element;
            }

            if (element instanceof IJavaElement)
            {
                resource=((IJavaElement) element).getResource();
            }
        }

	}	 
	//Extract the payload folder,icons,logs and config files in the users directory from the jar of plugin
	void extract_jar_toSystem(){
		  
		//Extract Jar file to class
		  final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		  JarFile jar;
		  try {
			 jar = new JarFile(jarFile);		
			 rootPath=System.getProperty("user.home")+"/"+"gopaddleExtract/";
			 Enumeration enumEntries = jar.entries();
			 String path;
			 File rootDirectory=new File(rootPath);
			 if(! rootDirectory.exists()) 
			     rootDirectory.mkdirs();
			 while (enumEntries.hasMoreElements()) {
			     JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
			     File f = new java.io.File(rootPath+ File.separator + file.getName());
			     if (file.isDirectory()) {
			    	 // if its a directory, create it
			    	 f.mkdir();
			         continue;
			     }
			     InputStream inputStream = jar.getInputStream(file); // get the input stream
			     File retrivedContent=new File(file.getName());
			     if(retrivedContent.getParent()!=null )
			     {
				     File parentDir = retrivedContent.getParentFile();
				     path=rootPath+"\\"+parentDir.getName();
				     File sample=new File(path);
					 if(! sample.exists()) 
					 {
				        if(sample.mkdirs())
				        System.out.println("New Path created");
					 }
				     if(retrivedContent.getParentFile().getAbsolutePath().contains("payload")|| retrivedContent.getParentFile().getAbsolutePath().contains("icons")|| retrivedContent.getParentFile().getAbsolutePath().contains("logs"))
				     {			    	      
			     	     FileOutputStream fileOutputStream = new FileOutputStream(f);
			     	     while (inputStream.available() > 0) {  // write contents of 'is' to 'fos'
			     	    	 fileOutputStream.write(inputStream.read());
			     	     }		     
				     	     fileOutputStream.close();
				     }
			     }
			     else if(retrivedContent.getName().equals("config") || retrivedContent.getName().equals("log4j.properties"))
			     {
			    	 FileOutputStream fos = new FileOutputStream(f);
				     while (inputStream.available() > 0) {  // write contents of 'is' to 'fos'
				         fos.write(inputStream.read());
				     }
				     
				     fos.close();
			     }

			     inputStream.close();
			 }
		}catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Jar fie retrival Error",e);
		}
	}

}
