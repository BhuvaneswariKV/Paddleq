package com.gopaddle.design;

import org.apache.log4j.Logger;

import com.gopaddle.application.KubernetesCluster;
import com.gopaddle.utils.ConfigReader;

public class InitiateLaunchingConfig implements Runnable{
	ConfigReader con=ConfigReader.getConfig();
	KubernetesCluster kubernetesCluster=new KubernetesCluster();
	Logger log=Logger.getLogger(InitiateLaunchingConfig.class);
	String selectedCluster;
	public InitiateLaunchingConfig(String selectedItem) {
		// TODO Auto-generated constructor stub
		selectedCluster=selectedItem;
	}
	
	@Override
	public void run() {
		//It is invoked from SWT UI.It initiates the Design creation and Launch 
		log.info("Design Creation is Invoking");
		CreateDesign create=new CreateDesign();
		boolean status=create.createDesign();
		if(status)
		{
			log.info("Fetching Kube Id....");
			kubernetesCluster.getKubeId(selectedCluster);
		}
		else
			log.info("Design Creation is failed");
	}

}
