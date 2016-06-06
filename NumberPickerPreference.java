package tk.parmclee.starfootprint;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.widget.NumberPicker;

public class NumberPickerPreference extends Preference
        implements Preference.OnPreferenceClickListener, NumberPicker.OnValueChangeListener,
        DialogInterface.OnClickListener {

    AlertDialog mDialog;
    NumberPicker mPicker;
    private int mMinute;
    String mDefaultSummary; //text without time in setSummary

    public NumberPickerPreference(Context context) {
        super(context);

        mPicker = new NumberPicker(context);
        mPicker.setMinValue(1);
        mPicker.setMaxValue(60);
        mPicker.setOnValueChangedListener(this);
        mPicker.setWrapSelectorWheel(false);

        LinearLayoutCompat layout = new LinearLayoutCompat(context);
        layout.addView(mPicker);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mDialog = builder.setView(layout)
                .setTitle(R.string.set_interval)
                .setPositiveButton(getContext().getString(R.string.ok), this)
                .setNeutralButton(getContext().getString(R.string.cancel), this)
                .create();
        setOnPreferenceClickListener(this);
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mMinute = this.getPersistedInt(0);
        } else {
            int defaultInt;
            if (defaultValue != null) defaultInt = (int) defaultValue;
            else defaultInt = 1;
            mMinute = defaultInt;
            persistInt(defaultInt);
            notifyChanged();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }

    @Override
    public void setSummary(CharSequence summary) {
        mDefaultSummary = (String) summary;
        super.setSummary(summary + " " + mMinute + " " + getContext().getString(R.string.minute));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        mPicker.setValue(mMinute);
        mDialog.show();
        return true;
    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mMinute = newVal;
        persistInt(newVal);
        notifyChanged();
        setSummary(mDefaultSummary);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        /* do nothing, everything happens in onValueChange */
    }
}