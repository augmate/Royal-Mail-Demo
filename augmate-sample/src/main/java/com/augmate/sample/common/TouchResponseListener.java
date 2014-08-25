package com.augmate.sample.common;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * Created by premnirmal on 8/25/14.
 */
public class TouchResponseListener implements GestureDetector.BaseListener {

    private View touchView;

    public TouchResponseListener(View touchView) {
        this.touchView = touchView;
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
            Animation animation = new ScaleAnimation(1f, 3f, 1f, 3f, 0f, touchView.getHeight() / 2);
            animation.setFillAfter(true);
            animation.setDuration(FlowUtils.SCALE_TIME/2);
            animation.setAnimationListener(animationListener);
            touchView.startAnimation(animation);
        }
        return false;
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Animation shrinkAnimation = new ScaleAnimation(3f, 1f, 3f, 1f, 0f, touchView.getHeight() / 2);
            shrinkAnimation.setFillAfter(true);
            shrinkAnimation.setDuration(FlowUtils.SCALE_TIME/2);
            touchView.startAnimation(shrinkAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

}
