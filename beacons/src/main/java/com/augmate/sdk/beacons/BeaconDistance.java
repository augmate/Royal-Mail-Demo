package com.augmate.sdk.beacons;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.text.TextUtils;
import com.augmate.sdk.logger.Log;

import java.util.*;

public class BeaconDistance implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, BeaconInfo> beaconInfos = Collections.synchronizedMap(new HashMap<Integer, BeaconInfo>());

    public void configureFromContext(Context ctx) {
        context = ctx;
    }

    public void startListening() {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        Log.debug("Starting BLE Scanner");
        bluetoothAdapter.startLeScan(this);
    }

    public void stopListening() {
        Log.debug("Stopping BLE Scanner");
        bluetoothAdapter.stopLeScan(this);
    }

    public Map<Integer, BeaconInfo> getLatestBeaconDistances() {
        return beaconInfos;
    }

    /**
     * callback from BluetoothAdapter comes from its own thread
     * @param device
     * @param rssi
     * @param scanRecord
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        int measuredPower = -74; // TODO: extract the real power value from the scanRecord blob
                                 // relevant payload processing is in estimotes-sdk-preview.jar Utils::beaconFromLeScan()

        final double distance = computeAccuracy(rssi, measuredPower);
        Log.debug("Pinged device: " + device.getName() + " @ " + device.getAddress() + " rssi=" + rssi + " dist=" + String.format("%.2f", distance));

        // TODO: extract unique minor/major numbers from estimotes

        BeaconInfo beaconInfo = new BeaconInfo();
        beaconInfo.uniqueId = device.getAddress().hashCode();
        beaconInfo.distance = distance;
        beaconInfo.beaconName = device.getName();

        if(!beaconInfos.containsKey(beaconInfo.uniqueId)) {
            Log.debug("* Found new device: " + device.getName() + " @ " + device.getAddress());

            // estimotes has the following records: Flags,Unknown Structure: -1,Name,Service Data
            List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
            if(records.size() != 0 ) {
                Log.debug("  scan record contains: " + TextUtils.join(",", records));
            }
        }

        beaconInfos.put(beaconInfo.uniqueId, beaconInfo);
    }

    /* ripped out of Estimotes' SDK */
    public static double computeAccuracy(double rssi, double power) {
        if (rssi == 0) {
            return -1.0D;
        }
        double ratio = rssi / power;
        double rssiCorrection = 0.96D + Math.pow(Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
        if (ratio <= 1.0D) {
            return Math.pow(ratio, 9.98D) * rssiCorrection;
        }
        return (0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
    }
}
