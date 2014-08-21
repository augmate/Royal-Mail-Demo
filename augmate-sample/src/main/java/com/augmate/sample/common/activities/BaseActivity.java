package com.augmate.sample.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.augmate.sample.common.FlowUtils;
import com.augmate.sample.scanner.ScannerActivity;
import com.augmate.sdk.logger.Log;

public class BaseActivity extends Activity {
    public static final int REQUEST_BARCODE_SCAN = 0x01;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // turn screen on when application is deployed (makes testing easier)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
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
        }, FlowUtils.TRANSITION_TIMEOUT);
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
            processBarcodeScanning(value, exited, resultCode == Activity.RESULT_OK);
        }
        super.onActivityResult(requestCode,resultCode, data);
    }

    public void processBarcodeScanning(String barcodeString, boolean wasExited, boolean wasSuccessful) {
        throw new RuntimeException("This method should be implemented if you need to scan");
    }
}
