package com.augmate.sdk.beacons;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.augmate.sdk.logger.What;

import java.util.*;
import java.util.concurrent.*;

public class BeaconRegionDetector implements BluetoothAdapter.LeScanCallback {
    private Context context;
    private Map<String, BeaconInfo> beaconData = new ConcurrentHashMap<>();

    public void configureFromContext(Context ctx) {
        context = ctx;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public void startListening() {
        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothAdapter.startLeScan(this);
    }

    public void stopListening() {
        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothAdapter.stopLeScan(this);
    }

    /**
     * may be called from UI-thread or a periodic timer's thread
     * this was rewritten a while ago. it's fast. <1 msec for 6 beacons.
     */
    public List<BeaconInfo> getLatestBeaconDistances() {
        // deep-copy the beacons list and its history fifo queue
        // not pretty, but 30x faster than rits.cloning.cloner
        List<BeaconInfo> beacons = new ArrayList<>();
        for (BeaconInfo readOnlyBeacon : beaconData.values()) {
            beacons.add(readOnlyBeacon.duplicate());
        }

        if (beacons.size() == 0)
            return new ArrayList<>();

        long now = What.timey();

        // process a local-thread copy of the latest beacon list
        for (BeaconInfo beacon : beacons) {
            updateBeaconSample(now, beacon);
        }

        // expire long-unseen beacons
        for (Iterator<BeaconInfo> beaconsIter = beacons.iterator(); beaconsIter.hasNext(); ) {
            BeaconInfo beacon = beaconsIter.next();
            if (beacon.numValidSamples == 0) {
                beaconsIter.remove();
            }
        }

        Collections.sort(beacons, new Comparator<BeaconInfo>() {
            @Override
            public int compare(BeaconInfo b1, BeaconInfo b2) {
                return b1.minor - b2.minor;
            }
        });

        return beacons;
    }

    private void updateBeaconSample(long now, BeaconInfo beacon) {
        // expire long unseen beacons
        beacon.numValidSamples = 0;
        for (int i = 0; i < BeaconInfo.NumHistorySamples; i++) {
            if (beacon.history[i] == null)
                continue;

            beacon.history[i].life = Math.max(0, 1 - (double) (now - beacon.history[i].timestamp) / 2000.0); // [0-1]

            if (beacon.history[i].life < 0.01) {
                beacon.history[i] = null;
            } else {
                beacon.numValidSamples++;
            }
        }

        if (beacon.numValidSamples == 0) {
            beacon.weightedAvgDistance = 100; // throw beacon away
            return;
        }

        // weighted average beacon samples
        double sum = 0;
        double energy = 0;
        final int biasRecentSamplesFactor = 3; // [1-10]

        // order doesn't matter because we are measuring extinction independently above
        for (int i = 0; i < BeaconInfo.NumHistorySamples; i++) {
            if (beacon.history[i] == null)
                continue;
            double weight = Math.pow(biasRecentSamplesFactor, 9.0 * beacon.history[i].life);
            sum += beacon.history[i].distance * weight;
            energy += weight;
        }

        beacon.weightedAvgDistance = sum / energy;

//        Log.debug("beacon minor='%d' / distance: %.2f / energy: %.2f / samples: %d", beacon.minor, beacon.weightedAvgDistance, energy, beacon.numValidSamples);
//        Log.debug("  recent samples: %s", TextUtils.join(" | ", beacon.history));
//        Log.debug("\n");
    }

    /* callback from BluetoothAdapter comes from its own thread */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        // NOTE: this must be a unique device id within a region
        // the device may broadcast the same information as another device
        // eg: both can claim to be "Truck #5"
        // but their unique-ids must be distinct so we can track their distance properly
        String uniqueBeaconId = device.getAddress();

        // grab existing beacon-entry, else create a new one
        BeaconInfo beaconInfo = beaconData.get(uniqueBeaconId);
        if (beaconInfo == null)
            beaconInfo = new BeaconInfo();

        beaconInfo.uniqueBleDeviceId = uniqueBeaconId;
        beaconInfo.beaconName = device.getName() != null ? device.getName() : "<unnamed>";
        beaconInfo.lastSeen = What.timey();

        if (!beaconData.containsKey(beaconInfo.uniqueBleDeviceId)) {
            // the first time we locate a new device, parse it for useful information
            //Log.debug("Found new device: " + device.getName() + " @ " + device.getAddress());

            // try to parse beacon info
            EstimoteBeaconInfo estimoteBeaconInfo = EstimoteBeaconInfo.getFromScanRecord(scanRecord);
            if (estimoteBeaconInfo != null) {
                beaconInfo.major = estimoteBeaconInfo.major;
                beaconInfo.minor = estimoteBeaconInfo.minor;
                beaconInfo.measuredPower = estimoteBeaconInfo.measuredPower;
                beaconInfo.beaconType = BeaconInfo.BeaconType.Estimote;

                // FIXME: this probably shouldn't be done here
                if (beaconInfo.minor == 2 || beaconInfo.minor == 4)
                    beaconInfo.regionId = 1;
                if (beaconInfo.minor == 3 || beaconInfo.minor == 5)
                    beaconInfo.regionId = 2;
            }
        }

        // only commit recognized beacons
        if ((beaconInfo.beaconType != BeaconInfo.BeaconType.Unknown) && (beaconInfo.regionId != 0)) {
            //Log.debug("Pinged device: major=" + beaconInfo.major + " minor=" + beaconInfo.minor + " rssi=" + rssi + " power=" + beaconInfo.measuredPower);
            onBeaconDiscovered(beaconInfo, rssi);
        }
    }

    /* will be called from BluetoothAdapter's own thread */
    private void onBeaconDiscovered(BeaconInfo beacon, int observedPower) {
        beaconData.put(beacon.uniqueBleDeviceId, beacon);

        HistorySample sample = new HistorySample();
        sample.distance = approximateDistanceInMeters(observedPower, beacon.measuredPower);
        sample.power = observedPower;
        sample.timestamp = beacon.lastSeen;
        sample.life = 1;

        // adds to a fixed size FIFO queue
        beacon.addHistorySample(sample);
    }

    /* ripped out of Estimotes' SDK */
    private static double approximateDistanceInMeters(double observedPower, double broadcastPowerAt1Meter) {
        if (observedPower == 0) {
            return -1.0D;
        }
        double ratio = observedPower / broadcastPowerAt1Meter;
        double rssiCorrection = 0.96D + Math.pow(Math.abs(observedPower), 3.0D) % 10.0D / 150.0D;
        if (ratio <= 1.0D) {
            return Math.pow(ratio, 9.98D) * rssiCorrection;
        }
        return (0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
    }
}
