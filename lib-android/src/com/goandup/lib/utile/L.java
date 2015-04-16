package com.goandup.lib.utile;

import android.util.Log;

public class L
{
	private final static String TAG = "goandup";
	
	private final static boolean LOG = false;

	private final static boolean E = true && LOG;
	private final static boolean D = true && LOG;
	private final static boolean V = true && LOG;
	private final static boolean I = true && LOG;
	private final static boolean W = true && LOG;
	
	//ERROR
	public static void e(String msg)
	{
		e(TAG, msg);
	}
	public static void e(String tag, String msg)
	{
		if(E) Log.e(tag, msg);
	}
	
	//DEBUG
	public static void d(String msg)
	{
		d(TAG, msg);
	}
	public static void d(String tag, String msg)
	{
		if(D) Log.d(tag, msg);
	}
	
	//VERBOSE
	public static void v(String msg)
	{
		v(TAG, msg);
	}
	public static void v(String tag, String msg)
	{
		if(V) Log.v(tag, msg);
	}
	
	//INFO
	public static void i(String msg)
	{
		i(TAG, msg);
	}
	public static void i(String tag, String msg)
	{
		if(I) Log.i(tag, msg);
	}
	
	//WARNING
	public static void w(String msg)
	{
		w(TAG, msg);
	}
	public static void w(String tag, String msg)
	{
		if(W) Log.w(tag, msg);
	}
}
