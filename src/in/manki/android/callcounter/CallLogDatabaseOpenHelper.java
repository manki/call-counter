package in.manki.android.callcounter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallLogDatabaseOpenHelper extends SQLiteOpenHelper {

  public static final String TRACKED_CALLS_TABLE = "TrackedCalls";
  public static final String ID_COLUMN = "_Id";
  public static final String CALL_TIME_COLUMN = "CallTime";
  public static final String NAME_COLUMN = "Name";
  public static final String NUMBER_COLUMN = "Number";
  public static final String CALL_DURATION_COLUMN = "CallDuration";
  public static final String TRACKED_COLUMN = "Tracked";
  public static final String ARCHIVED_COLUMN = "Archived";

  private static final String DB_NAME = "CallLog";
  private static final int DB_VERSION = 1;

  public CallLogDatabaseOpenHelper(Context ctx) {
    super(ctx, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TRACKED_CALLS_TABLE + "("
        + ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
        + CALL_TIME_COLUMN + " INTEGER NOT NULL, "
        + NAME_COLUMN + " TEXT NOT NULL, "
        + NUMBER_COLUMN + " TEXT NOT NULL, "
        + CALL_DURATION_COLUMN + " INTEGER NOT NULL, "
        + TRACKED_COLUMN + " BOOLEAN NOT NULL, "
        + ARCHIVED_COLUMN + " BOOLEAN NOT NULL"
        + ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

}
