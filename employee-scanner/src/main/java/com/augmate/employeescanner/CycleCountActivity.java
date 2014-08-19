package com.augmate.employeescanner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ViewFlipper;

/**
 * Created by prem on 8/18/14.
 */
public class CycleCountActivity extends Activity {

    private static final int REQUEST_BOX_SCAN = 0x41;

    private ViewFlipper mViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cyclecount);
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
    }

}
