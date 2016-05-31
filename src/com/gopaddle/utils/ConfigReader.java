package com.gopaddle.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigReader
{
	private static ConfigReader mInstance = null;
	private ConfigReader(){}
	private Map<String, String> map = new ConcurrentHashMap<String, String>();
	private Map<String, Integer> mapInt = new ConcurrentHashMap<String, Integer>();
	public Map<String, String> getMap(){
		return map;
	}
	public static ConfigReader getConfig()
	{
		if (mInstance == null)
		{
			mInstance = new ConfigReader();
		}
		return mInstance;
	}	
	public void Put(String key, String value)
	{
		map.put(key, value);			
	}
	public void Put(String key, int value)
	{
		mapInt.put(key,value );			
	}
	public String Get(String key)
	{
		return map.get(key);
		
	}
	public int GetInt(String key)
	{
		return mapInt.get(key);
		
	}

	@Override
	public String toString() {
		return "ConfigReader [map=" + map + "]";
	}	
}

