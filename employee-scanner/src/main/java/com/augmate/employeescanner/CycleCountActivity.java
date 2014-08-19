package com.augmate.employeescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.employeescanner.scanner.IDScannerActivity;
import com.augmate.sdk.logger.Log;

/**
 * Created by prem on 8/18/14.
 */
public class CycleCountActivity extends Activity {

    private enum SCANERROR {
        SCAN_ERROR(R.string.scan_error),
        BIN_ERROR(R.string.bin_id_error),
        TIMEOUT_ERROR(R.string.timeout_error);

        int error_msg;

        SCANERROR(int msg) {
            this.error_msg = msg;
        }
    }


    private static final int REQUEST_BOX_SCAN = 0x41;

    private Handler mHandler;
    private ViewFlipper mViewFlipper;
    int flipCount = 0;

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
        if (requestCode == REQUEST_BOX_SCAN && resultCode == RESULT_OK) {
            String value = data.getStringExtra(Constants.SCANNED_STRING);
            if (value.startsWith("user_")) {
                scanSuccessful(value);
            } else {
                scanError(SCANERROR.BIN_ERROR);
            }
        } else if (resultCode == Constants.TIMEOUT_RESULT) {
            scanError(SCANERROR.TIMEOUT_ERROR);
        } else {
            scanError(SCANERROR.SCAN_ERROR);
        }
    }

    private void scanSuccessful(final String value) {

    }

    private void scanError(SCANERROR error) {
        TextView errorText = new TextView(this);
        errorText.setText(error.error_msg);
        setContentView(errorText);
        int padding = (int) getResources().getDimension(R.dimen.padding);
        errorText.setPadding(padding, padding, padding, padding);
        errorText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        errorText.setTextSize(getResources().getDimension(R.dimen.large_text));
        errorText.setTextColor(getResources().getColor(R.color.red));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFlipper();
            }
        }, Constants.PROMPT_DURATION_MS * 3);
    }
}
