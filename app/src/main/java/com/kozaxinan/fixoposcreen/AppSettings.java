package com.kozaxinan.fixoposcreen;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by Sinan on 21.10.2014.
 */
public class AppSettings {

	/** The count constant for fixes */
	private static final String COUNT = "Use_Count";

	/** The enable constant for setting */
	private static final String ENABLE = "Service_Status";

	private static final String IMMERSIVE_ENABLE = "Immersive_Service_Status";

	/** The singleton instance of appsetting */
	private static AppSettings INSTANCE;

	/** The shared preference */
	private SharedPreferences pref;

	/** The editor */
	private SharedPreferences.Editor editor;

	private AppSettings() {
	}

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
			pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
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
			editor = getSharedPref(context).edit();
		}
		return editor;
	}

	/**
	 * Increase the number of fix.
	 *
	 * @param context
	 */
	public int increaseUseCount(Context context) {
		int value = getUseCount(context) + 1;
		getEditor(context).putInt(COUNT, value).commit();
		return value;
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
		BackupManager bm = new BackupManager(context);
		bm.dataChanged();
	}

	/**
	 * Get status of fix service.
	 *
	 * @param context
	 * @return boolean
	 */
	public boolean isServiceEnable(Context context) {
		return getSharedPref(context).getBoolean(ENABLE, true);
	}

	/**
	 * Enable fix services.
	 *
	 * @param context
	 * @param isEnable boolean
	 */
	public void setImmersiveServiceEnable(Context context, boolean isEnable) {
		getEditor(context).putBoolean(IMMERSIVE_ENABLE, isEnable).commit();
		BackupManager bm = new BackupManager(context);
		bm.dataChanged();
	}

	/**
	 * Get status of fix service.
	 *
	 * @param context
	 * @return boolean
	 */
	public boolean isImmersiveServiceEnable(Context context) {
		return getSharedPref(context).getBoolean(IMMERSIVE_ENABLE, false);
	}

	public boolean isPro(Context context) {
		if (BuildConfig.DEBUG) {
			return true;
		}
		return getSharedPref(context).getBoolean(Utils.PRO, false);
	}

	public void setPro(Context context, boolean isPro) {
		getEditor(context).putBoolean(Utils.PRO, isPro).commit();
		BackupManager bm = new BackupManager(context);
		bm.dataChanged();
	}

	public boolean isTry(Context context) {
		return getSharedPref(context).getInt(Utils.TRY, 0) <= 10;
	}

    public void resetTry(Context context) {
        getEditor(context).putInt(Utils.TRY, 5).commit();
    }

	public void increaseTry(Context context) {
		int value = getSharedPref(context).getInt(Utils.TRY, 0) + 1;
		getEditor(context).putInt(Utils.TRY, value).commit();
		if (!isPro(context) && value > 10) {
			Toast.makeText(context, "Your trial time is end for immersive mode.", Toast.LENGTH_LONG).show();
			setImmersiveServiceEnable(context, false);
			// Show notification for buy
			Utils.showNotification(context, "Immersive mode try is ended.", "Buy pro version to use unlimited fix service.");
		}
	}
}
