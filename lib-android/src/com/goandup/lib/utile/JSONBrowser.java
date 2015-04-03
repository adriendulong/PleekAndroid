package com.goandup.lib.utile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * 
 * @author nicolas
 *
 */
public class JSONBrowser
{
	private JSONObject json;
	
	public JSONBrowser(JSONObject json)
	{
		this.json = json;
	}
	
	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
	

	public String getS(String key)
	{
		return getOfJsonS(key, json);
	}
	
	public Integer getI(String key)
	{
		return getOfJsonI(key, json);
	}
	
	public Float getF(String key)
	{
		return getOfJsonF(key, json);
	}
	
	public Boolean getB(String key)
	{
		return getOfJsonB(key, json);
	}
	
	public Date getD(String key)
	{
		return getOfJsonD(key, json);
	}
	
	public JSONArray getA(String key)
	{
		return getOfJsonA(key, json);
	}
	
	public JSONBrowser getO(String key)
	{
		return new JSONBrowser(getOfJsonO(key, json));
	}
	

	
	//static methode
	public static String getOfJsonS(String key, JSONObject json)
	{
		return (String) getOfJson(key, json, String.class, "");
	}
	
	public static int getOfJsonI(String key, JSONObject json)
	{
		return (Integer) getOfJson(key, json, Integer.class, Integer.valueOf(0));
	}
	
	public static float getOfJsonF(String key, JSONObject json)
	{
		return ((Double)getOfJson(key, json, Double.class, Double.valueOf(0f))).floatValue();
	}
	
	public static boolean getOfJsonB(String key, JSONObject json)
	{
		return (Boolean) getOfJson(key, json, Boolean.class, Boolean.valueOf(false));
	}
	
	public static Date getOfJsonD(String key, JSONObject json)
	{
		String date = getOfJsonS(key, json);

		L.e(">>>>> date = "+date);
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.US);
	    if (date.contains("T")) date = date.replace('T', ' ');
	    if (date.contains("Z")) date = date.replace("Z", "+0000");
	    else if (date.contains(":")) date = date.substring(0, date.lastIndexOf(':')) + date.substring(date.lastIndexOf(':') + 1);
	    
	    try {
	        return fmt.parse(date);
	    }
	    catch (ParseException e) {
	        L.e("Could not parse datetime: " + date);
	    }
	    
        return new Date();
	}
	
	public static JSONArray getOfJsonA(String key, JSONObject json)
	{
		return (JSONArray) getOfJson(key, json, JSONArray.class, new JSONArray());
	}
	
	public static JSONObject getOfJsonO(String key, JSONObject json)
	{
		return (JSONObject) getOfJson(key, json, JSONObject.class, new JSONObject());
	}
	
	public static Object getOfJson(String key, JSONObject json, Class<?> clazz, Object defaultVal)
	{
		return json.containsKey(key) && clazz.isInstance(json.get(key)) ? clazz.cast(json.get(key)) : defaultVal;
	}
}
