package com.gopaddle.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.json.JSONException;

import com.gopaddle.application.KubernetesCluster;
import com.gopaddle.design.InitiateLaunchingConfig;
import com.gopaddle.utils.ConfigReader;
import com.launch.configload.ConfigurationManipulation;


public class DesignUI {

	ConfigReader con=ConfigReader.getConfig();
	Logger log=Logger.getLogger(DesignUI.class);
	KubernetesCluster kubernetesCluster;
	protected Shell shell;
	private Text text_DesignName;
	private Composite platformSpecificationComposite;
	private Label label_ConfigurationDetailHeading;
	private Label label_Platform;
	private Label label_Builder;
	private Label label_PlatformNotFoundNotify;
	private Label seperator;
	private Text text_Builder;
	private Label label_Build;
	private Combo combo_build;
	private Text text_AdditionalParamereters;
	private Composite composite_DeploymentDetails;
	private Label label_DeployementDetailsHeading;
	private Label label_KubeCluster;
	private Label label_StartScript;
	private Label label_ActualPort;
	private Combo combo_KubePlatformList;
	private Text text_StartScript;
	private Text text_Port;
	private Label label_seperatorComp2;
	private Label label_configurationDetails;
	private Label label_AlternatePort;
	private Text text_AlternatePort;
	
	String rootPath;
	Text text_Platform;
	static String kubeList[]=null;
	String builders[]=null,changePlatformVersion=null;
	String pack=null,designName;
	ConfigurationManipulation configurationManipulation=new ConfigurationManipulation();
	
