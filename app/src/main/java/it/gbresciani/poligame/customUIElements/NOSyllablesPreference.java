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

        int noSyllables = getPersistedInt(defaultNumber);
        if(noSyllables == 4){
            syllablesRadioGroup.check(R.id.four_syllables_radio_button);
        }
        if(noSyllables == 2){
            syllablesRadioGroup.check(R.id.two_syllables_radio_button);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            if(syllablesRadioGroup.getCheckedRadioButtonId() == R.id.four_syllables_radio_button){
                persistInt(4);
            }
            if(syllablesRadioGroup.getCheckedRadioButtonId()== R.id.two_syllables_radio_button){
                persistInt(2);
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return String.valueOf(getPersistedInt(defaultNumber));
    }
}
