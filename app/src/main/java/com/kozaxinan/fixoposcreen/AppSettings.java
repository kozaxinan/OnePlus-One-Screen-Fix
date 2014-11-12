package com.kozaxinan.fixoposcreen;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sinan on 21.10.2014.
 */
public class AppSettings {

	/** The count constant for fixes */
	private static final String COUNT = "Use_Count";

	/** The enable constant for setting */
	private static final String ENABLE = "Service_Status";

	/** The singleton instance of appsetting */
	private static AppSettings INSTANCE;

	/** The shared preference */
	private SharedPreferences pref;

	/** The editor */
	private SharedPreferences.Editor editor;

	/**
	 * Get the app setting.
	 *
	 * @return INSTANCE the app setting
	 */
	public static AppSettings getInstance() {

		if (INSTANCE == null) {
			INSTANCE = new AppSettings();
		}
		return INSTANCE;
	}

	/**
	 * Get the preference for internal use.
	 *
	 * @param context
	 * @return pref
	 */
	private SharedPreferences getSharedPref(Context context) {
		if (pref == null) {
			pref= context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		}
		return pref;
	}

	/**
	 * Get the editor for internal use.
	 *
	 * @param context
	 * @return editor
	 */
	private SharedPreferences.Editor getEditor(Context context) {
		if (editor == null) {
			editor= getSharedPref(context).edit();
		}
		return editor;
	}

	/**
	 * Increase the number of fix.
	 *
	 * @param context
	 */
	public void increaseUseCount(Context context) {
		getEditor(context).putInt(COUNT, getUseCount(context) + 1).commit();
	}

	/**
	 * Get the number of fixes.
	 *
	 * @param context
	 * @return int
	 */
	public int getUseCount(Context context) {
		return getSharedPref(context).getInt(COUNT, 0);
	}

	/**
	 * Enable fix services.
	 *
	 * @param context
	 * @param isEnable boolean
	 */
	public void enableService(Context context, boolean isEnable) {
		getEditor(context).putBoolean(ENABLE, isEnable).commit();
	}

	/**
	 * Get status of fix service.
	 *
	 * @param context
	 * @return boolean
	 */
	public boolean isServiceEnable(Context context) {
		return getSharedPref(context).getBoolean(ENABLE,true);
	}
}
