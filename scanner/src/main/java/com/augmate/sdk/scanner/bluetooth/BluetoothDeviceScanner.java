package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import com.augmate.sdk.logger.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Scans for traditional bluetooth devices
 * attempts to locate a barcode scanner or another white-listed device in discovery mode
 */
public class BluetoothDeviceScanner extends BroadcastReceiver {
    private OutgoingService service;
    private BluetoothAdapter bluetoothAdapter;

    // FIXME: get rid of mac-whitelist and replace with bluetooth class and name matching
    public static final List<String> WhitelistedDevices = Arrays.asList(
            //"00:1C:97:90:8A:4F" // ES 301 handheld scanner
            //"38:89:DC:00:0C:91" // scanfob 2006: as-001
            //"38:89:DC:00:00:93" // scanfob 2006: as-003
            //"38:89:DC:00:00:C0" // scanfob 2006: as-005
            "38:89:DC:00:00:A7" // scanfob 2006: as-006
            //"38:89:DC:00:00:C5" // scanfob 2006: as-007˚
    );

    public static boolean deviceIsWhitelisted(String deviceId) {
        return WhitelistedDevices.contains(deviceId);
    }

    BluetoothDeviceScanner(OutgoingService service, BluetoothAdapter bluetoothAdapter) {
        this.service = service;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    private boolean removeBond(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            return true;
        } catch (Exception e) {
            Log.exception(e, "Failed removing bond on device");
        }
        return false;
    }

    /**
     * Broadcasts ACTION_SCANNER_FOUND when a bonded scanner is found or when a new bond is created to a scanner
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        boolean foundScanner = false;

        if(device.getBluetoothClass().getDeviceClass() == 0x1f00 && device.getType() == 1 && device.getName() != null && device.getName().startsWith("Wireless Scan")) {
            // ES 301 handheld scanner format
            foundScanner = true;
        }

        if(device.getBluetoothClass().getDeviceClass() == 0x114 && device.getType() == 1 && device.getName() != null && device.getName().startsWith("Scanfob")) {
            foundScanner = true;
        }

        if(!foundScanner) {
            return;
        }

        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                Log.debug("Found device: \"%s\" @ %s", device.getName(), device.getAddress());
                Log.debug("  device.getBondState= " + device.getBondState());
                Log.debug("  device.getBluetoothClass = " + device.getBluetoothClass());
                Log.debug("  device.getType = " + device.getType() + " (unknown=0, classic=1, LE=2, dual=3)");

                if (deviceIsWhitelisted(device.getAddress())) {
                    // must cancel discovery mode before attempting to bond or connect with a device
                    bluetoothAdapter.cancelDiscovery();

                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.debug("Device is already paired. Re-pairing..");
                        removeBond(device);
                        //service.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_FOUND).putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, device));
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
                //Log.debug("Device %s bond state changed from %d to %d", device.getAddress(), prevBondState, bondState);

                switch (bondState) {
                    case BluetoothDevice.BOND_NONE:
                        Log.debug("Device %s unpairing successful. Starting pairing..", device.getAddress());
                        boolean beganPairing = device.createBond();
                        Log.debug("  createBond() returned = " + beganPairing);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.debug("Device %s is being paired", device.getAddress());
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.debug("Device %s pairing successful. Broadcasting device find.", device.getAddress());

                        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

                        service.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_FOUND)
                                .putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, remoteDevice));
                        break;
                }
                break;

            // device service scan. useful for learning about devices.
            case BluetoothDevice.ACTION_UUID:
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                Log.debug("Received Service UUIDs:");
                for (Parcelable anUuidExtra : uuidExtra) {
                    Log.debug("  Service: " + anUuidExtra.toString());
                }

                Log.debug("Broadcasting device find: %s", device.getAddress());

                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

                service.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_FOUND)
                        .putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, remoteDevice));

                break;

            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                Log.debug("Received device pairing request. Sending pin number and pairing confirmation..");
                device.setPin(Utils.convertPinToBytes("1234"));
                device.setPairingConfirmation(true);
                break;

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                //Log.debug("Device connection established to %s", device.getAddress());
                break;
        }
    }
}
