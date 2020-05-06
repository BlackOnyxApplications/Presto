package com.black_onyx.Presto.activity;

import android.content.Intent;
import android.os.Bundle;
import com.black_onyx.Presto.R;
import com.black_onyx.Presto.app.Activity;
import com.black_onyx.Presto.util.AppUtils;

import android.os.Handler;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        slideSplashView();
        boolean isFirstTime = AppUtils.getDefaultPreferences(this).getBoolean("introduction_shown", false);
        if(isFirstTime){
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
    protected void slideSplashView()
    {
        findViewById(R.id.layout_welcome_page_1_splash_image)
                .setAnimation(AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom_centered));
        findViewById(R.id.layout_welcome_page_1_details)
                .setAnimation(AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom));
    }
}
