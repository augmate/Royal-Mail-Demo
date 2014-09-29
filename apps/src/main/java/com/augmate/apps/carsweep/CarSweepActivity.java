package com.augmate.apps.carsweep;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.augmate.apps.R;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;

import java.util.HashMap;
import java.util.Map;

public class CarSweepActivity extends BaseActivity implements IBluetoothScannerEvents {
    private IncomingConnector scanner = new IncomingConnector(this);

    class PackageDetails {
        public String CarId;
        public boolean Spotted = false;
    }

    private Map<String, PackageDetails> packageLoadout = new HashMap<>();
    private String currentCarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_sweep);

        scanner.start();
    }

    private void processResultFromScan(String result) {
        PackageDetails packageDetails = packageLoadout.get(result);
        if(packageDetails == null) {
            // barcode not recognized
            Log.debug("Barcode doesn't belong to a known package");
            return;
        }

        if(packageDetails.CarId == currentCarId) {
            // success - package is inside the same car we are
            Log.debug("Package in correct car");
            SoundHelper.success(getBaseContext());
        } else {
            // failure - package is not inside the correct car
            Log.debug("Package not in correct car");
            SoundHelper.error(getBaseContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // reset app
            Log.debug("User requested counter reset");
            SoundHelper.dismiss(getBaseContext());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBtScannerResult(String barcode) {
        processResultFromScan(barcode);
    }

    @Override
    public void onBtScannerConnecting() {
    }

    @Override
    public void onBtScannerConnected() {
    }

    @Override
    public void onBtScannerDisconnected() {
    }
}
