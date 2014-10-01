package com.augmate.sdk.scanner.bluetooth.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.R;

public class PairActivity extends Activity {
    public static final int RESULT_CODE_READ_CONFIGURATION = 5001;
    public static final int REQUEST_CODE_BOND_DEVICE = 5002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        Log.start(this);
        startConfigurationRead();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CODE_READ_CONFIGURATION:
                if (resultCode == RESULT_OK) {
                    afterConfigurationRead(data);
                } else {
                    afterConfigurationReadCanceled();
                }
                break;

            case REQUEST_CODE_BOND_DEVICE:
                if (resultCode == RESULT_OK) {
                    afterDevicePaired();
                } else {
                    afterDevicePairingCanceled();
                }
                break;
        }
    }

    private void startConfigurationRead() {
        // read barcode with mac-address of device we want to pair
        startActivityForResult(new Intent(this, ConfigReadingActivity.class), RESULT_CODE_READ_CONFIGURATION);
    }

    private void afterConfigurationRead(Intent data) {
        Log.debug("Configuration read");
        String barcode = data.getStringExtra("barcode");

        if (barcode == null || !barcode.contains(":")) {
            Log.debug("Read invalid mac address: [%s]. Restarting config read..", barcode);
            // TODO: create custom configuration read screen with mac-address validation
            startConfigurationRead();
        } else {
            startDevicePairing(barcode);
        }
    }


    private void afterConfigurationReadCanceled() {
        Log.debug("Configuration read canceled");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Configuration Canceled");
    }


    private void startDevicePairing(String barcode) {
        Log.debug("Pairing with mac address: [%s]", barcode);
        startActivityForResult(new Intent(this, DeviceBondingActivity.class).putExtra("address", barcode), REQUEST_CODE_BOND_DEVICE);
    }

    private void afterDevicePaired() {
        Log.debug("Pairing completed");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Configured");
    }

    private void afterDevicePairingCanceled() {
        Log.debug("Pairing canceled");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Configuration Canceled");
    }
}
