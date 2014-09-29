package com.augmate.sdk.scanner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;

/**
 * This one only listens for incoming connections from already bonded devices
 */
public class IncomingService extends Service {

    private IncomingConnection listenerThread;

    // interface for Activities that want to subscribe to bind to this Service
    private ScannerBinder scannerBinder = new ScannerBinder();

    /**
     * Listens to events from ListeningConnection
     */
    private BroadcastReceiver barcodeScannerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                // from BluetoothBarcodeConnection
                case ServiceEvents.ACTION_SCANNER_CONNECTED:
                    Log.debug("Barcode scanner connection established.");
                    break;

                // from BluetoothBarcodeConnection
                case ServiceEvents.ACTION_SCANNER_DISCONNECTED: {
                    Log.debug("Barcode scanner connection lost.");
                }
                break;
            }
        }
    };

    @Override
    public void onCreate() {
        Log.debug("Starting bluetooth barcode-scanner service.");

        registerReceiver(barcodeScannerReceiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_CONNECTED));
        registerReceiver(barcodeScannerReceiver, new IntentFilter(ServiceEvents.ACTION_SCANNER_DISCONNECTED));

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();

        // launch dedicated scanner listening thread
        new Thread(listenerThread = new IncomingConnection(getBaseContext()), "bt-scanner").start();
    }

    @Override
    public void onDestroy() {
        Log.debug("Shutting down bluetooth barcode-scanner service.");

        unregisterReceiver(barcodeScannerReceiver);

        // shutdown dedicated listening thread
        if (listenerThread != null) {
            listenerThread.shutdown();
            listenerThread = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return scannerBinder;
    }

    public class ScannerBinder extends Binder {
        public IncomingService getService() {
            return IncomingService.this;
        }
    }
}
