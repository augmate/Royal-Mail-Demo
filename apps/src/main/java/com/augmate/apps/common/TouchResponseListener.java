package com.augmate.apps.common;

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

    private static final float scaleUp = 1.65f;
    private static final float scaleDown = 0.6f;
    private static final int FORWARDS = 1;
    private static final int BACKWARDS = -1;

    private final View touchView;

    private float endX = 1f, endY = 1f;
    private int swipeDirection = 0; // -1 = backwards, +1 = forwards

    public TouchResponseListener(View touchView) {
        this.touchView = touchView;
    }

    private void gestureOperation(Gesture gesture) {
        float pivot = 0f;
        float newEndX = 1f, newEndY = 1f;
        switch (gesture) {
            case TAP:
            case SWIPE_LEFT:
                newEndX = newEndY = scaleUp;
                pivot = touchView.getHeight() / 2;
                break;
            case SWIPE_RIGHT:
                newEndX = newEndY = scaleDown;
                pivot = touchView.getHeight() / 2;
                break;
        }
        final Animation animation = new ScaleAnimation(endX, newEndX,
                endY, newEndY,
                touchView.getWidth(), pivot);
        endX = newEndX;
        endY = newEndY;
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

    private boolean canSwipeBackwards() {
        return swipeDirection == 0 || swipeDirection == FORWARDS;
    }

    private boolean canSwipeForwards() {
        return swipeDirection == 0 || swipeDirection == BACKWARDS;
    }

    @Override
    public boolean onScroll(float displacement, float delta, float velocity) {
        if (displacement < 0 && canSwipeBackwards()) {
            swipeDirection = BACKWARDS;
            gestureOperation(Gesture.SWIPE_LEFT);
            return true;
        } else if (displacement > 0 && canSwipeForwards()) {
            swipeDirection = FORWARDS;
            gestureOperation(Gesture.SWIPE_RIGHT);
            return true;
        }
        return false;
    }

    @Override
    public void onFingerCountChanged(int previousCount, int currentCount) {
        if (currentCount == 0) {
            swipeDirection = 0;
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
