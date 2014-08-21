package com.augmate.sample.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.augmate.sample.R;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.ScannerFragmentBase;

import static com.augmate.sample.common.FlowUtils.SCANNER_TIMEOUT;

public class ScannerActivity extends FragmentActivity implements ScannerFragmentBase.OnScannerResultListener {

    public static final String BARCODE = "barcodeString";
    public static final String EXITED = "EXITED";
    public static final String TIMEOUT = "TIMEOUT";

    public boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_scan);
        Log.debug("Created activity that uses barcode scanner");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!busy) {
                    busy = true;
                    timeOut();
                }
            }
        }, SCANNER_TIMEOUT);
    }

    @Override
    public void onBarcodeScanSuccess(String result) {
        if (!busy) {
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
        super.finish();
    }
}
