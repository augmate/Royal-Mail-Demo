package com.augmate.sample;

import android.content.Intent;
import android.os.Bundle;

import static com.augmate.sample.common.FlowUtils.*;
import com.augmate.sample.common.activities.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToLogin();
            }
        }, TRANSITION_TIMEOUT);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
