package tk.parmclee.starfootprint;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class StartActivityService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        Intent activityIntent = new Intent(getApplicationContext(), PreviewActivity.class);
        activityIntent.putExtras(extras);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
