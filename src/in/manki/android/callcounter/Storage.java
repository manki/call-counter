package in.manki.android.callcounter;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Storage {

  private static final String PREFS_FILE = "CallCounterPrefs";
  private final String REVERSE_CHRONOLOGICAL =
      CallLogDatabaseOpenHelper.CALL_TIME_COLUMN + " DESC";

  // Preference keys.
  private static final String TRACKING_ENABLED = "tracking-enabled";

  private final SharedPreferences prefs;
  private final CallLogDatabaseOpenHelper dbHelper;

  public Storage(SharedPreferences prefs, CallLogDatabaseOpenHelper dbHelper) {
    this.prefs = prefs;
    this.dbHelper = dbHelper;
  }

  public static Storage get(Context ctx) {
    SharedPreferences prefs =
        ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    CallLogDatabaseOpenHelper dbHelper = new CallLogDatabaseOpenHelper(ctx);
    return new Storage(prefs, dbHelper);
  }

  public long getCumulativeMinutes() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(
        CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE,
        new String[] {
            "SUM(" + CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN + ")"
            },
        CallLogDatabaseOpenHelper.TRACKED_COLUMN + " != 0",
        null,   // where args
        null,   // group by
        null,   // having
        null);  // order by
    c.moveToNext();
    return c.getLong(0);
  }

  public void track(String name, String number, long timestamp, long minutes) {
    ContentValues values = new ContentValues();
    values.put(CallLogDatabaseOpenHelper.CALL_TIME_COLUMN, timestamp);
    values.put(CallLogDatabaseOpenHelper.NAME_COLUMN, name);
    values.put(CallLogDatabaseOpenHelper.NUMBER_COLUMN, number);
    values.put(CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN, minutes);
    values.put(CallLogDatabaseOpenHelper.TRACKED_COLUMN, isTrackingEnabled());
    values.put(CallLogDatabaseOpenHelper.ARCHIVED_COLUMN, false);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.insert(CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE, null, values);
    db.close();
  }

  public void resetDuration() {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(CallLogDatabaseOpenHelper.ARCHIVED_COLUMN, true);
    db.update(
        CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE,
        values,
        CallLogDatabaseOpenHelper.ARCHIVED_COLUMN + " = 0",
        null);
    db.close();
  }

  public String getLastTrackedNumber() {
    Cursor c = open(null, null, REVERSE_CHRONOLOGICAL);
    c.moveToNext();
    try {
      if (c.isAfterLast()) {
        return null;
      }
      return c.getString(
          c.getColumnIndex(CallLogDatabaseOpenHelper.NUMBER_COLUMN));
    } finally {
      c.close();
    }
  }

  public long getLastTrackedCallTime() {
    Cursor c = open(null, null, REVERSE_CHRONOLOGICAL);
    c.moveToNext();
    try {
      if (c.isAfterLast()) {
        return 0;
      }
      return c.getLong(
          c.getColumnIndex(CallLogDatabaseOpenHelper.CALL_TIME_COLUMN));
    } finally {
      c.close();
    }
  }

  private Cursor open(String where, String[] whereArgs, String orderBy) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    return db.query(
        CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE,
        null,
        where,
        whereArgs,
        null,    // group by
        null,    // having
        orderBy);
  }

  public boolean isTrackingEnabled() {
    return prefs.getBoolean(TRACKING_ENABLED, false);
  }

  public void setTrackingEnabled(boolean enabled) {
    prefs.edit()
        .putBoolean(TRACKING_ENABLED, enabled)
        .commit();
  }

}
