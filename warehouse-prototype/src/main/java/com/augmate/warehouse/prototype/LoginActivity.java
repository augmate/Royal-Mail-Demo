package com.augmate.warehouse.prototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author James Davis (Fuzz)
 */
public class LoginActivity extends BaseActivity implements GestureDetector.BaseListener {
    private static int LOGIN_SCANNER_REQUEST = 8675309;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt);
        getGestureDetector().setBaseListener(this);
    }


    @Override
    public boolean onGesture(Gesture gesture) {
        boolean handled = false;

        if (gesture == Gesture.TAP){
            Intent scannerIntent = new Intent(this, ScannerActivity.class);
            startActivityForResult(scannerIntent, LOGIN_SCANNER_REQUEST);
            playSoundEffect(Sounds.TAP);
            handled = true;
        } else if (gesture == Gesture.SWIPE_DOWN){
            playSoundEffect(Sounds.DISMISSED);
            handled = true;
            finish();
        }

        return handled;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return getGestureDetector().onMotionEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_SCANNER_REQUEST){
            if (resultCode == RESULT_OK){

            } else if (resultCode == RESULT_CANCELED){

            } else if (resultCode == ScannerActivity.RESULT_ERROR){

            } else if (resultCode == ScannerActivity.RESULT_TIMED_OUT){

            }
        }
    }
}