	//Constructor received parameter from the GopaddleLaunchShort.class 
	public DesignUI(final boolean fileStatus,final String platformVersion,final String builder,final IResource resource){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				rootPath=con.Get("##ROOTPATH");
				open(platformVersion, fileStatus, builder,resource);		
			}
		});
	}

	public DesignUI() throws JSONException {
		// TODO Auto-generated constructor stub
		
	}	
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String platformVersion = null;
			boolean classPathExists = false;
			String pack = null;
			IResource resource=null;
			
			DesignUI window = new DesignUI();
			window.open(platformVersion,classPathExists,pack,resource);
		}catch (Exception e) {
			e.printStackTrace();
		 }
	}

	/**
	 * Open the window. and initiates the control
	 */
	public void open(String platformVersion,boolean classPathExists,String builderReceived,final IResource resource) {
		Display display = Display.getDefault();
		createContents();
		try{
			Image icon_window = new Image(display,rootPath+"icons/configurationIcon.jpg");
		    shell.setImage(icon_window);
			shell.setSize(400, 430);
			//It configures the window in Center of our screen
			Monitor primary = display.getPrimaryMonitor();
		    Rectangle bounds = primary.getBounds();
		    Rectangle rect = shell.getBounds();
	    
		    int x = bounds.x + (bounds.width - rect.width) / 2;
		    int y = bounds.y + (bounds.height - rect.height) / 2;
		    
			log.info("Kubernetes List Monitoring  object Initiates");
			IRunnableWithProgress op = new ProgressShown();
		
			try {
				log.info("Kube List fetching...");
				new ProgressMonitorDialog(shell).run(true, false, op);
			} catch (InvocationTargetException | InterruptedException e1) {
				// TODO Auto-generated catch block
				log.error(e1);
			}
		
			if(kubeList==null){
				log.info("Kubernetes List is Empty");
				shell.setVisible(false);
				new CreateKube(shell);
				kubeList=kubernetesCluster.getKubernetesClusterName();
			}
	
			if(kubeList.length==0 || kubeList==null)
			{
				log.info("Kubernetes cluster is not Existing..Please Create new Cluster.....");
				shell.setVisible(false);
				new CreateKube(shell);
				kubernetesCluster.getKubernetesClusterName();
			}
			shell.setVisible(true);	
			Composite composite = new Composite(shell, SWT.NONE);
			composite.setBounds(0, 0, 394, 53);
			
			Label label_designName = new Label(composite, SWT.NONE);
			label_designName.setBounds(10, 13, 100, 15);
			label_designName.setText("Design Name");
			
			text_DesignName = new Text(composite, SWT.BORDER);
			text_DesignName.setBounds(173, 10, 150, 21);
			text_DesignName.setText(con.Get("##RESOURCENAME"));
			
			seperator = new Label(composite, SWT.HORIZONTAL |SWT.SEPARATOR);
			seperator.setLocation(0, 42);
			seperator.setSize(398, 15);
			
			platformSpecificationComposite = new Composite(shell, SWT.NONE);
			platformSpecificationComposite.setBounds(0, 59, 394, 134);
			
			label_Platform = new Label(platformSpecificationComposite, SWT.NONE);
			label_Platform.setBounds(10, 21, 55, 15);
			label_Platform.setText("Platform");
		
			if(!classPathExists || platformVersion==null ){
				String availablePlatforms[]={"java:1.7","java:1.8","python:2.7","tomcat:7","nodejs:3"};
				label_PlatformNotFoundNotify = new Label(platformSpecificationComposite, SWT.WRAP);
				label_PlatformNotFoundNotify.setBounds(269, 21, 85, 15);
				label_PlatformNotFoundNotify.setText("Please Choose the Platform for Your App.");
				
				final Combo combo_Platform = new Combo(platformSpecificationComposite, SWT.NONE);
				combo_Platform.setBounds(173, 21, 91, 23);
				combo_Platform.setItems(availablePlatforms);
				combo_Platform.select(0);
				
				changePlatformVersion=combo_Platform.getText().toString();
				
				combo_Platform.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						 changePlatformVersion=combo_Platform.getText().toString();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
			else
	    	{
	    		text_Platform = new Text(platformSpecificationComposite, SWT.BORDER| SWT.WRAP|SWT.SINGLE);
			    text_Platform.setBounds(173, 21, 91, 23);
			    text_Platform.setText(con.Get("##PLATFORM"));
			    platformVersion=con.Get("##PLATFORM");
			    changePlatformVersion=con.Get("##PLATFORM");
			}
					
			label_Builder = new Label(platformSpecificationComposite, SWT.NONE);
			label_Builder.setBounds(10, 56, 55, 15);
			label_Builder.setText("Builder");
			
			text_Builder = new Text(platformSpecificationComposite, SWT.BORDER);
			text_Builder.setText(builderReceived);
			text_Builder.setBounds(173, 56, 91, 21);
							
			label_Build = new Label(platformSpecificationComposite, SWT.NONE);
			label_Build.setBounds(10, 91, 55, 15);
			label_Build.setText("Build");
			
			combo_build = new Combo(platformSpecificationComposite, SWT.BORDER);
			combo_build.setBounds(173, 84, 91, 23);
	 
			if(builderReceived.equals("Gradle")){			
				combo_build.setItems(configurationManipulation.target);
			    combo_build.select(0); 	 
			}
			else if(builderReceived.equals("Ant")){			
				combo_build.setItems(configurationManipulation.target);
			    combo_build.select(0);		    
			}
			else if(builderReceived.equals("Maven")){
				String target[]={"package","install","deploy","integration-test"};
				combo_build.setItems(target);
			    combo_build.select(0);
			    
			}
			
			text_AdditionalParamereters = new Text(platformSpecificationComposite, SWT.BORDER | SWT.WRAP);
			text_AdditionalParamereters.selectAll();
			text_AdditionalParamereters.setBounds(173, 117, 97, 21);
			
			final Combo combo_BuilderList = new Combo(platformSpecificationComposite, SWT.NONE);
			combo_BuilderList.setBounds(267, 53, 91, 23);
			combo_BuilderList.setVisible(false);
			
			Label label_AdditionalParameters = new Label(platformSpecificationComposite, SWT.NONE);
			label_AdditionalParameters.setBounds(10, 117, 157, 15);
			label_AdditionalParameters.setText("Additional Build Parameters");
			
			label_configurationDetails = new Label(platformSpecificationComposite, SWT.NONE);
			label_configurationDetails.setBounds(124, 0, 146, 15);
			label_configurationDetails.setText("Configuration Details");
			label_configurationDetails.setFont(new Font(display, "Consolas", 10, SWT.BOLD));
			
			builders=configurationManipulation.builders.toArray(new String[configurationManipulation.builders.size()]);
			
			if(builders.length>1)
			{
				combo_BuilderList.setItems(builders);
				combo_BuilderList.select(0);
				combo_BuilderList.setVisible(true);
				combo_BuilderList.addSelectionListener(new SelectionListener() {
				String builder;	
				//Get the changed selection when combo get changed 
					@Override
					public void widgetSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						builder=combo_BuilderList.getItem(combo_BuilderList.getSelectionIndex());
						try {
							configurationManipulation.getBuilderList(resource,builder);
						} catch (CoreException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							//log.error("Updating basic configuration:", e1);
							
						}
						
						combo_build.setItems(configurationManipulation.target);
						text_Builder.setText(builder);
						combo_build.select(0);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
				
			composite_DeploymentDetails = new Composite(shell, SWT.NONE);
			composite_DeploymentDetails.setBounds(0, 204, 394, 157);
			
			label_DeployementDetailsHeading = new Label(composite_DeploymentDetails, SWT.NONE);
			label_DeployementDetailsHeading.setBounds(129, 10, 184, 15);
			label_DeployementDetailsHeading.setFont(new Font(display, "Consolas", 10, SWT.BOLD));
			label_DeployementDetailsHeading.setText("Deployement Details");
			
			label_KubeCluster = new Label(composite_DeploymentDetails, SWT.NONE);
			label_KubeCluster.setBounds(10, 39, 111, 15);
			label_KubeCluster.setText("Kubernetes Cluster");
			
			label_StartScript = new Label(composite_DeploymentDetails, SWT.NONE);
			label_StartScript.setBounds(10, 69, 91, 15);
			label_StartScript.setText("Start Script");
			
			label_ActualPort = new Label(composite_DeploymentDetails, SWT.NONE);
			label_ActualPort.setBounds(10, 99, 76, 15);
			label_ActualPort.setText("Actual Port");
			
			combo_KubePlatformList = new Combo(composite_DeploymentDetails, SWT.NONE);
			combo_KubePlatformList.setBounds(173, 31, 168, 23);
			combo_KubePlatformList.setItems(kubeList);
			combo_KubePlatformList.select(0);
			
			text_StartScript = new Text(composite_DeploymentDetails, SWT.BORDER);
			text_StartScript.setBounds(173, 62, 168, 21);
			
			text_Port = new Text(composite_DeploymentDetails, SWT.BORDER);
			text_Port.setBounds(173, 90, 76, 21);
			
			label_AlternatePort = new Label(composite_DeploymentDetails, SWT.NONE);
			label_AlternatePort.setBounds(10, 130, 76, 15);
			label_AlternatePort.setText("Alternate Port");
			
			text_AlternatePort = new Text(composite_DeploymentDetails, SWT.BORDER);
			text_AlternatePort.setBounds(173, 120, 76, 21);
			
			Button buttton_Cancel = new Button(shell, SWT.NONE);
			buttton_Cancel.setBounds(163, 367, 75, 25);
			buttton_Cancel.setText("Cancel");
			
			Button button_Ok = new Button(shell, SWT.NONE);
			button_Ok.setBounds(266, 367, 75, 25);
			button_Ok.setText("OK");
			
			label_ConfigurationDetailHeading = new Label(shell, SWT.NONE);
			label_configurationDetails.setBounds(124, 0, 146, 15);
			label_ConfigurationDetailHeading.setFont(new Font(display, "Consolas", 10, SWT.BOLD));
			label_ConfigurationDetailHeading.setText("Configuration Details");
			
			label_seperatorComp2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
			label_seperatorComp2.setBounds(-12, 190, 429, 21);
			label_seperatorComp2.setText("New Label");
			//It validates the field and give input for launch Config
			button_Ok.addSelectionListener(new SelectionListener() {
			
				@Override
				public void widgetSelected(SelectionEvent e) {
					int max=1000;
					int min=0;
					// TODO Auto-generated method stub
					Random rand = new Random();
					int randomNum = rand.nextInt((max - min) + 1) + min;
					designName=text_DesignName.getText().toLowerCase().toString();
					log.info("Design Name : "+designName);
					designName=designName+"-"+randomNum;
					con.Put("##DESIGNNAME", designName);
					con.Put("##APPNAME","app-"+designName);
					log.info("Application Name : "+"app-"+designName);
					boolean validDataRecieved=true;
					
					String buildCommand=combo_build.getItem(combo_build.getSelectionIndex());
					if(text_AdditionalParamereters.getText().isEmpty() | text_AdditionalParamereters.getText().equals("")){
						
					}
					else
						buildCommand=" "+text_AdditionalParamereters.getText();
					con.Put("##BUILDPACKAGE",combo_build.getItem(combo_build.getSelectionIndex()) );
					String errorMessage=null;
					if(text_Platform!=null)
					{
						changePlatformVersion=text_Platform.getText().toString().trim();
						if(changePlatformVersion.equals(""))
						{
							errorMessage="Please Specify a Platform Version";
	
						}
						else if(changePlatformVersion!=null)
						{
							log.info("Platform is : "+changePlatformVersion);
							con.Put("##PLATFORM", changePlatformVersion);
						}					
					}
					else
					{
						con.Put("##PLATFORM", changePlatformVersion);
					}
					if(text_Port.getText().toString().matches("^.*[,|0-9].*$"))					
						con.Put("##PORT",text_Port.getText().toString());				
					else
					{
						errorMessage="Please enter Valid port Number";
					}
					if(text_StartScript.getText().equals("")| text_StartScript.getText().isEmpty())
					{
						
						if(changePlatformVersion.startsWith("tomcat"))
						{
							
						}
						else
							errorMessage="Please enter Start script command";
					}
					else
						con.Put("##STARTSCRIPT",text_StartScript.getText().toString());
					if(text_AlternatePort.getText().toString().matches("^.*[,|0-9].*$"))
					{
						int value=Integer.parseInt(text_AlternatePort.getText());
						if (30000 <= value && value < 32500)	
							con.Put("##ALTERNATEPORT",text_AlternatePort.getText().toString());
					}
					else
						errorMessage="please enter Valid port Number in Range(30000-32500)";
					if(errorMessage!=null)
					{
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
					    messageBox.setMessage(errorMessage);
					    messageBox.open();
					    validDataRecieved=false;
	
					}
				try
				{
					if(validDataRecieved)
					{					
						shell.setVisible(false);
						//Invoking the Design Creation by the another Thread
						Thread thread = new Thread(new InitiateLaunchingConfig(combo_KubePlatformList.getItem(combo_KubePlatformList.getSelectionIndex()).toString()));
						thread.start();
					 }
					
				}catch(Exception e2){
					log.error("Error in Launch",e2);
				}
			}
				
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
					
			}
				
		});
		//cancel the window
		buttton_Cancel.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					log.info("Shell get closed...");
					shell.close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
		});
		
			shell.open();
			shell.setFocus();
			shell.layout();
		}catch(Exception e)
		{
			log.error(e);
		}
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.ON_TOP);
		shell.setSize(458, 430);
		shell.setText("goPaddle Design Configuration");
		shell.setFocus();
	}
	
	//Monitoring for KubeList request
	public class ProgressShown implements IRunnableWithProgress
	{
		ProgressShown(){}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
			// TODO Auto-generated method stub
			monitor.beginTask("Receiving Kube List....",IProgressMonitor.UNKNOWN);
			try
			{
				kubernetesCluster=new KubernetesCluster();
				kubeList=kubernetesCluster.getKubernetesClusterName();
			}catch(Exception e)
			{
				log.error(" kubernetes list is not existing..."+kubeList.length,e);
			}
			monitor.worked(10);
	        monitor.done();
	        log.info("Kubernetes cluster Monitoring completed");	
	        if(monitor.isCanceled())
	        {
	        	monitor.done();
	        	return;
            }
		}
	}
}
