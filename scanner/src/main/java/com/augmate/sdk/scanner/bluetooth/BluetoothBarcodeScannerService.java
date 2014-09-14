package com.augmate.sdk.scanner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;

public class BluetoothBarcodeScannerService extends Service {

    public static final String ACTION_BARCODE_SCANNED = "com.augmate.sdk.scanner.bluetooth.action.SCANNED";
    public static final String ACTION_SCANNER_FOUND = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_FOUND";
    public static final String ACTION_SCANNER_CONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_CONNECTED";
    public static final String ACTION_SCANNER_DISCONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_DISCONNECTED";

    public static final String EXTRA_BARCODE_STRING = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_STRING";
    public static final String EXTRA_BARCODE_SCANNER_DEVICE = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_SCANNER";

    private BluetoothBroadcastReceiver bluetoothAdapterReceiver;
    private BluetoothBarcodeConnection barcodeStreamer;
    private BluetoothAdapter bluetoothAdapter;

    /**
     * BluetoothBroadcastReceiver handles device discovery and pairing using BluetoothAdapter
     * this BroadcastReceiver gets the simple results of all that work
     */
    private BroadcastReceiver barcodeScannerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_SCANNER_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_BARCODE_SCANNER_DEVICE);
                    Log.debug("Discovered barcode-scanner: " + device);

                    // start streaming from the discovered device :)
                    connectToScanner(device);

                    break;

                case ACTION_SCANNER_CONNECTED:
                    Log.debug("Barcode scanner connection established.");
                    break;

                case ACTION_SCANNER_DISCONNECTED:
                    Log.debug("Barcode scanner connection lost.");
                    disconnectFromScanner();
                    break;
            }
        }
    };
    // interface for Activities that want to subscribe to bind to this Service
    private ScannerBinder scannerBinder = new ScannerBinder();

    @Override
    public void onCreate() {
        Log.debug("Starting bluetooth barcode-scanner service.");

        registerReceiver(barcodeScannerReceiver, new IntentFilter(ACTION_SCANNER_FOUND));
        registerReceiver(barcodeScannerReceiver, new IntentFilter(ACTION_SCANNER_CONNECTED));
        registerReceiver(barcodeScannerReceiver, new IntentFilter(ACTION_SCANNER_DISCONNECTED));

        // TODO: enable bluetooth adapter if disabled
        bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        findBarcodeScannerDevice();
    }

    @Override
    public void onDestroy() {
        Log.debug("Shutting down bluetooth barcode-scanner service.");

        unregisterReceiver(barcodeScannerReceiver);
        stopDiscovery();
        disconnectFromScanner();
    }

    private void findBarcodeScannerDevice() {
        // just because a device is bonded doesn't mean it's present. connection may fail.
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if ("00:1C:97:90:8A:4F".equals(device.getAddress())) {
                Log.debug("Bonded barcode scanner found. Broadcasting device..");
                sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_FOUND).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
                return;
            }
        }

        Log.debug("Bonded barcode-scanner not found. Scanning for a new one..");
        startDiscovery();
    }

    private void startDiscovery() {
        if (bluetoothAdapterReceiver == null) {
            Log.debug("Bluetooth broadcast-receiver created");

            // sign-up for relevant events
            bluetoothAdapterReceiver = new BluetoothBroadcastReceiver(this, bluetoothAdapter);
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
        }

        if (bluetoothAdapter.isDiscovering()) {
            Log.warn("Bluetooth discovery already in progress.");
        } else if (!bluetoothAdapter.startDiscovery()) {
            Log.error("Bluetooth discovery failed to start");
        }
    }

    private void stopDiscovery() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        if (bluetoothAdapterReceiver != null) {
            this.unregisterReceiver(bluetoothAdapterReceiver);
            bluetoothAdapterReceiver = null;
        }
    }

    private void connectToScanner(BluetoothDevice device) {
        if (barcodeStreamer != null) {
            Log.warn("Sanity Failure: asked to connect to a device while another connection is still alive.");
        }

        Log.debug("Connecting to barcode-scanner: %s", device.getAddress());
        new Thread(barcodeStreamer = new BluetoothBarcodeConnection(device, getBaseContext()), "bt-scanner").start();
    }

    private void disconnectFromScanner() {
        if (barcodeStreamer == null)
            return;

        Log.debug("Disconnecting from barcode-scanner");

        // shutdown() will interrupt hosting thread causing it to exit within 10ms
        // no need to manage the thread manually. can freely spawn another thread without race-conditions.
        barcodeStreamer.shutdown();
        barcodeStreamer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return scannerBinder;
    }

    public void reconnect() {
        disconnectFromScanner();
        findBarcodeScannerDevice();
    }

    public class ScannerBinder extends Binder {
        public BluetoothBarcodeScannerService getService() {
            return BluetoothBarcodeScannerService.this;
        }
    }
}
