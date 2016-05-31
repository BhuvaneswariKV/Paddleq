package com.gopaddle.design;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.gopaddle.jsonhandler.ReadTestCase;
import com.gopaddle.jsonhandler.ResponseBody;
import com.gopaddle.jsonhandler.RestCaller;
import com.gopaddle.utils.ConfigReader;
import com.gopaddle.utils.Readjson;

public class createComponentFromRepo {
	
	String jsonFile;
	String responseStr;
	int statuscode;
	RestCaller rc = new RestCaller();
	ResponseBody responseBody;
	ReadTestCase RTC=new ReadTestCase();
	String endPoint;
	String payloadPath,configPath;
	String bpId,appId,compId,buildPackage,builder,port,startScript;
	int length;
	int responseCode;
	ConfigReader con = ConfigReader.getConfig();
	Logger log=Logger.getLogger(createComponentFromRepo.class);
	private String handle;
	public static JSONObject js;
	static boolean status=false;
	
	/* Create the component by already created git url*/
	public boolean createComponentFromExistingRepository() {
		
		String buildAppendCmd = null;
		try {
			String platform=con.Get("##PLATFORM");
			String repoPath=con.Get("##REPOPATH").trim();
			builder=con.Get("##BUILDER");
			
			if(builder.equals("Maven"))
				buildAppendCmd="mvn";
			else if(builder.equals("Gradle"))
				buildAppendCmd="gradle";
			else if(builder.equals("Ant"))
				buildAppendCmd="ant";
			else
				throw new IllegalStateException("Builder received as Empty");
		
			buildPackage=buildAppendCmd+" "+con.Get("##BUILDPACKAGE");
			port=con.Get("##PORT");
			startScript=con.Get("##STARTSCRIPT");
			String apiUrl = endPoint + "/api/" + handle+"/design/"+bpId+"/component";
			jsonFile = Readjson.readFileAsString(payloadPath+
				"design/createComponentFromExistingRepo.json");
			RTC.dataparser(jsonFile);
			RTC.setInput(RTC.getInput().replaceAll("##PLATFORM", platform));
			RTC.setInput(RTC.getInput().replaceAll("##REPOPATH", repoPath));
			RTC.setInput(RTC.getInput().replaceAll("##BUILDPACKAGE",buildPackage));
			RTC.setInput(RTC.getInput().replaceAll("##BUILDER", builder));
			if(startScript==null)
				startScript="";
			RTC.setInput(RTC.getInput().replaceAll("##STARTSCRIPT", startScript));
			RTC.setInput(RTC.getInput().replaceAll("##PORT", port));
			log.info("Component creation Input:"+RTC.getInput().toString());
			
			responseBody = rc.httpPost(apiUrl, RTC.getInput(), "JSON");
			int statusCompare = responseBody.getResponseCode() == 200
					|| responseBody.getResponseCode() == 202 ? 0 : 1;
			if (statusCompare == 0) {
				log.info("Component creation get Success");
				status=true;
				linkComponent();
				log.info("Design Creation Completed...Now start Launch on Kuberenetes Cluster...");
			}
			else
			{
				log.info("component Creation failed reason : "+getReasonForFail(responseBody));
				status=false;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error("Error in Component ",e1);
		}
		
		 return status;	
	}
	
	//Get Project Root Url,server Name or ip ,Handle,designId
	public createComponentFromRepo() throws JSONException {
		
		// TODO Auto-generated constructor stub
		try
		{
			payloadPath=con.Get("##ROOTPATH")+"payload/";
			configPath=con.Get("##ROOTPATH")+"config";
			log.info("Initialize component creation");
			String jsonFile = Readjson.readFileAsString(configPath);
			js = new JSONObject(jsonFile);
			endPoint = js.getString("url");
			bpId=con.Get("##BPID");
			handle=con.Get("##HANDLE");
		
		}catch(JSONException e)
		{
			
			log.error("Component Repo Instantiation",e);
		}
		catch(Exception e)
		{
			log.error("Component Repo Instantiation",e);
		}
	}
	
	//Retrieve the fail reason from the Response
	private String getReasonForFail(ResponseBody responseBody) {
		// TODO Auto-generated method stub
		log.info("reson : "+responseBody.getResponseBody());
		JSONObject resp = new JSONObject(responseBody.getResponseBody().toString());
		boolean status = (boolean)resp.get("success");
		String reason = null;
		if(status==false)
			reason=resp.get("reason").toString();
		return reason;
	}
	
	//Link the Component In the Design
	public void linkComponent()
	{
		try {
			log.info("component Linking....");
			String apiUrl =  endPoint + "/api/" + handle+"/design/"+bpId;
			jsonFile = Readjson.readFileAsString(payloadPath
				+ "design/linkComponent.json");
			RTC.dataparser(jsonFile);
			responseBody = rc.httpPut(apiUrl, RTC.getInput());
					
			int statusCompare = responseBody.getResponseCode() == 200
					|| responseBody.getResponseCode() == 202 ? 0 : 1;
			if (statusCompare == 0) {
				log.info("Link component get success");
				status=true;
				//Publish the design
				publish();
			}
			else
			{
				status=false;
			
				log.info("component linking get failed"+"\n Reason :"+getReasonForFail(responseBody));
			}
			} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Component Linking Error",e);
		}
		
	}
	//Publish the Design
	public void publish() {
		try{
			log.info("Publish design ....");
			String apiUrl = endPoint + "/api/" + handle+"/design/"+bpId;
			jsonFile = Readjson.readFileAsString(payloadPath
					+ "design/publish.json");
			RTC.dataparser(jsonFile);
			responseBody = rc.httpPut(apiUrl, RTC.getInput());
			int statusCompare = responseBody.getResponseCode() == 200
						|| responseBody.getResponseCode() == 202 ? 0 : 1;
			if (statusCompare == 0) {
				log.info("Publishing design get success...");
			}
			else
			{
				status=false;
				log.error("publish Design get not success\n Reason:"+getReasonForFail(responseBody));
			}
				
		}catch(Exception e)
		{
			log.error("Publish Design :",e);
		}

	}


}
