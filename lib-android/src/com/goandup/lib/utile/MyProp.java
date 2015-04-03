package com.goandup.lib.utile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import android.content.Context;

public class MyProp
{
	private final static String FILE_NAME = "conf.properties";
	
	private static MyProp me;
	private Properties prop;
	private static Context context;
	
	/**
	 * Vaut mieux utiliser avec le contexte
	 *
	 * @deprecated use {@link getInstance(Context context)} instead.  
	 */
	@Deprecated
	public static MyProp getInstance(){
		return getInstance(null);
	}
	
	public static MyProp getInstance(Context context)
	{
		MyProp.context = context;
		if(me == null) me = new MyProp();
		return me;
	}
	
	public boolean set(String name, String data)//no context
	{
		return set(name, data, context);
	}
	
	public static boolean set(String name, String data, Context context)
	{
		return Utile.writeFile(name, data, context);
	}

	public boolean set(String name, int data)//no context
	{
		return set(name, data, context);
	}
	
	public static boolean set(String name, int data, Context context)
	{
		return Utile.writeFile(name, data + "", context);
	}
	
	public boolean set(String name, boolean data)//no context
	{
		return set(name, data, context);
	}
	
	public static boolean set(String name, boolean data, Context context)
	{
		return Utile.writeFile(name, data + "", context);
	}
	
	public String get(String name)//no context
	{
		return get(name, context);
	}
	
	public static String get(String name, Context context)
	{
		return get(name, null, context);
	}
	
	public String get(String name, String defaut)//no context
	{
		return get(name, null, context);
	}
		
	public static String get(String name, String defaut, Context context)
	{
		String val = Utile.readFile(name, context);
		if(val != null) return val;
		
		if(me.prop == null) loadProp(context);
		return me.prop.getProperty(name, defaut);
	}
	
	public int getI(String name)//no context
	{
		return getI(name, context);
	}
	
	public static int getI(String name, Context context)
	{
		return getI(name, -1, context);
	}
	
	public int getI(String name, int defaut)//no context
	{
		return getI(name, defaut, context);
	}
	
	public static int getI(String name, int defaut, Context context)
	{
		String val = get(name, context);
		if(val == null) return defaut;
		else
		{
			int i = defaut;
			try
			{
				i = Integer.parseInt(val);
			}
			catch(Exception e){}
			return i;
		}
	}
	
	public boolean getB(String name)//no context
	{
		return getB(name, false, context);
	}
	
	public static boolean getB(String name, Context context)
	{
		return getB(name, false, context);
	}
	
	public boolean getB(String name, boolean defaut)//no context
	{
		return getB(name, false, context);
	}
		
	public static boolean getB(String name, boolean defaut, Context context)
	{
		String val = get(name, context);
		if(val == null) return defaut;
		else
		{
			val = val.trim().toLowerCase();
			return val.equals("true") || val.equals("1") || val.equals("yes");
		}
	}
	
	public List<String> getList(String name)//no context
	{
		return getList(name, context);
	}
	
	public static List<String> getList(String name, Context context)
	{
		List<String> list = null;
		String listStr = get(name, null, context);
		
		if(listStr != null && listStr.trim().length() > 0)
		{
			list = new ArrayList<String>(Arrays.asList(listStr.split(";")));
		}
		
		return list;
	}
	
	private static void loadProp(Context context)
	{
		try
		{
			InputStream inputStream = context.getResources().getAssets().open(FILE_NAME);
		    me.prop = new Properties();
		    me.prop.load(inputStream);
		}
		catch(Exception e)
		{
			L.e("Erreur loading properteis e=["+e.getMessage()+"]");
			e.printStackTrace();
		}
	}
	
}
