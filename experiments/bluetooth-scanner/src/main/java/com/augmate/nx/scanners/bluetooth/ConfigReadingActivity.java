package com.augmate.nx.scanners.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import com.augmate.sdk.scanner.IScannerResultListener;

public class ConfigReadingActivity extends Activity implements IScannerResultListener {
    @Override
    public void onBarcodeScanSuccess(String result) {
        setResult(RESULT_OK, new Intent().putExtra("barcode", result));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_scanner);
    }
}
