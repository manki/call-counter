package in.manki.android.callcounter;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesStorage {

  private static final String PREFS_FILE = "CallCounterPrefs";
  private static final String CUMULATIVE_DURATION = "cumulative-duration";
  private static final String LAST_TRACKED_NUMBER = "last-tracked-number";
  private static final String LAST_TRACKED_CALL_TIME = "last-tracked-call-time";
  private static final String TRACKING_ENABLED = "tracking-enabled";

  private final SharedPreferences prefs;

  public PreferencesStorage(SharedPreferences prefs) {
    this.prefs = prefs;
  }

  public static PreferencesStorage get(Context ctx) {
    SharedPreferences prefs =
        ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    return new PreferencesStorage(prefs);
  }

  public long getCumulativeMinutes() {
    return prefs.getLong(CUMULATIVE_DURATION, 0);
  }

  public void track(String name, String number, long timestamp, long minutes) {
    prefs.edit()
        .putLong(CUMULATIVE_DURATION, getCumulativeMinutes() + minutes)
        .putString(LAST_TRACKED_NUMBER, number)
        .putLong(LAST_TRACKED_CALL_TIME, timestamp)
        .commit();
  }

  public void resetDuration() {
    prefs.edit()
        .putLong(CUMULATIVE_DURATION, 0)
        .putString(LAST_TRACKED_NUMBER, null)
        .putLong(LAST_TRACKED_CALL_TIME, new Date().getTime())
        .commit();
  }

  public String getLastTrackedNumber() {
    return prefs.getString(LAST_TRACKED_NUMBER, null);
  }

  public long getLastTrackedCallTime() {
    return prefs.getLong(LAST_TRACKED_CALL_TIME, 0);
  }

  public boolean isTrackingEnabled() {
    return prefs.getBoolean(TRACKING_ENABLED, false);
  }

  public void setTrackingEnabled(boolean enabled) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(TRACKING_ENABLED, enabled)
        .putLong(LAST_TRACKED_CALL_TIME, new Date().getTime());
    if (enabled) {
      editor.putString(LAST_TRACKED_NUMBER, null);
    }
    editor.commit();
  }
}
