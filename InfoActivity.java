package tk.parmclee.starfootprint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowHomeEnabled(true);

    }
}
