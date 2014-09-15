package com.augmate.texturetest.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


public class MainActivity extends Activity {

    protected Handler handler;
    protected GestureDetector mGestureDetector;

    protected final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.SWIPE_UP) {
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_startup);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        handler = new Handler();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }

   @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            launchScanActivity();
        }
        return super.onKeyDown(keyCode, event);
    }


    private void launchScanActivity() {
        Log.debug("Starting TextureScannerActivity activity..");
        Intent intent = new Intent(this, TextureScannerActivity.class);
        startActivity(intent);
        //finish();
    }
}
