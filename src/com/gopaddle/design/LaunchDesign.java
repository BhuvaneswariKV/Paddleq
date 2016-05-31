package com.gopaddle.design;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gopaddle.jsonhandler.ReadTestCase;
import com.gopaddle.jsonhandler.ResponseBody;
import com.gopaddle.jsonhandler.RestCaller;
import com.gopaddle.utils.ConfigReader;
import com.gopaddle.utils.Readjson;

public class LaunchDesign {
	
	String jsonFile;
	String responseStr;
	int statuscode;
	RestCaller rc = new RestCaller();
	ResponseBody responseBody;
	ReadTestCase RTC=new ReadTestCase();
	String endPoint,actualPort,alternatePort;
	String payloadPath,configPath;
	String bpId,appId,compId,kubeId,appName;
	int length;
	int responseCode;
	ConfigReader con = ConfigReader.getConfig();
	String handle;
	public static JSONObject js;

	Logger log=Logger.getLogger(LaunchDesign.class);
	private String reasonForFail;
	
	//get the Root path of Plugin,Server url,Handle and designId
	public LaunchDesign() throws JSONException
	{
		// TODO Auto-generated constructor stub
			 payloadPath=con.Get("##ROOTPATH")+"payload/";
			 configPath=con.Get("##ROOTPATH")+"config";
			 appName=con.Get("##APPNAME");
			 String jsonFile = Readjson.readFileAsString(configPath);
			 js = new JSONObject(jsonFile);
			 
			 endPoint = js.getString("url");
			 bpId=con.Get("##BPID");
			 handle=con.Get("##HANDLE");
			 log.info("Launching....");
	
	}
	//Launch the Design on The Kubernetes cluster
	public void launchDesign()  {
		
		try {
			kubeId=con.Get("##KUBEID");
			actualPort=con.Get("##PORT");
			alternatePort=con.Get("##ALTERNATEPORT");
			String apiUrl = endPoint + "/api/" + handle+"/app/"+bpId;
			jsonFile = Readjson.readFileAsString(payloadPath
				+ "design/launchDesign.json");
				RTC.dataparser(jsonFile);
				RTC.setInput(RTC.getInput().replaceAll("##KUBEID", kubeId));
				RTC.setInput(RTC.getInput().replaceAll("##PORT", actualPort));
				RTC.setInput(RTC.getInput().replaceAll("##APPNAME", appName));
				RTC.setInput(RTC.getInput().replaceAll("##ALTERNATEPORT",alternatePort));
				responseBody = rc.httpPost(apiUrl, RTC.getInput(), "JSON");
				int statusCompare = responseBody.getResponseCode() == 200
					|| responseBody.getResponseCode() == 202 ? 0 : 1;
				if (statusCompare == 0) {
					JSONObject resp = new JSONObject(responseBody.getResponseBody());
					boolean status = (boolean)resp.get("success");
					if(status){
						log.info("Design launch is initiated successfully....");
						//After launch get Succeeded get the status of the Launch
						getTransactionDetails(responseBody);
					}
					else
						log.info("Design Launch Initiation is get Fail Reason is :"+getReasonForFail(responseBody));
			}
			else
			{
				reasonForFail=getReasonForFail(responseBody);
				log.info("Design launch failed"+"input:"+RTC.getInput().toString()+"\n\nReason :"+reasonForFail);
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error("Error In Launching :",e1);
		}
	}
	
	private String getReasonForFail(ResponseBody responseBody2) {
		// TODO Auto-generated method stub
		JSONObject resp = new JSONObject(responseBody.getResponseBody());
		boolean status = (boolean)resp.get("success");
		String reason = null;
		if(status==false)
			reason = (String)resp.get("reason");
		return reason;
	}
	//Get the Launch Transaction Details after launch is initiated
	private void getTransactionDetails(ResponseBody responseBody) {
	// TODO Auto-generated method stub
		try{
			log.info("Invoking Transaction...");
			JSONObject resp = new JSONObject(responseBody.getResponseBody());
			String transId = resp.getString("tx_id");
			log.info("Transaction id for this launch is : "+ transId);
			String apiUrl = endPoint + "/api/" + handle+"/transaction/"+transId;
			String status = null;
			log.info("Launch operation to complete will take sometime....");
			while(true)
			{				
				responseBody = rc.httpGet(apiUrl, "Normal");
				int statusCompare = responseBody.getResponseCode() == 200
						|| responseBody.getResponseCode() == 202 ? 0 : 1;
				if (statusCompare == 0) {					
					resp = new JSONObject(responseBody.getResponseBody());
					status = resp.getJSONObject("result").getString("status");
					appId=resp.getJSONObject("result").getString("id");
					if(status.equals("Failed"))
						log.info("Launching was Fail.Reason is : "+resp.getJSONObject("result").getString("reason"));
					else
						log.info(responseBody.getResponseBody());
					Thread.sleep(10 * 1000 );
					if(status.equalsIgnoreCase("Running") ||status.isEmpty()){
						Thread.sleep(30 * 1000);
					
					}else{
						break;
					}
				}
				else
				{
					log.info("Getting Transaction detail is failed... Reason : "+getReasonForFail(responseBody));
					break;
				}
			}
			if(status!=null)
				checkApplicationState(status);
		}catch(Exception e)
		{
			log.error("Error in Fetching Transaction Detail", e);
		}

	}
	//Check the Status of the Application after get launched
	private void checkApplicationState(String status) {
			// TODO Auto-generated method stub
		JSONObject resp;
		if(status.equalsIgnoreCase("Success")){
			try{
				String apiUrl = endPoint + "/api/" + handle+"/app/"+appId+"?format=true";
				
				log.info("appId : "+appId);
				
				//waiting time for If application is in Scheduled State
				int max=5*60*1000;
				int currentTime=10*1000;
				Thread.sleep(10 * 1000 );
				while (currentTime<max){
					log.info("Application Status verifing : "+apiUrl);
					responseBody = rc.httpGet(apiUrl, "Normal");
					log.info("Received response Is : "+responseBody.getResponseBody());
					int statusCompare = responseBody.getResponseCode() == 200
							|| responseBody.getResponseCode() == 202 ? 0 : 1;
					if (statusCompare == 0) {					
						resp = new JSONObject(responseBody.getResponseBody());
						currentTime=currentTime+(30*1000);
						status = resp.getJSONObject("result").getString("status");
						Thread.sleep(10 * 1000 );
						if(status.equalsIgnoreCase("Scheduled")||
							status.isEmpty()){
						Thread.sleep(30 * 1000);
						
						}else{
							break;
						}
					}
					else{
						log.info("Failed To get Application Status : "+getReasonForFail(responseBody));	
						break;
					}

				}
				resp = new JSONObject(responseBody.getResponseBody());
				JSONArray jsonArray = resp.getJSONObject("result").getJSONArray("access_links");
				String kubeName = con.Get("##KUBENAME");
					if(status.equalsIgnoreCase("Success"))
					{
						//Print the Access Url
							for(int i=0; i<jsonArray.length();i++){
								String accessUrl = (String) jsonArray.get(i);
								log.info("Access Url: "+ accessUrl);
							}
							
							log.info("\nDesign got successfully launched on platform \"" +kubeName + 
							"\" by application name \"" +appName+"\".\nCheck from goPaddle portal's application tab for application details.\n" );
					}
					else if(status.equalsIgnoreCase("Scheduled")){
						log.info("\nDesign got launched on platform in scheduled state \"" +kubeName + 
							"\" by application name \"" +appName+"\".\nApplication in scheduled state...Please check the memory space in your Kubernetes...\n" );
					}
				
					else
						log.info("Getting Error in app Status Details.Reason is : "+getReasonForFail(responseBody));
			}
			catch(Exception e)
			{
				log.error("Error in Get Application Status ", e);
			}
		}
			
	}
	//Delete the Design By the Id of deign
	public void deleteDesign() {
		try
		{
		jsonFile = Readjson.readFileAsString(configPath);
		js = new JSONObject(jsonFile);
		log.info(" Id for delete design "+bpId);
		String apiUrl = endPoint + "/api/" +handle+"/design/"+bpId;
		try {
			responseBody=rc.httpDelete(apiUrl);
			int statusCompare = responseBody.getResponseCode() == 204
					|| responseBody.getResponseCode() == 500 ? 0 : 1;
			if(statusCompare==0)
				log.info(" Delete Design get passed");
			else
				log.info(" Delete Design get Fail:"+getReasonForFail(responseBody));
		}catch (Exception e) {
			log.error("Exception",e);
			}
		}catch(JSONException e)
		{
			log.error("Json",e);
		}
	} 
}
