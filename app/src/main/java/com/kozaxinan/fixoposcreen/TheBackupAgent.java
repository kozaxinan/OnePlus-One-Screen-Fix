package com.kozaxinan.fixoposcreen;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class TheBackupAgent extends BackupAgentHelper {
	// The name of the SharedPreferences file

	// A key to uniquely identify the set of backup data
	static final String FILES_BACKUP_KEY = "myfiles";

	// Allocate a helper and add it to the backup agent
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper =
				new SharedPreferencesBackupHelper(this, "settings");
		addHelper(FILES_BACKUP_KEY, helper);
	}
}