package com.augmate.employeescanner.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.augmate.employeescanner.Constants;
import com.augmate.employeescanner.R;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.ScannerFragmentBase;

/**
 * Created by premnirmal on 8/18/14.
 */
public class IDScannerActivity extends FragmentActivity implements ScannerFragmentBase.OnScannerResultListener {

    private static final long TIMEOUT_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idscan);
        Log.debug("Created activity that uses barcode scanner");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(Constants.TIMEOUT_RESULT, new Intent());
                finish();
            }
        }, TIMEOUT_MS);
    }

    @Override
    public void onBarcodeScanSuccess(String result) {
        Log.debug("Got scanning result: [%s]", result);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.SCANNED_STRING, result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
