package com.augmate.sdk.data;

import android.app.Activity;
import android.os.Bundle;

public class TestDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AugmateData(this).test();
    }
}
