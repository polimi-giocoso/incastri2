package it.gbresciani.legodigitalsonoro.customUIElements;


import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import it.gbresciani.legodigitalsonoro.R;

public class NumberPickerPreference extends DialogPreference{

    private NumberPicker numberPicker;

    private Integer min;
    private Integer max;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Get the custom attributes (min, max)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);

        min = a.getInteger(R.styleable.NumberPickerPreference_min, 1);
        max = a.getInteger(R.styleable.NumberPickerPreference_max, 10);

        a.recycle();

        setDialogLayoutResource(R.layout.dialog_number_picker);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        numberPicker = ((NumberPicker)view.findViewById(R.id.pref_interval_number_picker));
        numberPicker.setMaxValue(max);
        numberPicker.setMinValue(min);
        numberPicker.setValue(getPersistedInt(min));
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(numberPicker.getValue());
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return String.valueOf(getPersistedInt(min));
    }
}
