package com.gopaddle.singlesignon;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gopaddle.jsonhandler.ReadTestCase;
import com.gopaddle.jsonhandler.ResponseBody;
import com.gopaddle.jsonhandler.RestCaller;
import com.gopaddle.utils.ConfigReader;
import com.gopaddle.utils.Readjson;


public class TokenAccess {
	
	String responseStr = null;
	int statuscode;
	RestCaller rc = new RestCaller();
	ResponseBody responseBody;
	String code,token,handle;
	String payloadPath;
	String accountId = "",configPath;
	int nodeCount,diskSize;
	ReadTestCase RTC = new ReadTestCase();
	ConfigReader con = ConfigReader.getConfig();
	JSONObject js;
	JSONArray kubeId_temp;
	
	Logger log=Logger.getLogger(TokenAccess.class);
	private String endPoint;
	
	public TokenAccess() throws JSONException{
		
		payloadPath=con.Get("##ROOTPATH")+"/payload/";
		
		log.info("Payload path is"+payloadPath);
		configPath=con.Get("##ROOTPATH")+"config";
		String jsonFile = Readjson.readFileAsString(configPath);
		js = new JSONObject(jsonFile);
		endPoint = js.getString("url");
	}
	//get The Auth Token
	public void getToken(){
		code=con.Get("##AUTHCODE");
		String apiUrl = endPoint+"/api/auth/google";
		log.info("api Url for getToken : "+apiUrl);
		payloadPath=payloadPath+"authorization/tokenAccess.json";
		String jsonFile = Readjson.readFileAsString(payloadPath);
		try {
				RTC.dataparser(jsonFile);
				RTC.setInput(RTC.getInput().replaceAll("##AUTHCODE",code));
				responseBody = rc.httpPost(apiUrl, RTC.getInput(), "JSON");
				
				JSONObject resp = new JSONObject(responseBody.getResponseBody());
				token= resp.getString("token");
				handle=resp.getString("handle");
				if(handle.equals(""))
				{
				}
				else
					con.Put("##HANDLE",handle);
				log.info("Handle : "+handle);
				//add headers
				con.Put("##CONTENTTYPE", "Authorization");
				con.Put("##VALUE", "Bearer "+token);
		}catch (Exception e) {
			log.error("Error in Token access.Reason :"+getReasonForFail(responseBody),e);
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
}
