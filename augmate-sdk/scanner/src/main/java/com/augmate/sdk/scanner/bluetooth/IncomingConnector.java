package com.augmate.sdk.scanner.bluetooth;

import android.content.*;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Activities that require bluetooth-scanner should use this connector
 * it is based on a simpler bluetooth-service that listens to pre-bonded devices
 */
public class IncomingConnector {
    private IBluetoothScannerEvents callbackReceiver;
    private IncomingService bluetoothScannerService;
    private Context context;

    /**
     * @param context parent context that wants to know about scanner activity
     */

    @Inject
    public IncomingConnector(@Assisted IBluetoothScannerEvents bluetoothListener, Context context) {
        this.callbackReceiver = bluetoothListener;
        this.context = context;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothScannerService = ((IncomingService.ScannerBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
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
        context.bindService(new Intent(context, IncomingService.class), connection, Context.BIND_AUTO_CREATE);
        callbackReceiver.onBtScannerSearching();
        // register for scanner notifications
        context.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_BARCODE_SCANNED)); // barcode scanned
        context.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        context.registerReceiver(receiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected
    }

    /**
     * Async service unbind and notification unsubscribe request
     * Results will not be published because we unsubscribe here as well.
     * This is  fire-and-forget method
     */
    public void stop() {
        Log.debug("Unbinding activity to scanner service..");

        // unregister from scanner notifications
        context.unregisterReceiver(receiver);
        context.unbindService(connection);
    }
}
