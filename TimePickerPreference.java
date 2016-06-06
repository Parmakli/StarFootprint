package tk.parmclee.starfootprint;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.widget.TimePicker;

import java.util.Locale;

public class TimePickerPreference extends Preference
        implements Preference.OnPreferenceClickListener, TimePickerDialog.OnTimeSetListener {

    TimePickerDialog mDialog;
    private int mHour, mMinute;
    String mDefaultSummary; //text without time in setSummary

    public TimePickerPreference(Context context) {
        super(context);
        mDialog = new TimePickerDialog(getContext(), this, mHour, mMinute, true);
        setOnPreferenceClickListener(this);
    }

    /**
     * Instance time representation in "HH:mm" format
     *
     * @return string with leading zeroes if necessary
     */
    public String getCurrentPickerTime() {
        return String.format(Locale.ENGLISH, "%02d" + ":" + "%02d", mHour, mMinute);
    }

    /**
     * Sets hours and minutes of this instance
     *
     * @param string in "Hh:mm" format
     */
    private void setTimeComponents(String string) {
        mHour = Integer.parseInt(string.split(":")[0]);
        mMinute = Integer.parseInt(string.split(":")[1]);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setTimeComponents(this.getPersistedString("never gain this string"));
        } else {
            String defaultString;
            if (defaultValue != null) defaultString = (String) defaultValue;
            else defaultString = "00:00";
            setTimeComponents(defaultString);
            persistString(defaultString);
            notifyChanged();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    public void setSummary(CharSequence summary) {
        mDefaultSummary = (String) summary;
        super.setSummary(summary + " " + getCurrentPickerTime());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        mDialog.updateTime(mHour, mMinute);
        mDialog.show();
        return true;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mMinute = minute;
        mHour = hourOfDay;
        persistString(getCurrentPickerTime());
        notifyChanged();
        setSummary(mDefaultSummary);
    }
}