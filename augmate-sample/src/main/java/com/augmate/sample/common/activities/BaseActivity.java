package com.augmate.sample.common.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.widget.ViewFlipper;

import com.augmate.sample.R;
import com.augmate.sample.common.ErrorPrompt;
import com.augmate.sample.scanner.ScannerActivity;
import com.augmate.sdk.logger.Log;

import java.io.Serializable;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.augmate.sample.common.FlowUtils.TRANSITION_TIMEOUT;

public class BaseActivity extends Activity {
    public static final int REQUEST_BARCODE_SCAN = 0x01;
    public static final int REQUEST_PROMPT = 0x02;
    protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("fonts/GothamNarrow-Book.otf", R.attr.fontPath);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void rescan() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startScanner();
            }
        }, TRANSITION_TIMEOUT);
    }

    public void startScanner() {
        Log.debug("Starting scanner activity..");
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, REQUEST_BARCODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BARCODE_SCAN) {
            String value = data.getStringExtra(ScannerActivity.BARCODE);
            boolean exited = data.getBooleanExtra(ScannerActivity.EXITED,false);
            boolean timeout = data.getBooleanExtra(ScannerActivity.TIMEOUT,false);
            processBarcodeScanning(value, exited, resultCode == Activity.RESULT_OK, timeout);
        } else if (requestCode == REQUEST_PROMPT) {
            if (resultCode == RESULT_CANCELED) {
                handlePromptReturn();
            }
        }
        super.onActivityResult(requestCode,resultCode, data);
    }

    public void processBarcodeScanning(String barcodeString, boolean wasExited,
                                       boolean wasSuccessful, boolean wasTimedOut) {
        throw new RuntimeException("This method should be implemented if you need to scan");
    }

    public void handlePromptReturn() {
        throw new RuntimeException("This method should be implemented if you need to scan");
    }

    protected Animation.AnimationListener getAnimationListener(final ViewFlipper flipper){
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (flipper != null) {
                    if (flipper.getDisplayedChild() == flipper.getChildCount() - 1) {
                        flipper.stopFlipping();
                    }
                }
            }
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        };
    }

    public void showError(ErrorPrompt prompt) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.ERROR, true);
        intent.putExtra(MessageActivity.MESSAGE, prompt.getString());
        startActivityForResult(intent, REQUEST_PROMPT);
    }

    public void showConfirmation(String confirmationText,Class clazz, Serializable data) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.MESSAGE, confirmationText);
        if (clazz != null) {
            intent.putExtra(MessageActivity.CLASS, clazz.getName());
        }
        intent.putExtra(MessageActivity.DATA, data);
        startActivityForResult(intent, REQUEST_PROMPT);
    }
}
