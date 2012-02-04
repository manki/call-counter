package in.manki.android.callcounter;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TopUpMinutesDialogFragment extends DialogFragment {

  private final CallCounterActivity parent;

  public TopUpMinutesDialogFragment(CallCounterActivity parent) {
    this.parent = parent;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle(R.string.add_discount_call_minutes);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View dialogView = inflater.inflate(R.layout.topup_dialog, container);

    Button ok = (Button) dialogView.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EditText edit =
            (EditText) dialogView.findViewById(R.id.new_discount_minutes);
        String text = edit.getText().toString();
        int minutes;
        try {
          minutes = Integer.parseInt(text);
        } catch (NumberFormatException e) {
          minutes = 0;
        }
        parent.topUp(minutes);

        TopUpMinutesDialogFragment.this.dismiss();
      }
    });

    return dialogView;
  }

}
