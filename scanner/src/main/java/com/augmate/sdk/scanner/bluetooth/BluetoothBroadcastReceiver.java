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

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private BluetoothBarcodeScannerService bluetoothBarcodeScannerService;
    private BluetoothAdapter bluetoothAdapter;

    // ecom ES301 handheld barcode scanner, alex's phone
    private final List<String> whitelistedDevices = Arrays.asList("00:1C:97:90:8A:4F", "F8:A9:D0:AC:51:77");

    private boolean deviceIsWhitelisted(String deviceId) {
        return whitelistedDevices.contains(deviceId);
    }

    BluetoothBroadcastReceiver(BluetoothBarcodeScannerService bluetoothBarcodeScannerService, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothBarcodeScannerService = bluetoothBarcodeScannerService;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                Log.debug("Found device: \"%s\" @ %s", device.getName(), device.getAddress());

                if (deviceIsWhitelisted(device.getAddress())) {

                    // must cancel discovery mode before attempting to bond or connect with a device
                    bluetoothAdapter.cancelDiscovery();

                    Log.debug("  device.getBondState= " + device.getBondState());
                    Log.debug("  device.getBluetoothClass = " + device.getBluetoothClass());
                    Log.debug("  device.getType = " + device.getType());
                    Log.debug("  device.getName = " + device.getName());

                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.debug("Device is paired. Broadcasting device find.");
                        bluetoothBarcodeScannerService.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_FOUND).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
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
                        bluetoothBarcodeScannerService.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_FOUND).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
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
        }
    }
}
