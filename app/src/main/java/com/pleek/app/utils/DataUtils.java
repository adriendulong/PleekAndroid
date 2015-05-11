package com.pleek.app.utils;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;

public class DataUtils {

	public static long dirSize(File dir) {

	    if (dir.exists()) {
	        long result = 0;
	        File[] fileList = dir.listFiles();
	        for(int i = 0; i < fileList.length; i++) {
	            // Recursive call if it's a directory
	            if(fileList[i].isDirectory()) {
	                result += dirSize(fileList [i]);
	            } else {
	                // Sum the file size in bytes
	                result += fileList[i].length();
	            }
	        }
	        return result; // return the file size
	    }
	    
	    return 0;
	}

	public static boolean deleteFile(File file) {
		if (file != null) {
		    if (file.isDirectory()) {
				String[] children = file.list();
				for (int i = 0; i < children.length; i++) {
				    boolean success = deleteFile(new File(file, children[i]));
				    if (!success) {
				    	return false;
				    }
				}
		    }

		    return file.delete();
		}

		return false;
    }
}