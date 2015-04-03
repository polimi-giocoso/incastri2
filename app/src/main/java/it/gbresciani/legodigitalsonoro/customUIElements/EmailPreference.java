package it.gbresciani.legodigitalsonoro.customUIElements;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.squareup.otto.Bus;

import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;
import it.gbresciani.legodigitalsonoro.helper.Helper;

public class EmailPreference extends EditTextPreference {

    private final Context mContext;
    private Bus BUS;

    public EmailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        BUS = BusProvider.getInstance();
    }

    @Override protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override protected void showDialog(Bundle state) {
        super.showDialog(state);

        final EditText editText = getEditText();
        final AlertDialog d = (AlertDialog) getDialog();

        //Validate email on OK click
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String emailToValidate = editText.getText().toString();
                boolean isEmpty = "".equals(emailToValidate);
                if (!isEmpty && !Helper.isValidEmail(emailToValidate)) {
                    editText.setError(mContext.getResources().getString(R.string.mail_not_valid_message));
                } else {
                    if (isEmpty) {
                        // Disable data collect
                        getSharedPreferences().edit().putBoolean(mContext.getResources().getString(R.string.setting_collect_key), false).commit();
                    }
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