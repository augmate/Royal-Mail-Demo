package com.augmate.apps.nonretailtouching;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.ScannerFragmentBase;

import java.util.ArrayList;

public class NonRetailTouchActivity extends BaseActivity implements ScannerFragmentBase.OnScannerResultListener {

    private ArrayList<String> recordedBarcodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_retail_touch);
    }

    @Override
    public void onBarcodeScanSuccess(String result) {
        Log.debug("Got scanning result: [%s]", result);

        if(!recordedBarcodes.contains(result)) {
            recordedBarcodes.add(result);

            Log.debug("-> unique scanning result: [%s] (array size: %d)", result, recordedBarcodes.size());

            // we have a new unique barcode
            TextView nrtCounter = (TextView) findViewById(R.id.nrt_counter);
            nrtCounter.setText("" + recordedBarcodes.size());
            nrtCounter.setTextColor(0xFFFFFF00);
        }
    }
}
