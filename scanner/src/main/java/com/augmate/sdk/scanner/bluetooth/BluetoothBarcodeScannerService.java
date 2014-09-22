package com.augmate.sdk.scanner.bluetooth;

import android.app.Service;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Parcelable;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothBarcodeScannerService extends Service {

    public static final String ACTION_BARCODE_SCANNED = "com.augmate.sdk.scanner.bluetooth.action.SCANNED";
    public static final String ACTION_SCANNER_FOUND = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_FOUND";
    public static final String ACTION_SCANNER_CONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_CONNECTED";
    public static final String ACTION_SCANNER_CONNECTING = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_CONNECTING";
    public static final String ACTION_SCANNER_DISCONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_DISCONNECTED";

    public static final String EXTRA_BARCODE_STRING = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_STRING";
    public static final String EXTRA_BARCODE_SCANNER_DEVICE = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_SCANNER";

    private BluetoothDeviceScanner bluetoothAdapterReceiver;
    private BluetoothBarcodeConnection barcodeStreamer;
    private BluetoothAdapter bluetoothAdapter;

    /**
     * Listens to events from BluetoothDeviceScanner and BluetoothBarcodeConnection
     */
    private BroadcastReceiver barcodeScannerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                // from BluetoothDeviceScanner
                case ACTION_SCANNER_FOUND: {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_BARCODE_SCANNER_DEVICE);
                    Log.debug("Trying discovered device..");
                    //tryHardToConnectToDevice(device);
                    attemptDeviceConnection(device);
                } break;

                // from BluetoothBarcodeConnection
                case ACTION_SCANNER_CONNECTED:
                    Log.debug("Barcode scanner connection established.");
                    break;

                // from BluetoothBarcodeConnection
                case ACTION_SCANNER_DISCONNECTED: {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_BARCODE_SCANNER_DEVICE);
                    Log.debug("Barcode scanner connection lost.");
                    blacklistedDevices.add(device.getAddress());
                    disconnectFromScanner();
                } break;
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

        if(!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();

        findBarcodeScannerDevice();
    }

    @Override
    public void onDestroy() {
        Log.debug("Shutting down bluetooth barcode-scanner service.");

        unregisterReceiver(barcodeScannerReceiver);
        stopDiscovery();
        disconnectFromScanner();
    }

    private List<String> blacklistedDevices = new ArrayList<>();

    /**
     * finds an appropriate service on the device and attempts an async connection
     * @param device bluetooth device to connect (hopefully a scanner)
     * @return true if an appropriate service exists, false if this device is completely inappropriate
     */
    private boolean attemptDeviceConnection(BluetoothDevice device) {
        if(device.getUuids() == null) {
            // device is probably offline and there is no cached services list
            Log.warn("Sanity Failure: device '%s' has no services available!", device.getName());
            blacklistedDevices.add(device.getAddress());
            return false;
        }

        UUID bestService = BluetoothBarcodeConnection.findBestService(device.getUuids());
        if(bestService != null) {
            connectToScanner(device, bestService);
            return true;
        }
        Log.debug("-> No relevant services found on device. Ignoring device.");
        blacklistedDevices.add(device.getAddress());
        return false;
    }

    /**
     * works great right after discovery and bonding
     * or if device is already bonded and is in discovery mode
     * but fails miserably if device is bonded but not in discoverable mode
     * goal here is to connect to reconnect to a bonded device
     * it seems traditional bluetooth scanners can't do that
     * @param device
     */
    private void tryHardToConnectToDevice(BluetoothDevice device) {
        ParcelUuid[] uuids = device.getUuids();

        Log.debug("Trying hard to connect to device of type = " + device.getType() + "(unknown=0, classic=1, LE=2, dual=3)");

        for (Parcelable parceableUuid : uuids) {
            UUID uuid = UUID.fromString(parceableUuid.toString());
            Log.debug("Trying service: %s", uuid);

            BluetoothSocket socket;

            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (IOException e) {
                Log.exception(e, "Could not open rfcomm socket and stream to device.");
                continue;
            }

            Log.debug("Connection established. Opening input stream..");

            try {
                InputStream stream = socket.getInputStream();
                int read;
                byte[] buffer = new byte[128];
                Log.debug("Reading from socket..");
                while ((read = stream.read(buffer)) >= 0) {
                    String newData = new String(buffer, 0, read);
                    Log.debug("received from scanner: [%s] (%d bytes)", newData, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.debug("-> Service doesn't work: %s", uuid.toString());
        }
    }

    /**
     * iterates through bonded devices and attempts to connect to all of them
     * after it runs out of bonded devices to try, it attempts discovery of a new device
     * FIXME: bonded devices -> discovery mode loop is busted. need to make it work over and over and over
     * and not break if user requests re-pairing multiple times in a row\
     * and need to manage the connection a little tighter
     * so perhaps all of this logic should be moved to a non-ui thread
     */
    private void findBarcodeScannerDevice() {
        disconnectFromScanner();

        // always stop discovery just in case it's already going
        // could even have been started by another application
        stopDiscovery();

        // ties down ui-thread brute-forcing a device
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("38:89:DC:00:0C:91");
//        tryHardToConnectToDevice(device);

        // take a stab at connecting to the scanfob scanner on a separate thread
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("38:89:DC:00:0C:91");
//        UUID bestService = BluetoothBarcodeConnection.findBestService(device.getUuids());
//        if(bestService != null) {
//            connectToScanner(device, bestService);
//        }

        // just because a device is bonded doesn't mean it's connectable! :(
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if(blacklistedDevices.contains(device.getAddress()))
                continue;

            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

            Log.debug("Trying bonded device: %s (%s)", device.getName(), device.getAddress());
            // if attempt couldn't be started, then the device doesn't contain relevant services
            // blacklist it for a while
            if(attemptDeviceConnection(remoteDevice))
                return;
        }

        // clear blacklist if doing discovery
        // this allows us to cycle through bonded scanners and take a stab at discovery
        blacklistedDevices.clear();

        //Log.debug("Bonded barcode-scanner not found. Scanning for a new one..");
        startDiscovery();
    }

    private void connectToScanner(BluetoothDevice device, UUID service) {
        sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTING).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
        new Thread(barcodeStreamer = new BluetoothBarcodeConnection(device, service, getBaseContext()), "bt-scanner").start();
    }

    private void startDiscovery() {
        if (bluetoothAdapterReceiver == null) {
            Log.debug("Bluetooth broadcast-receiver created");

            // sign-up for relevant events
            bluetoothAdapterReceiver = new BluetoothDeviceScanner(this, bluetoothAdapter);
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            //this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
            this.registerReceiver(bluetoothAdapterReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
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
