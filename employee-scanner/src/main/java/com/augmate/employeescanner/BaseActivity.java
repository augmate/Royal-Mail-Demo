package com.augmate.employeescanner;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * Created by premnirmal on 8/19/14.
 */
public abstract class BaseActivity extends Activity {

    protected Handler handler;
    protected IEmployeeBin employeeBin = MockEmployeeBin.getInstance();

    // for debugging
    protected GestureDetector mGestureDetector;
    protected final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.SWIPE_UP) {
                onSwipeUp();
                return true;
            } else {
                return false;
            }
        }
    };

    protected abstract void onSwipeUp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        handler = new Handler();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    // ==============
}
