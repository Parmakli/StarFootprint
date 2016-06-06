package tk.parmclee.starfootprint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    static Intent sActivityIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String storage = intent.getStringExtra("storage");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long dawnMillis = intent.getLongExtra("dawnMillis", 0);
        long intervalMillis = intent.getLongExtra("intervalMillis", 10 * 60 * 1000);

        if (System.currentTimeMillis() + intervalMillis < dawnMillis) {
            //setting next alarm
            Intent nextIteration = new Intent(intent);
            nextIteration.putExtra("firstPicture", false);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, nextIteration,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            long nextTime = SystemClock.elapsedRealtime() + intervalMillis;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pIntent);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextTime, pIntent);
            } else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pIntent);
        } else CaptureFragment.isRunning = false; //only for CaptureFragment

        boolean firstPicture = intent.getBooleanExtra("firstPicture", false);
        sActivityIntent = new Intent(context, StartActivityService.class);
        sActivityIntent.putExtra("firstPicture", firstPicture);
        sActivityIntent.putExtra("storage", storage);
        startWakefulService(context, sActivityIntent); //starting Activity through Service
    }
}
