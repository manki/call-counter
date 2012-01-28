package in.manki.android.callcounter;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class CallCounterActivity extends Activity {
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
    Storage.get(this).resetDuration();
    refreshView();
  }

  private void setTrackingEnabled(boolean enabled) {
    Storage.get(this).setTrackingEnabled(enabled);
    refreshView();
  }

  private void refreshView() {
    Storage storage = Storage.get(this);

    CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
    enabled.setChecked(storage.isTrackingEnabled());

    String fmt = getString(R.string.accumulated_calls);
    TextView callDuration = (TextView) findViewById(R.id.accumulatedDuration);
    callDuration.setText(
        Html.fromHtml(String.format(fmt, storage.getCumulativeMinutes())));

    String lastTrackedNumber = storage.getLastTrackedNumber();
    TextView lastTracked = (TextView) findViewById(R.id.lastTracked);
    if (lastTrackedNumber == null) {
      lastTracked.setText(R.string.no_call_has_been_tracked);
    } else {
      String lastCallTime = DateFormat.getDateTimeInstance().format(
          new Date(storage.getLastTrackedCallTime()));
      fmt = getString(R.string.last_tracked_call);
      Spanned text = Html.fromHtml(String.format(
          fmt, lastTrackedNumber, lastCallTime));
      lastTracked.setText(text);
    }
  }
}