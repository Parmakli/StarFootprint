package tk.parmclee.starfootprint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CaptureFragment extends Fragment {

    CameraPreview mPreview;
    Switch mBtnPreview;
    ToggleButton mBtnStart;
    static AlarmManager alarmManager;
    CountDownTimer mTimer;
    static Intent sAlarmIntent;
    static boolean isRunning;
    TextView mStatus;
    static String sPictureExtension;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_capture, container, false);
        View.OnClickListener listener = new ViewClickListener();

        mBtnStart = (ToggleButton) mRootView.findViewById(R.id.btnStart);
        mBtnStart.setChecked(false);
        mBtnStart.setOnClickListener(listener);
        mBtnPreview = (Switch) mRootView.findViewById(R.id.btnPreview);
        mBtnPreview.setOnClickListener(listener);
        mStatus = (TextView) mRootView.findViewById(R.id.statusText);

        RelativeLayout layout = (RelativeLayout) mRootView.findViewById(R.id.previewContainer);
        mPreview = new CameraPreview(getContext(), getActivity());
        if (layout != null) layout.addView(mPreview);

        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        sAlarmIntent = new Intent(getContext(), AlarmReceiver.class);

        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRunning) stopProcess();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreview.releaseCamera();
        if (mTimer != null) mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBtnPreview.isChecked()) {
            mPreview.initCameraInstance();
            mPreview.setVisibility(View.VISIBLE);
        } else mPreview.setVisibility(View.INVISIBLE);
        if (isRunning) {
            mBtnStart.setChecked(true);
            mStatus.setText(R.string.processing);
        } else {
            mBtnStart.setChecked(false);
            mStatus.setText(R.string.status_waiting);
        }
    }

    public static Fragment newInstance() {
        return new CaptureFragment();
    }

    static void saveBitmap(Bitmap bitmap, File picture) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(picture);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart:
                    if (((ToggleButton) v).isChecked()) {
                        startProcess();
                        isRunning = true;
                    } else {
                        stopProcess();
                        mStatus.setText(R.string.status_waiting);
                    }
                    break;
                case R.id.btnPreview:
                    if (((Switch) v).isChecked()) {
                        mPreview.hideActionBar();
                        mPreview.setVisibility(View.VISIBLE);
                        mPreview.startCameraPreview(mPreview.getWidth(), mPreview.getHeight());
                    } else {
                        mPreview.releaseCamera();
                        mPreview.setVisibility(View.INVISIBLE);
                        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        if (bar != null) bar.show();
                    }
                    break;
                default:
            }
        }

        void startProcess() {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            int interval = preferences.getInt("interval", 10);
            long intervalMillis = 1000 * 60 * interval;
            startCountdown(intervalMillis);

            String dawn = preferences.getString("dawn", "06:00");
            int hour = Integer.parseInt(dawn.split(":")[0]);
            int minute = Integer.parseInt(dawn.split(":")[1]);
            Calendar dawnTime = Calendar.getInstance(); //sets the down time same as current
            dawnTime.set(Calendar.HOUR_OF_DAY, hour); // sets hours from preferences
            dawnTime.set(Calendar.MINUTE, minute); // sets minutes from preferences
            dawnTime.set(Calendar.SECOND, 0); // sets seconds
            long currentTime = System.currentTimeMillis();
            if (currentTime > dawnTime.getTimeInMillis()) {// if current time more then dawn time
                dawnTime.roll(Calendar.DAY_OF_YEAR, 1);   // add one day to dawn time
            }
            long dawnMillis = dawnTime.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyLLddHHmm", Locale.ENGLISH);
            String now = sdf.format(Calendar.getInstance().getTime());

            if ("0".equals(preferences.getString("cam_parameters", "0")))
                sPictureExtension = ".jpg";
            else sPictureExtension = ".png";

            String name = "PIC_" + now + sPictureExtension;
            preferences.edit().putString("picture", name).apply();
            String storagePath = ((CaptureActivity) getActivity()).getStorageUri().getPath();

            sAlarmIntent.putExtra("storage", storagePath);
            sAlarmIntent.putExtra("intervalMillis", intervalMillis);
            sAlarmIntent.putExtra("dawnMillis", dawnMillis);
            sAlarmIntent.putExtra("firstPicture", true);
            AutoCapture.sCounter = 1;
            PendingIntent pIntent = PendingIntent.getBroadcast(getContext(), 0, sAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //setting first alarm
            long nextTime = SystemClock.elapsedRealtime() + intervalMillis;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pIntent);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextTime, pIntent);
            } else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pIntent);
        }
    }

    private void startCountdown(long interval) {
        mTimer = new CountDownTimer(interval - 200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / (60 * 1000);
                long seconds = millisUntilFinished / 1000 - minutes * 60;
                String statusText = getString(R.string.status_starting) + " " +
                        String.format(Locale.ENGLISH, "%02d" + ":" + "%02d", minutes, seconds);
                mStatus.setText(statusText);
            }

            @Override
            public void onFinish() {
                mStatus.setText(R.string.processing);
                if (mBtnPreview.isChecked()) mBtnPreview.performClick();
            }
        }.start();
    }

    void stopProcess() {
        isRunning = false;
        PendingIntent intent = PendingIntent.getBroadcast(getContext(), 0, sAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(intent);
        if (mTimer != null) mTimer.cancel();
    }
}
