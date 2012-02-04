package in.manki.android.callcounter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CallCounterActivity extends ListActivity {

  private static final String TOP_UP_DIALOG_TAG = "top-up";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    refreshView();

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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater mi = getMenuInflater();
    mi.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.reset_meter:
        confirmAndReset();
        return true;
      case R.id.add_minutes:
        showTopUpDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void topUp(int minutes) {
    Storage storage = getStorage(this);
    storage.setCreditMinutes(storage.getCreditMinutes() + minutes);
    refreshView();
  }

  private void showTopUpDialog() {
    TopUpMinutesDialogFragment df = new TopUpMinutesDialogFragment(this);
    df.show(getFragmentManager(), TOP_UP_DIALOG_TAG);
  }

  private void confirmAndReset() {
    AlertDialog alert = new AlertDialog.Builder(this)
        .setMessage(R.string.reset_warning)
        .setPositiveButton(R.string.clear_data, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            CallCounterActivity activity = CallCounterActivity.this;
            activity.getStorage(activity).resetDuration();
            activity.refreshView();
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        })
        .create();
    alert.show();
  }

  private void setTrackingEnabled(boolean enabled) {
    getStorage(this).setTrackingEnabled(enabled);
    refreshView();
  }

  private void refreshView() {
    Storage storage = getStorage(this);

    CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
    enabled.setChecked(storage.isTrackingEnabled());

    TextView remaining = (TextView) findViewById(R.id.remaining_minutes);
    remaining.setText(String.valueOf(storage.getRemainingMinutes()));

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