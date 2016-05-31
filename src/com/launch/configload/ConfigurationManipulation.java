package com.launch.configload;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gopaddle.utils.ConfigReader;


public class ConfigurationManipulation {
	
	Logger log=Logger.getLogger(ConfigurationManipulation.class);
	
	ConfigReader con=ConfigReader.getConfig();
	public static String target[]=new String[30];
	int index = 0;
	String resultString,pack=null;
	public  String version=null;
	ArrayList<String> antTarget = new ArrayList<String>();
	public static ArrayList<String> builders=new ArrayList<String>();
	//default constructor
	public ConfigurationManipulation()
	{
		
	}
	//Convert the proper ssh .git url
	public void convertStringIntoGitUrl(IResource resource,IFile file)throws CoreException{
			
		resultString=readStringInFile(file, "url", "=");
		if(resultString!=null)
		{
			resultString=replaceString(resultString, "://", "@");
			resultString=replaceString(resultString, "/",":" );
			resultString=append(resultString, "git");
			writeConfig("##REPOPATH",resultString);
		}
		}
	
	//Add the key and value pair into the Singleton Object
	private void writeConfig(String key, String value) {
		// TODO Auto-generated method stub
		con.Put(key,value);
	}
	
	//Append the String to existing String
		//It checks the Git url must ends with .git
	public String append(String input,String appendString)
	{
		if(input.endsWith(appendString))
		{			
		}
		else
			 input=input+appendString;
		
		return input;
	}
	
	//Replace the given string by the existing String
	public String replaceString(String input,String replacedString,String newString)
	{
		return input.replaceFirst(replacedString, newString);
		
	}
		
	//Check Builder is Exist in The Project
	public void isBuilderExists(IResource resource)
	{
		builders.clear();
		try {
			if(fileExists(resource, "pom.xml"))
				builders.add("Maven");
			
			if(fileExists(resource, "build.xml"))
				builders.add("Ant");
			if(fileExists(resource, "build.gradle"))
				builders.add("Gradle");
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			log.error("",e);
		}
		
		 log.info("Builders are : "+builders);
	}

	/* Get the Builder List of Project */
	public String getBuilderList(IResource resource, String curentPack) throws CoreException
	{
		//Maven Build Target list is  install,package,deploy and integretation-test
		if(curentPack.equals("Maven"))
			writeConfig("##BUILDER", "Maven");
		
		else if(curentPack.equals("Ant")){
			
			writeConfig("##BUILDER", "Ant");
			//then Find the List of build target
			IFile file=getFile(resource, "build.xml");
			resultString=null;
			String fileNameToGive=file.getLocation().toString();
			try {
				validateXmlDocument(fileNameToGive);
				
				if(antTarget.isEmpty())
				{
					log.info("Ant TargetList received as empty");
				}
				else
				{
					//if duplicate is exists in the Ant target list then remove
					Set<String> tempduplicateRemove = new HashSet<>();
					tempduplicateRemove.addAll(antTarget);
					antTarget.clear();
					antTarget.addAll(tempduplicateRemove);
					target=antTarget.toArray(new String[antTarget.size()]);
				}
		
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				log.error("SAX Exception :",e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("IO Exception :",e);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				log.error("ParserconfigurationException : ",e);
			}
			
		}
		else if(curentPack.equals("Gradle")){			
			con.Put("##BUILDER", "Gradle");
			findGradleTaskList(resource);
		}
		return pack;
	}
	
	/* Find The Gradle Build Target*/
    private void findGradleTaskList(IResource resource) 
    {
    	try{    		
    	     //This command used to exeute the gradle tasks
			String command[] = {"cmd.exe", "/c", "gradle -q tasks"};
			Process process = Runtime.getRuntime().exec(command,null,new File(resource.getLocation().toString()));//This prints on console. Need 2 capture in String somehow?
			int result = process.waitFor();
			if ( result != 0 ){
	  		  log.info("Gradle Command to  list the task is not exit properly and the Result is : "+result);
	  		}
			//read the process output
			
			BufferedInputStream bufferedInputStream = new BufferedInputStream( process.getInputStream() );
			BufferedReader commandOutput= new BufferedReader( new InputStreamReader( bufferedInputStream ) );
			String line = null;
			ArrayList<String> words = new ArrayList<String>();
			while ( ( line = commandOutput.readLine() ) != null  ) {
				if(line.startsWith("To see all tasks"))
				{
					
				}
				else
					words.add(line);
                
		  }
			getGradleTasks(words);
			commandOutput.close();
		} catch(Exception e){
			 log.error("", e);
		}

	}
    
    /* Retrieve the Gradle Build List */
	private void getGradleTasks(ArrayList<String> words) {
		// TODO Auto-generated method stub
		List<String> gradleTask=new ArrayList<String>(); 
		List<String> omittedChractersList=new ArrayList<String>();
		String splitArray[],omittedArray[]= {"init","wrapper","buildEnvironment","components","dependencies","dependencyInsight","help","model","projects","properties","tasks"};;
		for(String line : words) 
	    {
	        if (line.contains("--"))
	        {
	        	
	        }
	        else
	        {
	        	if(line.contains("-"))
	        	{
	        	splitArray=line.split("-");
	            gradleTask.add(splitArray[0].trim());
	        	} 
	        }
	       
	     }
		omittedChractersList=Arrays.asList(omittedArray);
		gradleTask.removeAll(omittedChractersList);
		target=gradleTask.toArray(new String[gradleTask.size()]);
	}
	
	/* Showing the Message Dialog */
	public void messgeShow(String message,String messageDialog)
	{
		Shell shell=new Shell(SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);//added newSWT.APPLICATION_MODAL
		MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK| SWT.CANCEL);
		dialog.setText(message);
		dialog.setMessage(messageDialog);
		dialog.open(); 
	}
	
