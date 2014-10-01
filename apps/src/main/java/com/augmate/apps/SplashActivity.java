package com.augmate.apps;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.activities.BaseActivity;
import roboguice.inject.InjectView;

import static com.augmate.apps.common.FlowUtils.*;

public class SplashActivity extends BaseActivity {

    @InjectView(R.id.augmate_text)
    TextView augmate_text;

    @InjectView(R.id.background)
    View background;

    @InjectView(R.id.logo_background)
    View logoBackground;

    @InjectView(R.id.augmate_logo)
    ImageView logoView;

    @InjectView(R.id.augmate_text)
    TextView logoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startAnimation();
        FontHelper.updateFontForBrightness(augmate_text);
    }

    private void goToApplicationList() {
        Intent intent = new Intent(this, ApplicationsActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAnimation() {
        final ViewPropertyAnimator animator = logoBackground.animate()
                .setDuration(SPLASH_ANIMATION_1_DURATION)
                .setStartDelay(SPLASH_ANIMATION_1_START_DELAY)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1.0f);
        background.animate()
                .setDuration(SPLASH_ANIMATION_1_DURATION)
                .setStartDelay(SPLASH_ANIMATION_1_START_DELAY)
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
        final ValueAnimator animLogo = ValueAnimator.ofInt(Color.WHITE, R.color.augmate_blue);
        animLogo.setDuration(SPLASH_ANIMATION_2_DURATION).setStartDelay(0);
        animLogo.setInterpolator(new AccelerateDecelerateInterpolator());
        animLogo.setEvaluator(new ArgbEvaluator());
        animLogo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logoView.setColorFilter((Integer) animation.getAnimatedValue());
                logoText.setTextColor((Integer) animation.getAnimatedValue());
            }
        });

        background.animate()
                .setStartDelay(SPLASH_ANIMATION_2_START_DELAY)
                .setDuration(SPLASH_ANIMATION_2_DURATION)
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
                                goToApplicationList();
                            }
                        },500);
                    }
                }).start();
    }

}
