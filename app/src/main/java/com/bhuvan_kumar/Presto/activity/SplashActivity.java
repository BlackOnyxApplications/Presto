package com.bhuvan_kumar.Presto.activity;

import android.content.Intent;
import android.os.Bundle;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.util.AppUtils;

import android.os.Handler;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        boolean isFirstTIme = AppUtils.getDefaultPreferences(this).getBoolean("introduction_shown", false);
        if(isFirstTIme){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent homeIntent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                }
            }, 1500);
        }
    }
}