	//Check the Specified Folder Is exists or not in the Project
	public IFile folderIsExists(IResource resource, String folder,String file) throws CoreException {
		   
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(resource.getName());
		IFile getFile = null;
		if (project.exists()) {
			
	      IFolder projectFolder = project.getFolder(folder);
	      if (projectFolder.exists()) {
	    	  IResource[] members;
	    	  members = projectFolder.members();
	    	  getFile= projectFolder.getFile(file);
	      }
	      else
	    	  log.info("Folder"+folder+"is Not exists...");
	   }
	   
	   return getFile;
	   
	}
	
	/* Check file is exists or not in the project */
	public boolean fileExists(IResource resource,String file) throws CoreException {
		   
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(resource.getName());
		boolean status=false;
		//Check File Exists In Root Of project
		if (project.exists()) {
			
	      IFile fileIsExists=project.getFile(file);
	      if(fileIsExists.exists())
	      {		 
	    	  status=true;
		  }
    	  else //Search in The nested FolderStructure
    	  {
    		 IPath path= searchNestedStructure(project,file);
    		 if(path!=null)
    			 status=true;
    		 //log.info("File Name is : "+file+"Path is : "+path);
    	  }
		}
		return status;
		   
	}
	
	/* Get The Required file from the project by using the name of the file */
	protected IFile getFile(IResource resource,String fileName) throws CoreException {
		   
		IFile file = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		   IProject project = workspace.getRoot().getProject(resource.getName());
		    if (project.exists()) {
		         file=project.getFile(fileName);
		    }
		    else {
		    	
			}
		  return file;
		   
	}
	
	//Add ant Build pack Target  to list
	void setAntTargets(String result)
	{
		target[index]=result;
		index++;
	}
	
	//return the build Pack List of ant
	String[] getAntTarget()
	{
	 return target;
	}
	
