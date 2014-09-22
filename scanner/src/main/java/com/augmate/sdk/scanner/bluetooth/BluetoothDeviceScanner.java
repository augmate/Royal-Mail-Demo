package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import com.augmate.sdk.logger.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Scans for traditional bluetooth devices
 * attempts to locate a barcode scanner or another white-listed device in discovery mode
 */
public class BluetoothDeviceScanner extends BroadcastReceiver {
    private BluetoothBarcodeScannerService bluetoothBarcodeScannerService;
    private BluetoothAdapter bluetoothAdapter;

    // FIXME: get rid of mac-whitelist and replace with bluetooth class and name matching
    public static final List<String> WhitelistedDevices = Arrays.asList(
            "00:1C:97:90:8A:4F", // ES 301 handheld scanner
            "38:89:DC:00:0C:91" // scanfob 2006
    );

    private boolean deviceIsWhitelisted(String deviceId) {
        return WhitelistedDevices.contains(deviceId);
    }

    BluetoothDeviceScanner(BluetoothBarcodeScannerService bluetoothBarcodeScannerService, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothBarcodeScannerService = bluetoothBarcodeScannerService;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    /**
     * Broadcasts ACTION_SCANNER_FOUND when a bonded scanner is found or when a new bond is created to a scanner
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                Log.debug("Found device: \"%s\" @ %s", device.getName(), device.getAddress());
//                Log.debug("  device.getBondState= " + device.getBondState());
//                Log.debug("  device.getBluetoothClass = " + device.getBluetoothClass());
//                Log.debug("  device.getType = " + device.getType() + " (unknown=0, classic=1, LE=2, dual=3)");

                if (deviceIsWhitelisted(device.getAddress())) {
                    // must cancel discovery mode before attempting to bond or connect with a device
                    bluetoothAdapter.cancelDiscovery();

                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.debug("Device is paired. Broadcasting device find.");
                        bluetoothBarcodeScannerService.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_FOUND)
                                .putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
                    } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.debug("Device in pairing-state. Waiting on it.");
                    } else {
                        Log.debug("Device is not paired. Starting pairing..");
                        boolean beganPairing = device.createBond();
                        Log.debug("  createBond() returned = " + beganPairing);
                    }
                }
                break;

            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                Log.debug("Device %s bond state changed from %d to %d", device.getAddress(), prevBondState, bondState);

                switch (bondState) {
                    case BluetoothDevice.BOND_NONE:
                        Log.debug("Device unpairing successful. Starting pairing..");
                        boolean beganPairing = device.createBond();
                        Log.debug("  createBond() returned = " + beganPairing);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.debug("Device is being paired");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.debug("Device pairing successful. Broadcasting device find.");
                        bluetoothBarcodeScannerService.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_FOUND)
                                .putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
                        break;
                }
                break;

            // device service scan. useful for learning about devices.
            case BluetoothDevice.ACTION_UUID:
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                Log.debug("Found UUID:");
                for (Parcelable anUuidExtra : uuidExtra) {
                    Log.debug("  Service: " + anUuidExtra.toString());
                }
                break;

            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                Log.debug("Received device pairing request. Sending pin number and pairing confirmation..");
                device.setPin(Utils.convertPinToBytes("1234"));
                device.setPairingConfirmation(true);
                break;

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Log.debug("Device connection established to %s", device.getAddress());
                break;
        }
    }
}
