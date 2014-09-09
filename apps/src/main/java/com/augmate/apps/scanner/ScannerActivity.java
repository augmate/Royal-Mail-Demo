package com.augmate.apps.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.augmate.apps.R;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.ScannerFragmentBase;

import static com.augmate.apps.common.FlowUtils.SCANNER_TIMEOUT;

public class ScannerActivity extends BaseActivity implements ScannerFragmentBase.OnScannerResultListener {

    public static final String BARCODE = "barcodeString";
    public static final String EXITED = "EXITED";
    public static final String TIMEOUT = "TIMEOUT";

    public boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_scan);
        Log.debug("Created activity that uses barcode scanner");

        getHandler().postDelayed(timeoutRunnable,SCANNER_TIMEOUT);
    }

    Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!busy) {
                busy = true;
                timeOut();
            }
        }
    };

    @Override
    public void onBarcodeScanSuccess(String result) {
        if (!busy) {
            getHandler().removeCallbacks(timeoutRunnable);
            busy = true;
            Log.debug("Got scanning result: [%s]", result);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(BARCODE, result);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        getHandler().removeCallbacks(timeoutRunnable);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXITED, true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void timeOut() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TIMEOUT, true);
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public void finish() {
        getHandler().removeCallbacks(timeoutRunnable);
        super.finish();
    }
}
