package com.augmate.apps.carloading;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.apps.R;
import com.augmate.apps.common.ErrorPrompt;
import com.augmate.apps.common.FlowUtils;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.TouchResponseListener;
import com.augmate.apps.common.activities.BaseActivity;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class CarLoadingActivity extends BaseActivity {

    private GestureDetector mGestureDetector;

    @Inject
    private SharedPreferences prefs;

    @InjectView(R.id.flipper)
    ViewFlipper flipper;

    @InjectView(R.id.lets_load_trucks)
    TextView lets_load_trucks;

    @InjectView(R.id.tap_to_scan)
    TextView tap_to_scan;

    @InjectView(R.id.background)
    View background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_truckloading);

        FontHelper.updateFontForBrightness(lets_load_trucks, tap_to_scan);

        TouchResponseListener responseListener = new TouchResponseListener(background);
        mGestureDetector = new GestureDetector(this)
                .setBaseListener(responseListener)
                .setScrollListener(responseListener)
                .setFingerListener(responseListener);

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
        if (savedInstanceState == null || savedInstanceState.getBoolean("LoadAnimation",true)) {
            flipper.setAutoStart(true);
        } else {
            flipper.setDisplayedChild(2);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("LoadAnimation",false);
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
            if (wasExited) {
                SoundHelper.dismiss(this);
            } else {
                SoundHelper.success(this);
                showConfirmation(getString(R.string.package_verified), null, null);
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
