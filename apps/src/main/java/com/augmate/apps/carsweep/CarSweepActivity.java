package com.augmate.apps.carsweep;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.augmate.sdk.data.PackageCarLoad;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;
import java.util.List;

public class CarSweepActivity extends RoboActivity implements IBluetoothScannerEvents {
    public static final int LOUD_TEXT_COLOR = 0xFFFF3300;
    public static final int QUIET_TEXT_COLOR = 0xFF444444;

    private IncomingConnector scanner = new IncomingConnector(this);
    CarLoadingDataStore carLoadingDataStore;

    public static final String EXTRA_CAR_LOAD = "EXTRA_CAR_LOAD";

    @InjectExtra(EXTRA_CAR_LOAD)
    String carLoadPosition;

    List<String> correctPackages = new ArrayList<>();
    List<String> wrongPackages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_sweep);

        carLoadingDataStore = new CarLoadingDataStore(this);
        scanner.start();

        // reset values to default
        ((TextView) findViewById(R.id.correct_package_car_id)).setText(carLoadPosition);

        findViewById(R.id.label_tap_to_dismiss).setVisibility(View.INVISIBLE);
        findViewById(R.id.wrong_package_car_id).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.label_misplaced_header)).setTextColor(QUIET_TEXT_COLOR);
        ((TextView) findViewById(R.id.wrong_package_counter)).setTextColor(QUIET_TEXT_COLOR);
        ((TextView) findViewById(R.id.wrong_package_counter)).setText("0");
        ((TextView) findViewById(R.id.correct_package_count)).setText("0");
    }

    private void processResultFromScan(String packageId) {
        // could be null or a car-id string
        String expectedLoadPosition = carLoadingDataStore.findLoadForTrackingNumberHack(packageId);

        // HACK: bypassing parse-object local datastore
        //PackageCarLoad loadForTrackingNumber = carLoadingDataStore.findLoadForTrackingNumber(packageId);

        if (expectedLoadPosition != null && expectedLoadPosition.equals(carLoadPosition)) {
            // success - package is inside the same car as us
            Log.debug("Package in correct car");
            onCorrectPackage(packageId);
        } else {
            // failure - package is not inside the correct car
            Log.debug("Package not in correct car");
            onMisplacedPackage(packageId);
        }
    }

    /**
     * Update correct-package stats UI
     *
     * @param packageId
     */
    private void onCorrectPackage(String packageId) {
        if (!correctPackages.contains(packageId)) {
            correctPackages.add(packageId);
        }

        SoundHelper.success(getBaseContext());

        // increment correct-package counter
        ((TextView) findViewById(R.id.correct_package_count)).setText("" + correctPackages.size());
    }

    /**
     * Update wrong-package stats UI
     */
    private void onMisplacedPackage(String packageId) {
        if (!wrongPackages.contains(packageId)) {
            wrongPackages.add(packageId);
        }

        SoundHelper.error(getBaseContext());

        // apply active color-scheme to counter and header
        ((TextView) findViewById(R.id.label_misplaced_header)).setTextColor(LOUD_TEXT_COLOR);
        ((TextView) findViewById(R.id.wrong_package_counter)).setTextColor(LOUD_TEXT_COLOR);

        // increment wrong-package counter
        ((TextView) findViewById(R.id.wrong_package_counter)).setText("" + wrongPackages.size());

        // show "tap to dismiss" label
        findViewById(R.id.label_tap_to_dismiss).setVisibility(View.VISIBLE);

        // show "wrong car" label
        findViewById(R.id.wrong_package_car_id).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // dismiss wrong-package message if it's up
            onTapToDismiss();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void onTapToDismiss() {
        Log.debug("User tapped to dismiss wrong-package msg");

        findViewById(R.id.label_tap_to_dismiss).setVisibility(View.INVISIBLE);
        findViewById(R.id.wrong_package_car_id).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.label_misplaced_header)).setTextColor(QUIET_TEXT_COLOR);
        ((TextView) findViewById(R.id.wrong_package_counter)).setTextColor(QUIET_TEXT_COLOR);

        SoundHelper.dismiss(getBaseContext());
    }

    @Override
    public void onBtScannerResult(String barcode) {
        processResultFromScan(barcode);
    }

    @Override
    public void onBtScannerSearching() {
        ((TextView) findViewById(R.id.scanner_connect_status)).setText("Waiting for scanner..");
        ((TextView) findViewById(R.id.scanner_connect_status)).setTextColor(0xFFFFFF00);
    }

    @Override
    public void onBtScannerConnected() {
        ((TextView) findViewById(R.id.scanner_connect_status)).setText("Scanner Connected");
        ((TextView) findViewById(R.id.scanner_connect_status)).setTextColor(0x6600FF00);
    }

    @Override
    public void onBtScannerDisconnected() {
        ((TextView) findViewById(R.id.scanner_connect_status)).setText("Scanner Lost");
        ((TextView) findViewById(R.id.scanner_connect_status)).setTextColor(0xFFFF0000);
    }
}
