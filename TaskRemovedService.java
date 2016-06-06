package tk.parmclee.starfootprint;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class TaskRemovedService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                CaptureFragment.sAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        CaptureFragment.alarmManager.cancel(intent);
    }
}
