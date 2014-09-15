package com.augmate.beacontest.app;

import android.app.Activity;
import android.content.*;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.BluetoothBarcodeScannerService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class BluetoothPairingActivity extends Activity {
    private BluetoothBarcodeScannerService bluetoothScannerService;
    private ServiceConnection bluetoothScannerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothScannerService = ((BluetoothBarcodeScannerService.ScannerBinder) service).getService();
            Log.debug("Service connected: " + name.flattenToShortString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.debug("Service disconnected: " + name.flattenToShortString());
            bluetoothScannerService = null;
        }
    };
    /**
     * Receive simple messages from BluetoothBarcodeScannerService
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED:
                    String barcode = intent.getStringExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_STRING);
                    onBarcodeScanned(barcode);
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connecting");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFFFF00);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
                    beaconDistanceMeasurer.startListening();
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
                    beaconDistanceMeasurer.stopListening();
                    break;

                default:
                    Log.warn("Unhandled intent action: %s", action);
            }
        }
    };

    private void onBarcodeScanned(String barcode) {
        int nearestTruckId = RegionProcessor.getNearestRegionId(beaconDistanceMeasurer.getLatestBeaconDistances());

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

    private int packageCountTruck1 = 0;
    private int packageCountTruck2 = 0;

    // captures keys before UI elements can steal them :)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            Log.debug("User requested scanner reconnect.");
            beaconDistanceMeasurer.stopListening();
            if (bluetoothScannerService != null)
                bluetoothScannerService.reconnect();
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

        Log.debug("Binding from scanner service.");
        bindService(new Intent(this, BluetoothBarcodeScannerService.class), bluetoothScannerConnection, Context.BIND_AUTO_CREATE);

        // register for scanner notifications
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED)); // barcode scanned
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING)); // can take a few seconds
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected
    }

    BeaconDistance beaconDistanceMeasurer = new BeaconDistance();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");

        beaconDistanceMeasurer.stopListening();

        // unregister from scanner notifications
        unregisterReceiver(receiver);

        unbindService(bluetoothScannerConnection);
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
}
