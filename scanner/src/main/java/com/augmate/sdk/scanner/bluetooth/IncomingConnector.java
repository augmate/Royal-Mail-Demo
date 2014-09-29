package com.augmate.sdk.scanner.bluetooth;

import android.app.Activity;
import android.content.*;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;

/**
 * Activities that require bluetooth-scanner should use this connector
 * it is based on a simpler bluetooth-service that listens to pre-bonded devices
 */
public class IncomingConnector {

    private Activity activity;
    private IBluetoothScannerEvents callbackReceiver;

    /**
     * I'll be honest, Java has a pretty great implementation of generics now: base class and interface in one typename!
     *
     * @param activity parent activity that wants to know about scanner activity
     * @param <T>      must extend Activity and implement IBluetoothScannerConnection
     */
    public <T extends Activity & IBluetoothScannerEvents> IncomingConnector(T activity) {
        this.activity = activity;
        this.callbackReceiver = activity;
    }

    private IncomingService bluetoothScannerService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothScannerService = ((IncomingService.ScannerBinder) service).getService();
            Log.debug("Service bound: " + name.flattenToShortString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.debug("Service unbound: " + name.flattenToShortString());
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
                case ServiceEvents.ACTION_BARCODE_SCANNED:
                    callbackReceiver.onBtScannerResult(intent.getStringExtra(ServiceEvents.EXTRA_BARCODE_STRING));
                    break;

                case ServiceEvents.ACTION_SCANNER_CONNECTED:
                    callbackReceiver.onBtScannerConnected();
                    break;

                case ServiceEvents.ACTION_SCANNER_DISCONNECTED:
                    callbackReceiver.onBtScannerDisconnected();
                    break;

                default:
                    Log.warn("Unhandled intent action: %s", action);
            }
        }
    };

    /**
     * Async service bind request
     * Results will be published using the IBluetoothScannerConnection interface
     * This is a fire-and-wait-for-events method
     */
    public void start() {
        Log.debug("Binding activity to scanner service..");
        activity.bindService(new Intent(activity, IncomingService.class), connection, Context.BIND_AUTO_CREATE);

        // register for scanner notifications
        activity.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_BARCODE_SCANNED)); // barcode scanned
        activity.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        activity.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected
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
        activity.unbindService(connection);
    }
}
