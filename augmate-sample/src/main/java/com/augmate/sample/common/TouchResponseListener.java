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
        Animation animation = null;
        float pivot = 0f;
        float endX = 0f, endY = 0f;
        switch (gesture) {
            case TAP:
                endX = endY = 3f;
                pivot = touchView.getHeight() / 2;
                break;
            case SWIPE_LEFT:
            case SWIPE_DOWN:
                endX = endY = 7f;
                pivot = 0f;
                break;
            case SWIPE_RIGHT:
            case SWIPE_UP:
                endX = endY = 7f;
                pivot = touchView.getHeight();
                break;
        }
        animation = new ScaleAnimation(1f, endX, 1f, endY, 0f, pivot);
        animation.setFillAfter(true);
        animation.setDuration(FlowUtils.SCALE_TIME / 2);
        animation.setAnimationListener(new AnimationResponse(endX, endY, pivot));
        touchView.startAnimation(animation);
        return false;
    }

    private class AnimationResponse implements Animation.AnimationListener {

        float startX, startY, pivot;

        private AnimationResponse(float startX, float startY, float pivot) {
            this.startX = startX;
            this.startY = startY;
            this.pivot = pivot;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Animation shrinkAnimation = new ScaleAnimation(startX, 1f, startY, 1f, 0f, pivot);
            shrinkAnimation.setFillAfter(true);
            shrinkAnimation.setDuration(FlowUtils.SCALE_TIME / 2);
            touchView.startAnimation(shrinkAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
