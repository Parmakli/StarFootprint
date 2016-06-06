package tk.parmclee.starfootprint;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());

        NumberPickerPreference intervalPreference = new NumberPickerPreference(getContext());
        intervalPreference.setKey("interval");
        intervalPreference.setDefaultValue(10);
        intervalPreference.setTitle(getString(R.string.interval));
        screen.addPreference(intervalPreference);
        intervalPreference.setSummary(getString(R.string.current_interval));

        TimePickerPreference dawnPreference = new TimePickerPreference(getContext());
        dawnPreference.setKey("dawn");
        dawnPreference.setDefaultValue("06:00");
        dawnPreference.setTitle(getString(R.string.dawn_time));
        screen.addPreference(dawnPreference);
        dawnPreference.setSummary(getString(R.string.dawn_current));

        Preference storagePreference = new Preference(getContext());
        storagePreference.setTitle(R.string.open);
        final Uri storageUri = ((CaptureActivity) getActivity()).getStorageUri();
        final Intent viewPictureIntent = new Intent(Intent.ACTION_VIEW,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        viewPictureIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        storagePreference.setIntent(viewPictureIntent);
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File file = new File(storageUri.getPath());
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), getString(R.string.cant_open),
                            Toast.LENGTH_SHORT).show();
                    startActivity(viewPictureIntent);
                    return true;
                }
                return false;
            }
        });
        screen.addPreference(storagePreference);

        setPreferenceScreen(screen);
        addPreferencesFromResource(R.xml.chkbx);
        addPreferencesFromResource(R.xml.radiobtn);
    }
}
