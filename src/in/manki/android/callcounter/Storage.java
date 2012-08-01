package in.manki.android.callcounter;

import java.util.Date;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

public class Storage {

  private final String REVERSE_CHRONOLOGICAL =
      CallLogDatabaseOpenHelper.CALL_TIME_COLUMN + " DESC";

  // Preference strings.
  private static final String PREFS_FILE = "CallCounterPrefs";
  private static final String TRACKING_ENABLED = "tracking-enabled";
  private static final String TRACK_MIN_CALL_TIME = "track-min-call-time";
  private static final String CREDIT_MINUTES = "credit-minutes";
  private static final String NUMBER_PREFIXES = "number-prefixes";

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

  public long getRemainingMinutes() {
    return getCreditMinutes() - getCumulativeMinutes();
  }

  public long getCumulativeMinutes() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(
        CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE,
        new String[] {
            "SUM(" + CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN + ")"
            },
        CallLogDatabaseOpenHelper.TRACKED_COLUMN + " != 0 AND "
            + CallLogDatabaseOpenHelper.ARCHIVED_COLUMN + " = 0",
        null,   // where args
        null,   // group by
        null,   // having
        null);  // order by
    try {
      c.moveToNext();
      return c.getLong(0);
    } finally {
      c.close();
      db.close();
    }
  }

  public void track(String name, String number, long timestamp, long minutes, boolean tracked) {
    ContentValues values = new ContentValues();
    values.put(CallLogDatabaseOpenHelper.CALL_TIME_COLUMN, timestamp);
    values.put(CallLogDatabaseOpenHelper.NAME_COLUMN, name);
    values.put(CallLogDatabaseOpenHelper.NUMBER_COLUMN, number);
    values.put(CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN, minutes);
    values.put(CallLogDatabaseOpenHelper.TRACKED_COLUMN, tracked);
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

    setTrackMinCallTime(new Date().getTime());
    setCreditMinutes(0);
  }

  public String getLastTrackedNumber() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = open(
        db,
        CallLogDatabaseOpenHelper.TRACKED_COLUMN + " = 1",
        null,
        REVERSE_CHRONOLOGICAL);
    c.moveToNext();
    try {
      if (c.isAfterLast()) {
        return null;
      }
      return c.getString(
          c.getColumnIndex(CallLogDatabaseOpenHelper.NUMBER_COLUMN));
    } finally {
      c.close();
      db.close();
    }
  }

  public long getLastTrackedCallTime() {
    return getLastCallTime(CallLogDatabaseOpenHelper.TRACKED_COLUMN + " = 1");
  }

  public long getLastKnownCallTime() {
    return getLastCallTime(null);
  }

  private long getLastCallTime(String where) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = open(
        db,
        where,
        null,
        REVERSE_CHRONOLOGICAL);
    c.moveToNext();
    try {
      if (c.isAfterLast()) {
        return 0;
      }
      return c.getLong(
          c.getColumnIndex(CallLogDatabaseOpenHelper.CALL_TIME_COLUMN));
    } finally {
      c.close();
      db.close();
    }
  }

  private Cursor open(
      SQLiteDatabase db, String where, String[] whereArgs, String orderBy) {
    return db.query(
        CallLogDatabaseOpenHelper.TRACKED_CALLS_TABLE,
        CallLogDatabaseOpenHelper.ALL_COLUMNS,
        where,
        whereArgs,
        null,    // group by
        null,    // having
        orderBy);
  }

  public Cursor getTrackedCalls() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    return open(
        db,
        CallLogDatabaseOpenHelper.TRACKED_COLUMN + " = 1 AND "
            + CallLogDatabaseOpenHelper.ARCHIVED_COLUMN + " = 0",
        null,
        REVERSE_CHRONOLOGICAL);
  }

  public boolean isTrackingEnabled() {
    return prefs.getBoolean(TRACKING_ENABLED, false);
  }

  public void setTrackingEnabled(boolean enabled) {
    prefs.edit()
        .putBoolean(TRACKING_ENABLED, enabled)
        .commit();
    setTrackMinCallTime(new Date().getTime());
  }

  public long getTrackMinCallTime() {
    return prefs.getLong(TRACK_MIN_CALL_TIME, 0);
  }

  private void setTrackMinCallTime(long t) {
    prefs.edit()
        .putLong(TRACK_MIN_CALL_TIME, t)
        .commit();
  }

  public long getCreditMinutes() {
    return prefs.getLong(CREDIT_MINUTES, 0);
  }

  public void setCreditMinutes(long mins) {
    prefs.edit()
        .putLong(CREDIT_MINUTES, mins)
        .commit();
  }

  public Set<String> getTrackabelNumberPrefixes() {
    String prefixes = prefs.getString(NUMBER_PREFIXES, "");
    return ImmutableSet.copyOf(Splitter.on(' ').split(prefixes));
  }

  public void setTrackableNumberPrefixes(Set<String> prefixes) {
    prefs.edit()
        .putString(NUMBER_PREFIXES, Joiner.on(' ').join(prefixes))
        .commit();
  }
}
