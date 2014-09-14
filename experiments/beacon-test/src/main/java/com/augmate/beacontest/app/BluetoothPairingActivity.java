package com.augmate.beacontest.app;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.BluetoothBarcodeScannerService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
                    Log.debug("Received barcode value: [%s]", barcode);
                    int nearestBeaconMinorId = getNearestBeacon();
                    ((TextView) findViewById(R.id.barcodeScannerResults)).setText(
                            String.format("Barcode: [%s] near #%d\nat %s", barcode, nearestBeaconMinorId, DateTime.now().toString(DateTimeFormat.mediumDateTime()))
                    );
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
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
                    break;

                default:
                    Log.warn("Unhandled intent action: %s", action);
            }
        }
    };

    private int getNearestBeacon() {
        List<BeaconInfo> beaconDistances = beaconDistanceMeasurer.getLatestBeaconDistances();

        Log.debug("We know about %d beacons within the area", beaconDistances.size());

        for (BeaconInfo beacon : beaconDistances) {
            Log.debug("  beacon %d = mean: %.2f / 80th percentile: %.2f", beacon.minor, beacon.distance, beacon.weightedAvgDistance);
        }

        if(beaconDistances.size() > 0) {
            Collections.sort(beaconDistances, new Comparator<BeaconInfo>() {
                @Override
                public int compare(BeaconInfo b1, BeaconInfo b2) {
                    return (int) (b1.weightedAvgDistance * 500 - b2.weightedAvgDistance * 500);
                }
            });

            BeaconInfo nearestBeacon = beaconDistances.get(0);
            Log.info("-> Nearest beacon: #%d at %.2f units away", nearestBeacon.minor, nearestBeacon.weightedAvgDistance);
            return nearestBeacon.minor;
        }

        return -1;
    }

    // captures keys before UI elements can steal them :)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.debug("User requested scanner reconnect.");
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

        ((TextView) findViewById(R.id.barcodeScannerResults)).setMovementMethod(new ScrollingMovementMethod());

        Log.debug("Binding from scanner service.");
        bindService(new Intent(this, BluetoothBarcodeScannerService.class), bluetoothScannerConnection, Context.BIND_AUTO_CREATE);

        // register for scanner notifications
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED)); // barcode scanned
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING)); // can take a few seconds
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected

        beaconDistanceMeasurer.configureFromContext(this);
        beaconDistanceMeasurer.startListening();
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
