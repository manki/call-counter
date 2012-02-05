package in.manki.android.callcounter;

import java.util.Set;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

public class CallCounterActivity extends FragmentActivity {

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

    final EditText prefixes = (EditText) findViewById(R.id.prefixes);
    prefixes.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        Storage storage = getStorage(CallCounterActivity.this);
        storage.setTrackableNumberPrefixes(split(prefixes.getText().toString()));
        return false;
      }
    });
  }

  private Set<String> split(String str) {
    return ImmutableSet.copyOf(
        Splitter.on(Pattern.compile(",|;|\\s"))
            .trimResults()
            .omitEmptyStrings()
            .split(str));
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
    df.show(getSupportFragmentManager(), TOP_UP_DIALOG_TAG);
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

    TextView remaining = (TextView) findViewById(R.id.remaining_minutes);
    remaining.setText(String.valueOf(storage.getRemainingMinutes()));

    CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
    enabled.setChecked(storage.isTrackingEnabled());

    EditText prefixes = (EditText) findViewById(R.id.prefixes);
    prefixes.setText(
        Joiner.on('\n').join(storage.getTrackabelNumberPrefixes()));

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
    ListView callHistory = (ListView) findViewById(R.id.call_history);
    callHistory.setAdapter(adapter);
  }

  private Storage getStorage(Context ctx) {
    return Storage.get(ctx);
  }
}