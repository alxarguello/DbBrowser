package com.browser.app.util;

import java.util.regex.Pattern;

import android.content.Context;

public final class Util {

	public static String getResourceString(int stringId, Context cc) {
		String s = null;
		if (cc != null) {
			s = cc.getString(stringId);
		}
		return s;
	}

	public static int objectToInteger(Object val) {
		int v = 0;
		if (val != null) {
			v = stringToInteger(String.valueOf(val));
		}
		return v;
	}

	public static int stringToInteger(String val) {
		int v = 0;
		if (val != null && Pattern.matches("[0-9]{1,4}", val)) {
			v = Integer.parseInt(val);
		}
		return v;
	}

	public static String integerToString(int value) {
		return String.valueOf(value);
	}

	public static boolean stringToBoolean(String value) {
		boolean v = false;
		try {
			if (value != null && value.equalsIgnoreCase("true")) {
				v = Boolean.parseBoolean(value);
			}
		} catch (Exception e) {

		}
		return v;

	}
}
