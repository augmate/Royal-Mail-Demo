package com.augmate.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.sample.common.activities.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startAnimation();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAnimation() {
        View background = findViewById(R.id.background);
        View logoBackground = findViewById(R.id.logo_background);
        final ViewPropertyAnimator animator = logoBackground.animate()
                .setDuration(4500)
                .setStartDelay(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1.0f);
        background.animate()
                .setDuration(4500)
                .setStartDelay(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1.0f)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        endingAnimation();
                    }
                }).start();
    }

    private void endingAnimation() {
        final ImageView logoView = (ImageView) findViewById(R.id.augmate_logo);
        final TextView logoText = (TextView) findViewById(R.id.augmate_text);
        final ValueAnimator animLogo = ValueAnimator.ofInt(Color.WHITE, getResources().getColor(R.color.augmate_blue));
        animLogo.setDuration(2250).setStartDelay(0);
        animLogo.setInterpolator(new AccelerateDecelerateInterpolator());
        animLogo.setEvaluator(new ArgbEvaluator());
        animLogo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logoView.setColorFilter((Integer) animation.getAnimatedValue());
                logoText.setTextColor((Integer) animation.getAnimatedValue());
            }
        });

        View background = findViewById(R.id.background);
        background.animate()
                .setStartDelay(250)
                .setDuration(2250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0.0f)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        animLogo.start();
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                goToLogin();
                            }
                        },500);
                    }
                }).start();
    }

}
