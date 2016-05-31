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

public class CreateDesign {	
		String jsonFile;
		String responseStr;
		int statuscode;
		RestCaller rc = new RestCaller();
		ResponseBody responseBody;
		ReadTestCase RTC=new ReadTestCase();
		String endPoint;
		String payloadPath,configPath;
		String handle = "";
		String designName,bpId,appId,compId;
		int length;
		int responseCode;
		boolean status=false;
		
		Logger log=Logger.getLogger(CreateDesign.class);
		ConfigReader con = ConfigReader.getConfig();
		public static JSONObject js;
		JSONArray result;
			
	//design Creation
	//get the handle,server url,and root of plugin
	public CreateDesign() throws JSONException
	{
		payloadPath=con.Get("##ROOTPATH")+"payload/";
		 configPath=con.Get("##ROOTPATH")+"config";
		 String jsonFile = Readjson.readFileAsString(configPath);
		 js = new JSONObject(jsonFile);
		 endPoint = js.getString("url");
		 handle=con.Get("##HANDLE");
	}
	//Creates the New design
	public boolean createDesign() {
		designName=con.Get("##DESIGNNAME");
		String apiUrl = endPoint + "/api/" + handle+"/design";
		jsonFile = Readjson.readFileAsString(payloadPath+"design/createDesign.json");
		try {
			RTC.dataparser(jsonFile);
			RTC.setInput(RTC.getInput().replaceAll("##DESIGNNAME", designName));
			responseBody = rc.httpPost(apiUrl, RTC.getInput(), "JSON");
			int statusCompare = responseBody.getResponseCode() == 200
					|| responseBody.getResponseCode() == 202 ? 0 : 1;
			if (statusCompare == 0) {
				JSONObject resp = new JSONObject(responseBody.getResponseBody());
				bpId=resp.getString("id");
				js.put("bpId", bpId);
				Readjson.stringWriteAsFile(js.toString(), "config");
				con.Put("##BPID", bpId);
				log.info("Design Created Success....");
				log.info("bp id is "+bpId);
			
				//Invoke Component Creation from here
				createComponentFromRepo createcomponentfromrepo= new createComponentFromRepo();
				log.info("Component create instantiate...");
				status=createcomponentfromrepo.createComponentFromExistingRepository();
			}
			else
			{
				log.error("Design Creation failed"+getReasonForFail(responseBody));
			
			}
		}catch (Exception e) {
			log.error("Design Creation failed...",e);
		
		}
		return status;
	}
	//get the Failed Reason from response 
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
	
	
	//List the designs
	public void  listDesign()
	{
		try{
		 // TODO Auto-generated catch block
			String apiUrl = endPoint + "/api/" + handle+"/design";
			responseBody = rc.httpGet(apiUrl, "Normal");
			responseCode=responseBody.getResponseCode();
			if(responseCode==200)
			log.info("Design list get success");
			else
				System.out.print("Design List get failed:"+getReasonForFail(responseBody));
		
		} catch (Exception e) {
			log.error("",e);
		}
	}
	

}
