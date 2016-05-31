package com.gopaddle.jsonhandler;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadTestCase {

	private String input;
	private int intInput;
	private String expectedOutput;
	Logger log=Logger.getLogger(ReadTestCase.class);
	
	public ReadTestCase dataparser(String jsonFile)
	{
		try
		{
			 JSONObject js = new JSONObject(jsonFile);			
			 setInput(js.getJSONObject("input").toString());			
			 setExpectedOutput(js.getJSONObject("output").toString());
		}catch(JSONException je)
		{
			log.error("Error in JSON Parsing", je);
		}
		
		return null;		
	}
	public String getExpectedOutput() {
		return expectedOutput;
	}
	public void setExpectedOutput(String expectedOutput) {
		this.expectedOutput = expectedOutput;
	}
	public String getInput() {
		return input;
	}
	public int getInputInt() {
		return intInput;
	}
	public void setInput(String input) {
		this.input = input;
		
	}
	public void setInput(int input) {
		this.intInput = input;
	}
	
}
