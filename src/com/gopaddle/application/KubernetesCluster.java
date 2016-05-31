package com.gopaddle.application;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gopaddle.design.LaunchDesign;
import com.gopaddle.jsonhandler.ReadTestCase;
import com.gopaddle.jsonhandler.ResponseBody;
import com.gopaddle.jsonhandler.RestCaller;
import com.gopaddle.utils.ConfigReader;
import com.gopaddle.utils.Readjson;

public class KubernetesCluster {

	String jsonFile;
	String responseStr = null;
	int statuscode;
	RestCaller rc = new RestCaller();
	ResponseBody responseBody;
	String endPoint,handle;
	String payloadPath,configPath;
	String bpId,appId,compId,kubeId;
	String name,projectId,machineType,zone;
	int nodeCount,diskSize;
	int count;
	
	ReadTestCase RTC = new ReadTestCase();
	Map<String, String> header=new HashMap<String, String>();
	
	Logger log=Logger.getLogger(KubernetesCluster.class);
	ConfigReader con = ConfigReader.getConfig();
	
	JSONObject js;
	JSONArray jsonKubeList;
	
	//Read the Handle,Server Name or Ip,Root Path of Plugin
	 public KubernetesCluster() throws JSONException
	 {
		 log.info("Kubernetes Cluster Initiated...");
		 payloadPath=con.Get("##ROOTPATH")+"payload/";
		 
		 configPath=con.Get("##ROOTPATH")+"config";
		 String jsonFile = Readjson.readFileAsString(configPath);
		 js = new JSONObject(jsonFile);
		 endPoint = js.getString("url");
		 handle=con.Get("##HANDLE");
	}
	 
	// Get the List of Kubernetes cluster Name
	public String[] getKubernetesClusterName() {
		 
	 	String [] kubeList = null;
		String apiUrl = endPoint + "/api/" + handle+"/kube?force=true&cmd_line=true";
		try {
			responseBody = rc.httpGet(apiUrl, "Normal");
			JSONObject resp = new JSONObject(responseBody.getResponseBody());
			
			if(responseBody.getResponseCode()==200)
			{
				count = resp.getInt("count");
				jsonKubeList=resp.getJSONArray("result");
			}
			else
				log.info("Kubernetes cluster was not found : "+getReasonForFail(responseBody));
				
			if (count > 0) {				
				kubeList = new String [count];	
			    for (int i = 0; i < count; i++) {
			        kubeList[i] = String.valueOf(jsonKubeList.getJSONObject(i).getString("name"));
			        
			    }
			}
			}catch (Exception e) {
				log.error("Kubernetes Name List",e);
				
			}
			return kubeList;
	}
	
	//Get the kube id of the cluster
	public void getKubeId(String kubeName) throws JSONException
	 {
		 String apiUrl = endPoint + "/api/" + handle+"/kube?force=true&cmd_line=true";
		 try {
				responseBody = rc.httpGet(apiUrl, "Normal");
				JSONObject resp = new JSONObject(responseBody.getResponseBody());
		 
				count = resp.getInt("count");
				jsonKubeList=resp.getJSONArray("result");
				
				if(responseBody.getResponseCode()==200)
					log.info("Fetching Kube id get Success");
				else
					log.info("Sorry,Unable to Get the Id of the Kube...Reason : "+getReasonForFail(responseBody));
				for(int i=0;i<count;i++)
				{
					String value=String.valueOf(jsonKubeList.getJSONObject(i).getString("name"));
					if(kubeName.equals(value))
					{
						 kubeId=String.valueOf(jsonKubeList.getJSONObject(i).getString("id"));
						 con.Put("##KUBEID", kubeId);
						 con.Put("##KUBENAME", kubeName);
						 //Launch the design of the given kube id
						 LaunchDesign launchDesign=new LaunchDesign();
						 launchDesign.launchDesign();
					}
				}
		 }catch(Exception e){
			 log.error("fetching error in kube id", e);
		 }
	 }
	
	 /* Create the new kube cluster */
	 public void createKubernetesPlatform() {
		log.info("Creating Kubernetes Platform....");
		jsonFile = Readjson.readFileAsString("config");
		try
		{
			js = new JSONObject(jsonFile);
			
			String apiUrl = endPoint + "/api/" + handle+"/kube?operation=create";
			jsonFile = Readjson.readFileAsString(payloadPath
					+ "component/createComponentFromExistingRepo.json");
			try {
					
					jsonFile = Readjson.readFileAsString(payloadPath
							+ "kube/createKube.json");
					RTC.dataparser(jsonFile);
					name=con.Get("##NAME");
					projectId=con.Get("##PROJECTID");
					nodeCount=con.GetInt("##NODECOUNT");
					zone=con.Get("##ZONE");
					machineType=con.Get("##MACHINETYPES");
					diskSize=con.GetInt("##DISKSIZE");
					RTC.setInput(RTC.getInput().replaceAll("##KUBENAME",name));
					RTC.setInput(RTC.getInput().replaceAll("##KUBEPROJECTID",projectId));
					RTC.setInput(RTC.getInput().replaceAll("\"##KUBEDISKSIZE\"",String.valueOf(diskSize)));
					RTC.setInput(RTC.getInput().replaceAll("\"##KUBENODECOUNT\"",String.valueOf(nodeCount)));
					RTC.setInput(RTC.getInput().replaceAll("##KUBEZONE",zone));
					RTC.setInput(RTC.getInput().replaceAll("##KUBEMACHINETYPE",machineType));
					responseBody = rc.httpPost(apiUrl, RTC.getInput(), "JSON");
					if(responseBody.getResponseCode()==200)
						log.info("Kubernetes cluster Created success in your environment..."+responseBody.getResponseBody());
					else
						log.info("Kube Created fails"+getReasonForFail(responseBody));
					
			}catch(Exception e)
			{
				log.error("Create Kubernetes Platform : ", e);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	 }
	 //Retrieve the Reason for Fail from Response Body
	 private String getReasonForFail(ResponseBody responseBody) {
			// TODO Auto-generated method stub
			JSONObject resp = new JSONObject(responseBody.getResponseBody().toString());
			boolean status = (boolean)resp.get("success");
			String reason=resp.get("reason").toString();
			return reason;
		}
		
	
}



