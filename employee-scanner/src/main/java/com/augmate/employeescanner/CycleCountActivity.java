package com.augmate.employeescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.employeescanner.scanner.IDScannerActivity;
import com.augmate.sdk.logger.Log;

import java.util.List;

/**
 * Created by prem on 8/18/14.
 */
public class CycleCountActivity extends Activity {


    private static final int REQUEST_BOX_SCAN = 0x41;
    private static final int REQUEST_NUMBER_OF_BINS = 0x51;
    private static final int REQUEST_CONFIRM = 0x61;

    private Handler mHandler;
    private ViewFlipper mViewFlipper;
    private int flipCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setFlipper();
    }

    private void setFlipper() {
        flipCount = 0;
        setContentView(R.layout.activity_cyclecount);
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mHandler.postDelayed(flipRunnable, (long) (Constants.PROMPT_DURATION_MS * 2));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            launchScanActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void launchScanActivity() {
        Log.debug("Starting employee scanning activity..");
        Intent intent = new Intent(this, IDScannerActivity.class);
        startActivityForResult(intent, REQUEST_BOX_SCAN);
    }

    private Runnable flipRunnable = new Runnable() {
        @Override
        public void run() {
            flipCount++;
            if (flipCount == mViewFlipper.getChildCount()) {
                mHandler.removeCallbacks(this);
                return;
            }
            mViewFlipper.showNext();
            mHandler.postDelayed(this, (long) (Constants.PROMPT_DURATION_MS * 2));
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BOX_SCAN) {
            scanSuccessful("");
//            String value = data.getStringExtra(Constants.SCANNED_STRING);
//            if (value.startsWith("user_")) {
//                scanSuccessful(value);
//            } else {
//                scanError(SCANERROR.BIN_ERROR);
//            }
        } else if (requestCode == REQUEST_NUMBER_OF_BINS && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.debug("SpokenText: " + spokenText);
            handleNumberOfItems(spokenText);

        } else if (requestCode == REQUEST_CONFIRM && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String confirmation = results.get(0);
            Log.debug("Confirmation: " + confirmation);
            if ("yes".equalsIgnoreCase(confirmation)) {
                finish();
            }
        } else if (resultCode == Constants.TIMEOUT_RESULT) {
            scanError(SCANERROR.TIMEOUT_ERROR);
        } else {
            scanError(SCANERROR.SCAN_ERROR);
        }
    }

    private void scanSuccessful(final String binID) {
        setContentView(R.layout.bin_confirmed);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                askForNumberOfItems();
            }
        }, Constants.PROMPT_DURATION_MS * 3);
    }

    private void handleNumberOfItems(final String number) {
        try {
            final int numItems = Integer.parseInt(number);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    askForConfirmation(numItems);
                }
            }, Constants.PROMPT_DURATION_MS * 2);
        } catch (Throwable t) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    askForNumberOfItems();
                }
            }, Constants.PROMPT_DURATION_MS * 2);
        }
    }

    private void askForConfirmation(final int numItems) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.number_of_items_confirm, numItems));
        startActivityForResult(intent, REQUEST_CONFIRM);
    }

    private void askForNumberOfItems() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.number_of_items));
        startActivityForResult(intent, REQUEST_NUMBER_OF_BINS);
    }

    private void scanError(final SCANERROR error) {
        View errorLayout = LayoutInflater.from(this).inflate(R.layout.scan_error, null);
        TextView errorText = (TextView) errorLayout.findViewById(R.id.error_field);
        errorText.setText(error.error_msg);
        setContentView(errorLayout);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFlipper();
            }
        }, Constants.PROMPT_DURATION_MS * 3);
    }
}
