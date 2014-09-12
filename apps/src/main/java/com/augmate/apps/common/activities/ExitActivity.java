package com.augmate.apps.common.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;

import com.augmate.apps.R;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.activities.BaseActivity;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.Timer;

/**
 * @author James Davis (Fuzz)
 */
public class ExitActivity extends BaseActivity {
    private int taps = 0;
    private GridLayout gridLayout;
    Handler handler = new Handler();
    final Runnable returnToApp = new Runnable() {
        @Override
        public void run() {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        gridLayout = ((GridLayout) findViewById(R.id.exitGridLayout));

        performTap();
    }

    private void performTap(){
        SoundHelper.tap(ExitActivity.this);
        handler.removeCallbacks(returnToApp);
        taps++;

        try {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(500);
            animation.setAnimationListener(animationListener);
            View view = gridLayout.getChildAt(taps - 1);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(animation);
        } catch (NullPointerException npe){ }
        handler.postDelayed(returnToApp, 1500);
    }

    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (taps >= 7){
                // exit app
                setResult(RESULT_OK);
                finish();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    };

    @Override
    protected GestureDetector createGestureDetector(Context context) {
        GestureDetector detector = new GestureDetector(context);

        detector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                boolean handled = false;
                if (gesture == Gesture.TWO_TAP) {
                    performTap();
                    handled = true;
                }
                return handled;
            }
        });

        return detector;
    }
}
