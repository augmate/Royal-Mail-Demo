package com.augmate.sample.common;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * Created by premnirmal on 8/25/14.
 */
public class TouchResponseListener implements
        GestureDetector.BaseListener,
        GestureDetector.ScrollListener,
        GestureDetector.FingerListener {

    private final static float scaleUp = 1.65f;
    private final static float scaleDown = 0.6f;

    private final View touchView;
    private boolean isTouching = false;
    private float endX = 1f, endY = 1f;

    public TouchResponseListener(View touchView) {
        this.touchView = touchView;
    }

    private void gestureOperation(Gesture gesture) {
        float pivot = 0f;
        endX = 1f;
        endY = 1f;
        switch (gesture) {
            case TAP:
                endX = endY = scaleUp;
                pivot = touchView.getHeight() / 2;
                break;
            case SWIPE_LEFT:
                endX = endY = scaleUp;
                pivot = touchView.getHeight() / 2;
                break;
            case SWIPE_RIGHT:
                endX = endY = scaleDown;
                pivot = touchView.getHeight() / 2;
                break;
        }
        final Animation animation = new ScaleAnimation(1f, endX,
                1f, endY,
                touchView.getWidth(), pivot);
        animation.setFillAfter(true);
        animation.setDuration(FlowUtils.SCALE_TIME / 2);
        touchView.startAnimation(animation);
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
            gestureOperation(gesture);
        }
        return false;
    }

    @Override
    public boolean onScroll(float displacement, float delta, float velocity) {
        if (!isTouching) {
            isTouching = true;
            if (displacement < 0) {
                gestureOperation(Gesture.SWIPE_LEFT);
            } else if (displacement > 0) {
                gestureOperation(Gesture.SWIPE_RIGHT);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onFingerCountChanged(int previousCount, int currentCount) {
        if (currentCount == 0) {
            isTouching = false;
            final Animation animation = new ScaleAnimation(endX, 1f,
                    endY, 1f,
                    touchView.getWidth(), touchView.getHeight() / 2);
            endX = endY = 1f;
            animation.setFillAfter(true);
            animation.setDuration(FlowUtils.SCALE_TIME / 2);
            touchView.startAnimation(animation);
        }
    }

    private class AnimationResponse implements Animation.AnimationListener {

        final float startX, startY, pivot;

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
            Animation shrinkAnimation = new ScaleAnimation(startX, 1f,
                    startY, 1f,
                    touchView.getWidth(), pivot);
            shrinkAnimation.setFillAfter(true);
            shrinkAnimation.setDuration(FlowUtils.SCALE_TIME / 2);
            touchView.startAnimation(shrinkAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
