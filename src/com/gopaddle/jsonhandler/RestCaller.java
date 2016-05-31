package com.gopaddle.jsonhandler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.gopaddle.utils.ConfigReader;
import com.gopaddle.utils.Readjson;


public class RestCaller {
	
	ConfigReader con=ConfigReader.getConfig();
	Logger log=Logger.getLogger(RestCaller.class);
	
	String responseStr,key=null,value=null;
	int statuscode;
	ResponseBody rb = new ResponseBody();
	
	public RestCaller(){
		key=con.Get("##CONTENTTYPE");
		value=con.Get("##VALUE");
	}
	//http POST request
	public ResponseBody httpPost(String endpoint, String payload, String format) {
		String responseStr = null;
		try {
			HttpClient client = new DefaultHttpClient();
			final HttpParams httpParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
		    client = new DefaultHttpClient(httpParams);		    
			String url = endpoint;
			HttpPost httppost = new HttpPost(url);
			if (format.equalsIgnoreCase("JSON")) {
				httppost.addHeader("Content-Type", "application/json");
				StringEntity setcontent = new StringEntity(payload);
				setcontent.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json"));
				httppost.setEntity(setcontent);
			}
			if(key!=null && value!=null)
			httppost.setHeader(key,value);
			HttpResponse response = client.execute(httppost);
			statuscode = response.getStatusLine().getStatusCode();
			InputStream in = response.getEntity().getContent();
			responseStr = Readjson.readInputStreamAsString(in);
			rb.setResponseCode(statuscode);
			rb.setResponseBody(responseStr);

		} catch (ClientProtocolException cp) {
				log.error("Connection failed ",cp);
		} catch (IOException io) {
			log.error("",io);
		} 
		return rb;

	}

	//http  GET request
	public ResponseBody httpGet(String endpoint, String format){
		try {
			HttpClient client = new DefaultHttpClient();
			String url = endpoint;
			HttpGet httpget = new HttpGet(url);
			HttpResponse response;
			if(key!=null && value!=null)
				httpget.setHeader(key, value);
			int statuscode = 0;
			JSONObject rB;
			String status;
			Thread.sleep(15000);
			if(format.equalsIgnoreCase("status"))
			{
				  do{
					  response=client.execute(httpget);
					  statuscode=response.getStatusLine().getStatusCode();
					  InputStream in = response.getEntity().getContent();
					  responseStr = Readjson.readInputStreamAsString(in);
					  rB = new JSONObject(responseStr);
					  rB = rB.getJSONObject("result");
					  status = rB.getString("status");
					  if(status.equalsIgnoreCase("running") || status.equalsIgnoreCase("true"))
						  Thread.sleep(30000);
				   }while(status.equalsIgnoreCase("running") || status.equalsIgnoreCase("true"));
			}else
			{
				response = client.execute(httpget);
				statuscode = response.getStatusLine().getStatusCode();
				InputStream in = response.getEntity().getContent();
				responseStr = Readjson.readInputStreamAsString(in);
			}
			rb.setResponseCode(statuscode);
			rb.setResponseBody(responseStr);
			return rb;
		} catch (ClientProtocolException cp) {
			
			log.error("Connection failed : ",cp);
		}catch(InterruptedException ie)
		{
			log.error("Error in Slepping: ",ie);
			
		}
		catch (IOException ioe) {
			log.error("Invalid Data : " ,ioe);
			
		} catch (JSONException ex) {
			log.error("Error in Json object handling: " ,ex);
		}
		rb.setResponseCode(500);
		rb.setResponseBody("Internal Error");
		return rb;
	}
	
	//code for PUT Command
	public ResponseBody httpPut(String aPI, String body)
	{
		String errorMsg = null;
		try
		{	
			HttpClient client=new DefaultHttpClient();
			String url=aPI;
			org.apache.http.client.methods.HttpPut httpput = new HttpPut(url);
			httpput.addHeader("Content-Type", "application/json");
			StringEntity setcontent = new StringEntity(body);
			setcontent.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpput.setEntity(setcontent);
			if(key!=null && value!=null)
				httpput.setHeader(key, value);
			HttpResponse response=client.execute(httpput);
			int statuscode=response.getStatusLine().getStatusCode();
			InputStream in = response.getEntity().getContent();
			String responseStr = Readjson.readInputStreamAsString(in);
			rb.setResponseCode(statuscode);
			rb.setResponseBody(responseStr);
			return rb;
		}
		catch(ClientProtocolException cp)
		{
			log.error("Connection failed : " ,cp);
		}
		catch(IOException ioe)
		{
			 log.error("Invalid Data : " ,ioe);
		}
		catch(Exception ex)
		{
			log.error("Exception",ex);
		}
		rb.setResponseCode(500);
		rb.setResponseBody("Internal Error : " + errorMsg);
		return rb;
	}
	public ResponseBody httpDelete(String aPI)
	{
		  try
		  {
		   HttpClient client=new DefaultHttpClient();
		   String url=aPI;
		   HttpDelete httpdelete = new HttpDelete(url);
		   if(key!=null && value!=null)
		   httpdelete.setHeader(key, value);
		   HttpResponse response=client.execute(httpdelete);
		   int statuscode=response.getStatusLine().getStatusCode();
		   InputStream in = response.getEntity().getContent();
		   responseStr = Readjson.readInputStreamAsString(in);
		   rb.setResponseCode(statuscode);
		   rb.setResponseBody(responseStr);
		   return rb;
		  }
		  catch(ClientProtocolException cp)
		  {
			  log.error("Connection failed : " + cp.getLocalizedMessage());
		  }
		  catch(IOException ioe)
		  {
			  log.error("Invalid Data : " + ioe.getLocalizedMessage());
		  }
		  catch(Exception ex)
		  {
			  log.error(ex.getLocalizedMessage());
		  }
		 rb.setResponseCode(500);
		 rb.setResponseBody("Internal Error");
		return rb;		
	}
}
