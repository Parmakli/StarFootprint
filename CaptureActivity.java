package tk.parmclee.starfootprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        FragmentPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        if (viewPager != null) {
            viewPager.setAdapter(adapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        TabLayout.Tab tabCapture = tabLayout.getTabAt(0);
        tabCapture.setIcon(R.drawable.ic_camera_enhance_white_48dp)
                .setContentDescription(R.string.capture);
        TabLayout.Tab tabSettings = tabLayout.getTabAt(1);
        tabSettings.setIcon(R.drawable.ic_settings_white_48dp);

        Intent onTaskRemovedIntent = new Intent(getApplicationContext(), TaskRemovedService.class);
        startService(onTaskRemovedIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getResources().getString(R.string.about))
                .setIcon(R.drawable.ic_help_outline_white_48dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), InfoActivity.class));
        return true;
    }

    Uri getStorageUri() {
        File storagePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        } else storagePath = getApplicationContext().getFilesDir();
        storagePath = new File(storagePath.getAbsolutePath() + "/StarFootprints");
        storagePath.mkdirs();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String pic = preferences.getString("picture", null);
        return Uri.parse(storagePath.getAbsolutePath() + "/" + pic);
    }

    class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CaptureFragment.newInstance();
            } else return SettingsFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}