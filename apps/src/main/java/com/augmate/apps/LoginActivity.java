package com.augmate.apps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.apps.common.ErrorPrompt;
import com.augmate.apps.common.FlowUtils;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.TouchResponseListener;
import com.augmate.apps.common.UserUtils;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.google.android.glass.touchpad.GestureDetector;

import static com.augmate.apps.common.FlowUtils.VIEWFLIPPER_TRANSITION_TIMEOUT;

public class LoginActivity extends BaseActivity {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FontHelper.updateFontForBrightness(
                (TextView) findViewById(R.id.lets_login)
                , (TextView) findViewById(R.id.tap_to_scan));

        TouchResponseListener responseListener = new TouchResponseListener(findViewById(R.id.touch));
        mGestureDetector = new GestureDetector(this)
                .setBaseListener(responseListener)
                .setScrollListener(responseListener)
                .setFingerListener(responseListener);
        flipper = ((ViewFlipper) findViewById(R.id.flipper));

        SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_prefs), MODE_PRIVATE);
        boolean animationsOn = prefs.getBoolean(getString(R.string.pref_animation_toggle), true);

        if (animationsOn) {
            flipper.setInAnimation(this, android.R.anim.slide_in_left);
            flipper.setOutAnimation(this, android.R.anim.slide_out_right);
            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
        } else {
            flipper.setInAnimation(this, R.anim.no_slide_anim);
            flipper.setOutAnimation(this, R.anim.no_slide_anim);
            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
        }

        flipper.setFlipInterval(VIEWFLIPPER_TRANSITION_TIMEOUT);
        flipper.setAutoStart(true);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onMotionEvent(event);
        return super.onGenericMotionEvent(event);
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
            } else if (UserUtils.isAUser(barcodeString)) {
                SoundHelper.success(this);
                String employeeName = UserUtils.getUserFromBarcode(barcodeString);
                UserUtils.setUser(employeeName);
                showConfirmation(getString(R.string.welcome, employeeName), ApplicationsActivity.class, null);
                finish();
            } else {
                //invalid code
                SoundHelper.error(this);
                showError(ErrorPrompt.SCAN_ERROR);
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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startScanner();
                }
            }, FlowUtils.SCALE_TIME);
        }
        return super.onKeyDown(keyCode, event);
    }
}
