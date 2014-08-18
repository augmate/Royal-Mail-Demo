package com.augmate.sample.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.augmate.sample.R;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.ScannerFragmentBase;

public class ScannerActivity extends FragmentActivity implements ScannerFragmentBase.OnScannerResultListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_scan);
        Log.debug("Created activity that uses barcode scanner");
    }

    @Override
    public void onBarcodeScanSuccess(String result) {
        Log.debug("Got scanning result: [%s]", result);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("barcodeString", result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("exited", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
