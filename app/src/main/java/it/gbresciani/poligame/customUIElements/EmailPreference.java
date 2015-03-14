package it.gbresciani.poligame.customUIElements;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.helper.Helper;

public class EmailPreference extends EditTextPreference {

    private final Context mContext;

    public EmailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override protected void showDialog(Bundle state) {
        super.showDialog(state);

        final EditText editText = getEditText();
        final AlertDialog d = (AlertDialog)getDialog();

        //Validate email on OK click
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String emailToValidate = editText.getText().toString();

                if (!"".equals(emailToValidate) && !Helper.isValidEmail(emailToValidate)) {
                    editText.setError(mContext.getResources().getString(R.string.mail_not_valid_message));
                } else {
                    persistString(emailToValidate);
                    onDialogClosed(true);
                    d.dismiss();
                }
            }
        });
    }

    @Override protected void onDialogClosed(boolean positiveResult) {
        setSummary(getSummary());
        super.onDialogClosed(positiveResult);
    }

    @Override
    public CharSequence getSummary() {
        return String.valueOf(getPersistedString("email@example.com"));
    }
}