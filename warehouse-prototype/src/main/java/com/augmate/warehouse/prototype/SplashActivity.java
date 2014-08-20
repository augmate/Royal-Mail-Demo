package com.augmate.warehouse.prototype;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.glass.touchpad.Gesture;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        }, 1000);
    }
}
