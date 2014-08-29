package com.augmate.sample.counter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import com.augmate.sample.R;
import com.augmate.sample.common.ErrorPrompt;
import com.augmate.sample.common.FlowUtils;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.TouchResponseListener;
import com.augmate.sample.common.UserUtils;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.google.android.glass.touchpad.GestureDetector;

public class CycleCountActivity extends BaseActivity {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cyclecount);

        TouchResponseListener responseListener = new TouchResponseListener(findViewById(R.id.touch));
        mGestureDetector = new GestureDetector(this)
                .setBaseListener(responseListener)
                .setScrollListener(responseListener)
                .setFingerListener(responseListener);
        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_prefs),MODE_PRIVATE);
        boolean animationsOn = prefs.getBoolean(getString(R.string.pref_animation_toggle),true);

        flipper.setFlipInterval(FlowUtils.VIEWFLIPPER_TRANSITION_TIMEOUT_LONG);
        if (animationsOn) {
            flipper.setInAnimation(this, android.R.anim.slide_in_left);
            flipper.setOutAnimation(this, android.R.anim.slide_out_right);
            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
        } else {
            flipper.setInAnimation(this, R.anim.no_slide_anim);
            flipper.setOutAnimation(this, R.anim.no_slide_anim);
            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
        }
        flipper.setAutoStart(true);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public void handlePromptReturn() {
        rescan();
    }

    @Override
    public void processBarcodeScanning(String barcodeString, boolean wasExited,
                                       boolean wasSuccessful, boolean wasTimedOut) {
        if (wasSuccessful) {
            Log.debug("Got barcode value=%s", barcodeString);
            if (wasExited) {
                SoundHelper.dismiss(this);
            } else {
                SoundHelper.success(this);
                BinModel model = new BinModel();
                model.setBinBarcode(barcodeString);
                model.setUser(UserUtils.getUser());
                showConfirmation(getString(R.string.bin_confirmed), RecordCountActivity.class, model);
            }
        } else {
            //generic error
            SoundHelper.error(this);
            if (wasTimedOut) {
                showError(ErrorPrompt.TIMEOUT_ERROR);
            } else {
                showError(ErrorPrompt.SCAN_ERROR);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            SoundHelper.tap(this);
            startScanner();
        }
        return super.onKeyDown(keyCode, event);
    }
}
