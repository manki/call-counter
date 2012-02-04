package in.manki.android.callcounter;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CallCounterActivity extends ListActivity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    refreshView();

    Button reset = (Button) findViewById(R.id.reset);
    reset.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        reset();
      }
    });

    CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
    enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        setTrackingEnabled(isChecked);
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    refreshView();
  }

  private void reset() {
    getStorage(this).resetDuration();
    refreshView();
  }

  private void setTrackingEnabled(boolean enabled) {
    getStorage(this).setTrackingEnabled(enabled);
    refreshView();
  }

  private void refreshView() {
    Storage storage = getStorage(this);

    CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
    enabled.setChecked(storage.isTrackingEnabled());

    String fmt = getString(R.string.accumulated_calls);
    TextView callDuration = (TextView) findViewById(R.id.accumulatedDuration);
    callDuration.setText(
        Html.fromHtml(String.format(fmt, storage.getCumulativeMinutes())));

    Cursor cursor = storage.getTrackedCalls();
    startManagingCursor(cursor);
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        this,
        R.layout.call_log_entry,
        cursor,
        new String[] {
            CallLogDatabaseOpenHelper.NAME_COLUMN,
            CallLogDatabaseOpenHelper.NUMBER_COLUMN,
            CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN,
        },
        new int[] {
            R.id.name,
            R.id.number,
            R.id.duration,
        });
    setListAdapter(adapter);
  }

  private Storage getStorage(Context ctx) {
    return Storage.get(ctx);
  }
}