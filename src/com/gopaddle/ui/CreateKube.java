package com.gopaddle.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.json.JSONException;

import com.gopaddle.application.KubernetesCluster;
import com.gopaddle.utils.ConfigReader;

public class CreateKube {

	protected static Shell shell;
	private Text text_Name;
	private Text text_DiskSize;
	private Text text_ProjectID;
	Logger log=Logger.getLogger(CreateKube.class);
	ConfigReader con=ConfigReader.getConfig();
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CreateKube window = new CreateKube();
			shell = new Shell();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CreateKube(final Shell parent){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				shell=new Shell(parent);
				shell.setFocus();
				open();
			}
		
		});
	
	}
	public CreateKube() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
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
		shell.setSize(398, 313);
		shell.setText("SWT Application");
		
		Label label_ConfigureKubernetesCluster = new Label(shell, SWT.NONE);
		label_ConfigureKubernetesCluster.setBounds(90, 10, 256, 15);
		label_ConfigureKubernetesCluster.setText("Configure Your Kube Environment For Launch");
		
		Composite composite_KubeConfig = new Composite(shell, SWT.NONE);
		composite_KubeConfig.setBounds(0, 47, 382, 185);
		
		Label label_Name = new Label(composite_KubeConfig, SWT.NONE);
		label_Name.setBounds(10, 13, 81, 15);
		label_Name.setText("Cluster Name");
		
		text_Name = new Text(composite_KubeConfig, SWT.BORDER);
		text_Name.setBounds(173, 10, 76, 21);
		
		Label label_DiskSize = new Label(composite_KubeConfig, SWT.NONE);
		label_DiskSize.setBounds(10, 69, 55, 15);
		label_DiskSize.setText("Disk Size");
		
		text_DiskSize = new Text(composite_KubeConfig, SWT.BORDER);
		text_DiskSize.setBounds(173, 66, 76, 21);
		
		Label label_InitialNodeCount = new Label(composite_KubeConfig, SWT.NONE);
		label_InitialNodeCount.setBounds(10, 96, 104, 15);
		label_InitialNodeCount.setText("Initial Node Count");
		
		final Spinner spinner_nodecount = new Spinner(composite_KubeConfig, SWT.BORDER);
		spinner_nodecount.setBounds(173, 93, 47, 22);
		spinner_nodecount.setMinimum(1);
		spinner_nodecount.setMaximum(5);
		
		String[] machineType={"n1-standard-1","n1-standard-2","n1-standard-4","n1-standard-8","n1-standard-16","n1-highmem-2","n1-highmem-4",
	    		"n1-highmem-8","n1-highmem-16","n1-highcpu-2","n1-highcpu-4","n1-highcpu-8","n1-highcpu-16",
	    		"f1-micro","g1-small"};
	    
	    String[] zone={"us-east1-b","us-east1-c","us-east1-d","us-central1-a","us-central1-b","us-central1-c","us-central1-f","europe-west1-b",
	    		"europe-west1-c","europe-west1-d","asia-east1-a","asia-east1-b","asia-east1-c"};
	    
		
		Label label_MachineType = new Label(composite_KubeConfig, SWT.NONE);
		label_MachineType.setBounds(10, 122, 81, 15);
		label_MachineType.setText("Machine Type");
		
		Label label_ProjectID = new Label(composite_KubeConfig, SWT.NONE);
		label_ProjectID.setBounds(10, 40, 55, 15);
		label_ProjectID.setText("ProjectId");
		
		text_ProjectID = new Text(composite_KubeConfig, SWT.BORDER);
		text_ProjectID.setBounds(173, 37, 76, 21);
		
		Label label_Zone = new Label(composite_KubeConfig, SWT.NONE);
		label_Zone.setBounds(10, 148, 55, 15);
		label_Zone.setText("Zone");
				
		final Combo combo_Zone = new Combo(composite_KubeConfig, SWT.NONE);
		combo_Zone.setBounds(173, 119, 91, 23);
		combo_Zone.setItems(zone);
		combo_Zone.select(0);
		
		final Combo combo_MachineType = new Combo(composite_KubeConfig, SWT.NONE);
		combo_MachineType.setBounds(173, 152, 91, 23);
		combo_MachineType.setItems(machineType);
		combo_MachineType.select(0);
		
		Button button_ok = new Button(shell, SWT.NONE);
		button_ok.setBounds(297, 250, 75, 25);
		button_ok.setText("OK");
		
		Button button_Cancel = new Button(shell, SWT.NONE);
		button_Cancel.setBounds(203, 250, 75, 25);
		button_Cancel.setText("Cancel");
		
		button_ok.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				KubernetesCluster kubernetesCluster;
				try {
					kubernetesCluster = new KubernetesCluster();
					con.Put("##ZONE", combo_Zone.getItem(combo_Zone.getSelectionIndex()));
					con.Put("##PROJECTID", text_ProjectID.getText().toString());
					con.Put("##NODECOUNT", spinner_nodecount.getSelection());
					con.Put("##MACHINETYPES",combo_MachineType.getItem(combo_MachineType.getSelectionIndex()));
					con.Put("##NAME",text_Name.getText().toString());
					con.Put("##DISKSIZE", Integer.parseInt(text_DiskSize.getText()));
					kubernetesCluster.createKubernetesPlatform();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					log.error("",e);
					e.printStackTrace();
				}
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		button_Cancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				shell.close();
				//System.exit(0);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
