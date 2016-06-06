package tk.parmclee.starfootprint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    final static int FLAGS =
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(FLAGS);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().stopService(AlarmReceiver.sActivityIntent); // stop idle service
        getWindow().addFlags(FLAGS);
        Log.d("StarLog", "Activity created");
        File file = new File(getIntent().getStringExtra("storage"));
        boolean firstPicture = getIntent().getBooleanExtra("firstPicture", false);
        setContentView(new AutoCapture(getApplicationContext(), firstPicture, file, this));
    }
}