		/* Read the File and Search the Given String */ 
	public String readStringInFile(IFile file,String serachString,String splitString) throws CoreException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		String readLine,partSplit = null;
		String parts[]=new String[10];
		try {		
			while ((readLine = reader.readLine()) != null) {
			
				if(readLine.trim().contains(serachString))
				{
					parts=readLine.split(splitString);
					List<String> arrayList = new ArrayList<String>(Arrays.asList(parts));
					partSplit=arrayList.get(1);
				}
				else
				{
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("",e);
		}
		return partSplit;
	}
	/* Read the Xml Document and invoke the findNodeType(NodeList) if it have a Child Nodes
	 * Then invoke the findAttributesAndValueOfNode(tempNode) to get the values and attributes of the secific xml Node */
	void validateXmlDocument(String fileNameToGive) throws SAXException, IOException, ParserConfigurationException
	{
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		File file = new File(fileNameToGive);
		log.info("Read the Xml File "+fileNameToGive);
		InputStream inputStream = new FileInputStream(file);
		Document document = builder.parse(inputStream);
		
		if (document.hasChildNodes()) {

			findNodeType(document.getChildNodes());

		}

	}
	
	/*  Find the Node is whether element node or child node and invoke the 
	*	findAttributesAndValueOfNode(tempNode) to get attributes and value of node */
	private void findNodeType(NodeList childNodes) {
		// TODO Auto-generated method stub
		
		 for (int count = 0; count < childNodes.getLength(); count++) {

				Node tempNode = childNodes.item(count);

				// make sure it's element node.
				if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
					if (tempNode.hasAttributes()) {						
						findAttributesAndValueOfNode(tempNode);
					}
				}

				if (tempNode.hasChildNodes()) {

					// loop again if has child nodes
					if(tempNode.hasAttributes())
					{
						///if(tempNode.getNodeName().toString().equals("target"))
						findAttributesAndValueOfNode(tempNode);
					}
					findNodeType(tempNode.getChildNodes());
	
				}
		 	}
		}
	//Find the Node value and attributes in the xml file
	void findAttributesAndValueOfNode(Node tempNode)
	{
		
		// get attributes names and values
			NamedNodeMap nodeMap = tempNode.getAttributes();

			for (int i = 0; i < nodeMap.getLength(); i++) {

				Node node = nodeMap.item(i);
				if(node.getNodeName().equals("sourceCompatibility"))
					version=node.getNodeValue().toString();
				if(node.getNodeName().equals("kind") && node.getNodeValue().equals("con") )
				{
					i++;
					node=nodeMap.item(i);
					if(node.getNodeName().equals("path") && node.getNodeValue().contains("JavaSE"))
					{
						version=node.getNodeValue().toString();
					}
				
				}
			}
			if(tempNode.getNodeName().equals("target"))
			{
				
				for (int i = 0; i < nodeMap.getLength(); i++) {

					Node node = nodeMap.item(i);
					if(node.getNodeName().equals("name"))
					{
					antTarget.add(node.getNodeValue().toString());
					}				
				}
			}
			if(tempNode.getNodeName().equals("javac"))
				for (int i = 0; i < nodeMap.getLength(); i++) {
					Node node = nodeMap.item(i);
					if(node.getNodeName().equals("source"))
						version=node.getNodeValue();
					 if(node.getNodeName().equals("target"))
						version=node.getNodeValue();
				}
		

	}
	//find the Path of file in the Nested Folder Structure
	IPath searchNestedStructure(IContainer container, String file) throws CoreException {
		IPath path = null;
		for(IResource member : container.members()) {
			if(member.getName().toString().equals(file))
			{
			 path=member.getFullPath();
				return path;
			}
			if (member instanceof IContainer) {
            searchNestedStructure((IContainer)member,file);
			}
		}
		return path;
	}
	
	//Find the version in Project .classPath file.
	//If classpath doesn't contain the version of java then searches the build.xml or build.gradle file in case of ant or gradle. 
	public String isClasspathExists(IResource resource, String pack) throws CoreException, SAXException, IOException, ParserConfigurationException {
		// TODO Auto-generated method stub
		resultString=null;
		version=null;
		log.info("Validation of Class path ");
		IFile file=getFile(resource,".classpath");
		String fileNameToGive=file.getLocation().toString();
		 
		 if(file.exists())
			{
			validateXmlDocument(fileNameToGive);
			if(version!=null)
				if(version.contains("JavaSE"))
				{
					String splitArray[]=version.split("JavaSE-");
					resultString=splitArray[1];
				}
			
			}
			
		if(resultString==null)
		{
			//If the Project Builder is gradle or ant, the xml file have the version of java 
			
			if(pack.equals("Gradle"))
			{
				IFile filePath=getFile(resource, "build.gradle");
				version=null;
				fileNameToGive=filePath.getLocation().toString();
				resultString=readStringInFile(filePath,"sourceCompatibility","=");
				if(resultString!=null)
				{
					resultString=resultString.replaceAll("(?i)^[a-z]+|[=\"]+|[a-z]+$","");
					if(resultString.equals(" "))
						resultString=null;
					log.info("Java version : "+resultString);
				}
				else
					return null;
				
			}
			if(pack.equals("Ant"))
			{
				IFile filePath=getFile(resource, "build.xml");
				fileNameToGive=filePath.getLocation().toString();
				version=null;
				validateXmlDocument(fileNameToGive);
				resultString=version;
			}
		}
		if(resultString!=null)
		{
		resultString="java:"+resultString;
		resultString=resultString.replaceAll("\\s+","");
		writeConfig("##PLATFORM", resultString);
		}
		log.info("Platform Version returned as : "+resultString);
		return resultString;
	}

}
