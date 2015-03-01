package it.gbresciani.poligame.customUIElements;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;

/**
 * Custom preference to choose the number of syllables
 */
public class NOSyllablesPreference extends DialogPreference {

    private int defaultNumber = 4;

    @InjectView(R.id.syllables_radio_group) RadioGroup syllablesRadioGroup;

    public NOSyllablesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_no_syllables);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ButterKnife.inject(this, view);

        syllablesRadioGroup.check(getPersistedInt(R.id.four_syllables_radio_button));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(syllablesRadioGroup.getCheckedRadioButtonId());
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (getPersistedInt(R.id.four_syllables_radio_button) == R.id.four_syllables_radio_button) {
            return String.valueOf(4);
        }
        if (getPersistedInt(R.id.four_syllables_radio_button) == R.id.two_syllables_radio_button) {
            return String.valueOf(2);
        }

        return String.valueOf(defaultNumber);
    }
}
