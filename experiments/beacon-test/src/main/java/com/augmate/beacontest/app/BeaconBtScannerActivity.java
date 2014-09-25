package com.augmate.beacontest.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.beacons.BeaconRegion;
import com.augmate.sdk.beacons.RegionProcessor;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeaconBtScannerActivity extends Activity implements IBluetoothScannerEvents {
    private IncomingConnector incomingConnector = new IncomingConnector(this);
    private BeaconDistance beaconDistanceMeasurer = new BeaconDistance();

    // captures keys before UI elements can steal them :)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            Log.debug("User requested scanner reconnect.");
            beaconDistanceMeasurer.stopListening();
            incomingConnector.reconnect();

            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    // NOTE: bindService/unbindService can be moved to onResume/onPause to disconnect from the scanner on app sleep
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        Log.start(this);

        // prep beacon ranger but don't start it until we have a scanner bonded and connected.
        // ranging seems to interfere with ordinary bluetooth connections :(
        beaconDistanceMeasurer.configureFromContext(this);
        incomingConnector.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");

        beaconDistanceMeasurer.stopListening();
        incomingConnector.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug("Resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing");
    }

    private int packageCountTruck1 = 0;
    private int packageCountTruck2 = 0;

    @Override
    public void onBtScannerResult(String barcode) {
        List<BeaconInfo> latestBeaconDistances = beaconDistanceMeasurer.getLatestBeaconDistances();
        // region-processor must take a mutable region-list, as it will get trimmed due to lack of beaons
        RegionProcessor regionProcessor = new RegionProcessor(new ArrayList<>(Arrays.asList(
                new BeaconRegion(2, 4).setRegionId(1),
                new BeaconRegion(3, 5).setRegionId(2)
        )));

        // may return -1 if there are no regions detected at all
        int nearestTruckId = regionProcessor.getNearestRegionId(latestBeaconDistances);

        ((TextView) findViewById(R.id.barcodeScannerResults)).setText(
                String.format("Barcode: [%s] near Truck %d\nat %s", barcode, nearestTruckId, DateTime.now().toString(DateTimeFormat.mediumDateTime()))
        );

        Log.debug("Received barcode value: [%s] in region %d", barcode, nearestTruckId);

        // reset trucks
        ((ImageView) findViewById(R.id.beaconRegionTruck1)).setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);
        ((ImageView) findViewById(R.id.beaconRegionTruck2)).setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);

        // update count and highlight the nearest truck
        if (nearestTruckId == 1) {
            ((TextView) findViewById(R.id.truck1LoadCounter)).setText("" + ++packageCountTruck1);
            ((ImageView) findViewById(R.id.beaconRegionTruck1)).setColorFilter(0xFF78B3EC, PorterDuff.Mode.SRC_IN);
        }

        if (nearestTruckId == 2) {
            ((TextView) findViewById(R.id.truck2LoadCounter)).setText("" + ++packageCountTruck2);
            ((ImageView) findViewById(R.id.beaconRegionTruck2)).setColorFilter(0xFF78B3EC, PorterDuff.Mode.SRC_IN);
        }

        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
    }

    @Override
    public void onBtScannerConnecting() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connecting");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFFFF00);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerConnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        beaconDistanceMeasurer.startListening();
    }

    @Override
    public void onBtScannerDisconnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        beaconDistanceMeasurer.stopListening();
    }
}
