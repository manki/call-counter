package in.manki.android.callcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

public class CallTracker extends BroadcastReceiver {

  private static final long TEN_SECONDS_IN_MILLIS = 10 * 1000;
  private static final String[] SELECTED_COLS = new String[] {
      CallLog.Calls.DATE,
      CallLog.Calls.NUMBER,
      CallLog.Calls.DURATION,
  };

  @Override
  public void onReceive(final Context ctx, Intent intent) {
    final Storage storage = Storage.get(ctx);

    if (!storage.isTrackingEnabled() || !isInIdleState(intent)) {
      return;
    }

    new Thread(new Runnable() {
      @Override
      public void run() {
        // Wait for call log database to be updated.
        // Otherwise we won't find the call that has just ended.
        try {
          Thread.sleep(TEN_SECONDS_IN_MILLIS);
        } catch (InterruptedException e) {
          // Just ignore.
        }

        long lastCallTime = storage.getLastTrackedCallTime();
        Cursor cursor = ctx.getContentResolver().query(
            CallLog.Calls.CONTENT_URI,
            SELECTED_COLS,
            CallLog.Calls.TYPE + " = ? AND "
                + CallLog.Calls.DATE + " > ?",
                new String[] {
                String.valueOf(CallLog.Calls.OUTGOING_TYPE),
                String.valueOf(lastCallTime)},
                CallLog.Calls.DATE);
        while (cursor.moveToNext()) {
          long callTime =
              cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
          String number =
              cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
          long duration =
              cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
          if (isInternationalCall(number)) {
            storage.track(number, callTime, duration);
            Log.d("CallCounter",
                String.format("Tracked call to %s for %d seconds.",
                    number, duration));
          }
        }
      }
    }).start();
  }

  private boolean isInIdleState(Intent intent) {
    String state = intent.getExtras().getString("state");
    return "IDLE".equals(state);
  }

  private boolean isInternationalCall(String number) {
    if (number.startsWith("+")) {
      return !number.startsWith("+61");
    } else {
      return number.startsWith("0011");
    }
  }
}
