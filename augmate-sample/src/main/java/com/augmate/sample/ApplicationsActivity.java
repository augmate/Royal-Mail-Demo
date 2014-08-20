package com.augmate.sample;

import android.content.Intent;
import android.os.Bundle;

import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sample.counter.CycleCountActivity;

public class ApplicationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        goToCycleCounter();
    }

    private void goToCycleCounter() {
        Intent intent = new Intent(this, CycleCountActivity.class);
        startActivity(intent);
        finish();
    }

}
