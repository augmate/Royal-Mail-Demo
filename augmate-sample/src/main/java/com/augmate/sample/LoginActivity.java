package com.augmate.sample;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.ViewFlipper;

import com.augmate.sample.common.ErrorPrompt;
import com.augmate.sample.common.FlowUtils;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.TouchResponseListener;
import com.augmate.sample.common.UserUtils;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.google.android.glass.touchpad.GestureDetector;

import static com.augmate.sample.common.FlowUtils.TRANSITION_TIMEOUT;

public class LoginActivity extends BaseActivity {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mGestureDetector = new GestureDetector(this).setBaseListener(new TouchResponseListener(findViewById(R.id.touch)));
        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        flipper.setInAnimation(this, android.R.anim.slide_in_left);
        flipper.setOutAnimation(this, android.R.anim.slide_out_right);

        flipper.setFlipInterval(TRANSITION_TIMEOUT);

        flipper.setAutoStart(true);

        flipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (flipper.getDisplayedChild() == flipper.getChildCount() - 1){
                    flipper.stopFlipping();
                }
            }
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
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
            } else if (barcodeString.startsWith("user_")) {
                SoundHelper.success(this);
                String employeeName = barcodeString.replace("user_","");
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
