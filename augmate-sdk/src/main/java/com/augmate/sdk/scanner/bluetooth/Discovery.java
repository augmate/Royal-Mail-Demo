package com.augmate.sdk.scanner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.augmate.sdk.logger.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Discovers Bluetooth scanners
 * Sorts them by supposed signal strength
 */
public class Discovery extends Service {

    // FIXME: get rid of mac-whitelist and replace with bluetooth class and name matching
    public static final List<String> WhitelistedDevices = Arrays.asList(
            //"00:1C:97:90:8A:4F" // ES 301 handheld scanner
            //"38:89:DC:00:0C:91" // scanfob 2006: as-001
            //"38:89:DC:00:00:93" // scanfob 2006: as-003
            //"38:89:DC:00:00:C0" // scanfob 2006: as-005
            //"38:89:DC:00:00:A7" // scanfob 2006: as-006
            "38:89:DC:00:00:AC" // scanfob 2006: as-004
            //"38:89:DC:00:00:C5" // scanfob 2006: as-007˚
    );

    private Binder serviceBinder = new Binder() {
        public Discovery getService() {
            return Discovery.this;
        }
    };
    /**
     * registers for bluetooth events and starts discovery
     */
    public void start() {
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().startDiscovery();
    }

    /**
     * stops discovery and unregisters from bluetooth events
     */
    private void stop() {
        ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().cancelDiscovery();
        this.unregisterReceiver(receiver);
    }

    protected void onDeviceDiscovered(BluetoothDevice device) {
        Log.debug("Device %s found; name=\"%s\" bond=%d class=%d type=%d",
                device.getAddress(), device.getName(), device.getBondState(), device.getBluetoothClass(), device.getType());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        private boolean isScanner(BluetoothDevice device) {
            String deviceName = device.getName();
            if (deviceName == null || device.getType() != -1) {
                return false;
            }

            int deviceClass = device.getBluetoothClass().getDeviceClass();

            // ES 301 scanner
            if (deviceClass == 0x1f00 && deviceName.startsWith("Wireless Scan")) {
                return true;
            }

            // Scanfob 2006 scanner
            if (deviceClass == 0x114 && deviceName.startsWith("Scanfob")) {
                return true;
            }

            return false;
        }

        /**
         * Broadcasts ACTION_SCANNER_FOUND when a bonded scanner is found or when a new bond is created to a scanner
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    onDeviceDiscovered(device);
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.debug("Device %s connection established", device.getAddress());
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.debug("Device %s disconect requested", device.getAddress());
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.debug("Device %s disconnected", device.getAddress());
                    break;
            }
        }
    };
}
