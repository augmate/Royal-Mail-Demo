package com.augmate.sdk.scanner.bluetooth;

import android.app.Activity;
import android.content.*;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;

/**
 * Activities that require bluetooth-scanner should use this connector
 * it will abstract away background services, discovery, and pairing
 */
public class BluetoothScannerConnector {

    private Activity activity;
    private IBluetoothScannerEvents connector;

    /**
     * I'll be honest, Java has a pretty great implementation of generics now: base class and interface in one typename!
     *
     * @param activity parent activity that wants to know about scanner activity
     * @param <T>      must extend Activity and implement IBluetoothScannerConnection
     */
    public <T extends Activity & IBluetoothScannerEvents> BluetoothScannerConnector(T activity) {
        this.activity = activity;
        this.connector = activity;
    }

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
     * Receive messages from BluetoothBarcodeScannerService
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED:
                    connector.onBtScannerResult(intent.getStringExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_STRING));
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING:
                    connector.onBtScannerConnecting();
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED:
                    connector.onBtScannerConnected();
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED:
                    connector.onBtScannerDisconnected();
                    break;

                default:
                    Log.warn("Unhandled intent action: %s", action);
            }
        }
    };

    public void reconnect() {
        if (bluetoothScannerService != null)
            bluetoothScannerService.reconnect();
    }

    /**
     * Async service bind request
     * Results will be published using the IBluetoothScannerConnection interface
     * This is a fire-and-wait-for-events method
     */
    public void start() {
        Log.debug("Binding activity to scanner service..");
        activity.bindService(new Intent(activity, BluetoothBarcodeScannerService.class), bluetoothScannerConnection, Context.BIND_AUTO_CREATE);

        // register for scanner notifications
        activity.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED)); // barcode scanned
        activity.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING)); // can take a few seconds
        activity.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        activity.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected
    }

    /**
     * Async service unbind and notification unsubscribe request
     * Results will not be published because we unsubscribe here as well.
     * This is a fire-and-forget method
     */
    public void stop() {
        Log.debug("Unbinding activity to scanner service..");

        // unregister from scanner notifications
        activity.unregisterReceiver(receiver);
        activity.unbindService(bluetoothScannerConnection);
    }
}
