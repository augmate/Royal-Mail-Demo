package com.augmate.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.sample.common.FlowUtils;
import static com.augmate.sample.common.FlowUtils.*;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.UserUtils;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;

public class LoginActivity extends BaseActivity {
    boolean allowOneTap = true;
    ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        flipper.setInAnimation(this, android.R.anim.slide_in_left);
        flipper.setOutAnimation(this, android.R.anim.slide_out_right);

        flipper.setFlipInterval(TRANSITION_TIMEOUT);

        flipper.setAutoStart(true);

        flipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (flipper.getDisplayedChild() == flipper.getChildCount() - 1){
                    flipper.stopFlipping();
                }
            }
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    public void processBarcodeScanning(String barcodeString, boolean wasExited, boolean wasSuccessful) {
        findViewById(R.id.scan_response).setVisibility(View.VISIBLE);
        findViewById(R.id.tap_to_scan).setVisibility(View.GONE);

        ImageView responseImage = (ImageView) findViewById(R.id.response_image);
        TextView responseText = (TextView) findViewById(R.id.response_text);
        if (wasSuccessful) {
            Log.debug("Got barcode value=%s", barcodeString);
            if (wasExited) {
                SoundHelper.dismiss(this);
                findViewById(R.id.scan_response).setVisibility(View.GONE);
                findViewById(R.id.tap_to_scan).setVisibility(View.VISIBLE);
                allowOneTap = true;
            } else if (barcodeString.startsWith("user_")) {
                SoundHelper.success(this);
                String employeeName = barcodeString.replace("user_","");
                responseImage.setImageResource(android.R.drawable.ic_menu_add);
                responseText.setText(getString(R.string.welcome,employeeName));
                UserUtils.setUser(employeeName);
                goToApplications();
            } else {
                //invalid code
                SoundHelper.error(this);
                responseImage.setImageResource(android.R.drawable.ic_menu_camera);
                responseText.setText(R.string.invalid_scan);
                rescan();
            }
        } else {
            //generic error
            SoundHelper.error(this);
            responseImage.setImageResource(android.R.drawable.ic_menu_camera);
            responseText.setText(R.string.scan_error);
            rescan();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && allowOneTap) {
           SoundHelper.tap(this);
           allowOneTap = false;
           startScanner();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToApplications() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, ApplicationsActivity.class);
                startActivity(intent);
                finish();
            }
        }, TRANSITION_TIMEOUT);
    }
}
