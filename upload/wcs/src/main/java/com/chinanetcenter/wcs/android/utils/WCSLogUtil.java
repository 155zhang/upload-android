package com.chinanetcenter.wcs.android.utils;

import com.chinanetcenter.wcs.android.Config;

import android.util.Log;

public class WCSLogUtil {

	private static final String TAG = "CNCLog";

	private static boolean DEBUGGING = Config.DEBUGGING;

	public static void v(String msg) {
		WCSLogUtil.v(null, msg);
	}

	public static void v(Class<?> clazz, String msg) {
		if (DEBUGGING) {
			Log.v(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void v(Class<?> clazz, String msg, Throwable tr) {
		if (DEBUGGING) {
			Log.v(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void d(Class<?> clazz, String msg) {
		if (DEBUGGING) {
			Log.d(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void d(Class<?> clazz, String msg, Throwable tr) {
		if (DEBUGGING) {
			Log.d(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void d(String msg) {
		WCSLogUtil.d(null, msg);
	}

	public static void i(Class<?> clazz, String msg) {
		if (DEBUGGING) {
			Log.i(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void i(Class<?> clazz, String msg, Throwable tr) {
		if (DEBUGGING) {
			Log.i(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void i(String msg) {
		WCSLogUtil.i(null, msg);
	}

	public static void w(Class<?> clazz, String msg) {
		if (DEBUGGING) {
			Log.w(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void w(Class<?> clazz, String msg, Throwable tr) {
		if (DEBUGGING) {
			Log.w(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void w(String msg) {
		WCSLogUtil.w(null, msg);
	}

	public static void e(Class<?> clazz, String msg) {
		if (DEBUGGING) {
			Log.e(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void e(Class<?> clazz, String msg, Throwable tr) {
		if (DEBUGGING) {
			Log.e(null == clazz ? TAG : getTag(clazz), msg);
		}
	}

	public static void e(String msg) {
		WCSLogUtil.e(null, msg);
	}

	private static String getTag(Class<?> clazz) {
		return "[CNCLog" + clazz.getSimpleName() + "]";
	}
}
