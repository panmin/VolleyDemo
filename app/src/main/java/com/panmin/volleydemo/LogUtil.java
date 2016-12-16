package com.panmin.volleydemo;

import android.util.Log;

public class LogUtil {
	private static final String PREFIX_TAG = "ASU_";
	public static final boolean DEBUG = true;

	public static void v(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.VERBOSE) */)
			Log.v(PREFIX_TAG + tag, msg);
	}

	public static void v(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.VERBOSE) */)
			Log.v(PREFIX_TAG + tag, logFormat(format, args));
	}

	public static void d(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.DEBUG) */)
			Log.d(PREFIX_TAG + tag, msg);
//		System.out.print(PREFIX_TAG + tag+"="+msg);
	}

	public static void d(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.DEBUG) */)
			Log.d(PREFIX_TAG + tag, logFormat(format, args));
//		System.out.print(PREFIX_TAG + tag+"="+logFormat(format, args));
	}

	public static void i(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.INFO) */)
			Log.i(PREFIX_TAG + tag, msg);
//		System.out.print(PREFIX_TAG + tag+"="+msg);
	}

	public static void i(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.INFO) */)
			Log.i(PREFIX_TAG + tag, logFormat(format, args));
//		System.out.print(PREFIX_TAG + tag+"="+logFormat(format, args));
	}

	public static void w(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.WARN) */)
			Log.w(PREFIX_TAG + tag, msg);
	}

	public static void w(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.WARN) */) {
			Log.w(PREFIX_TAG + tag, logFormat(format, args));
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.ERROR) */)
			Log.e(PREFIX_TAG + tag, msg);
	}

	public static void e(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.ERROR) */)
			Log.e(PREFIX_TAG + tag, logFormat(format, args));
	}

	public static void wtf(String tag, String msg) {
		if (DEBUG/* Log.isLoggable(tag, Log.ERROR) */)
			Log.wtf(PREFIX_TAG + tag, msg);
	}

	public static void wtf(String tag, String format, Object... args) {
		if (DEBUG/* Log.isLoggable(tag, Log.ERROR) */)
			Log.wtf(PREFIX_TAG + tag, logFormat(format, args));
	}

	private static String prettyArray(String[] array) {
		if (array.length == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder("[");
		int len = array.length - 1;
		for (int i = 0; i < len; i++) {
			sb.append(array[i]);
			sb.append(", ");
		}
		sb.append(array[len]);
		sb.append("]");

		return sb.toString();
	}

	private static String logFormat(String format, Object... args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof String[]) {
				args[i] = prettyArray((String[]) args[i]);
			}
		}
		String s = String.format(format, args);
		s = "[" + Thread.currentThread().getId() + "] " + s;
		return s;
	}
}