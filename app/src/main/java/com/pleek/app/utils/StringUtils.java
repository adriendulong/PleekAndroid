package com.pleek.app.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StringUtils {

	public static boolean isStringEmpty(String str) {
		if (str == null || str.equals("") || str.equals("null")) {
			return true;
		}
		
		return false;
	}
}