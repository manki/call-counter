package in.manki.android.callcounter;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class SettingsBackupAgent extends BackupAgentHelper {

  @Override
  public void onCreate() {
    super.onCreate();
    SharedPreferencesBackupHelper helper =
        new SharedPreferencesBackupHelper(this, Storage.BACKUP_PREFS_FILE);
    addHelper("prefs", helper);
  }
}
