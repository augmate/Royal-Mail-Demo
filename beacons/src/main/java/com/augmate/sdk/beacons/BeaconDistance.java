package com.augmate.sdk.beacons;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.Timer;
import com.augmate.sdk.logger.What;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeaconDistance implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    @SuppressLint("UseSparseArrays")
    private Map<String, BeaconInfo> beaconInfos = new ConcurrentHashMap<>();

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

    /**
     * may be called from UI-thread or a periodic timer's thread
     * currently this is pretty slow. about 30msec for 6 beacons.
     */
    public List<BeaconInfo> getLatestBeaconDistances() {

        Timer cloneTimer = new Timer("beacons array deep-copy");

        // deep-copy the beacons list and its history fifo queue
        // not pretty, but 30x faster than rits.cloning.cloner
        List<BeaconInfo> beacons = new ArrayList<>();
        for(BeaconInfo readOnlyBeacon : beaconInfos.values()) {
            beacons.add(readOnlyBeacon.duplicate());
        }

        cloneTimer.stop();

        if(beacons.size() == 0)
            return new ArrayList<>();

        long now = What.timey();

        //Log.debug("-------------------------------------------------");

        Timer beaconStats = new Timer("beacon stat crush");

        // process a local-thread copy of the latest beacon list
        for(BeaconInfo beacon : beacons) {

            // expire long unseen beacons
            beacon.numValidSamples = 0;
            for(int i = 0; i < beacon.NumHistorySamples; i ++) {
                if(beacon.history[i] != null) {
                    if(now - beacon.history[i].timestamp > 10000) {
                        beacon.history[i] = null;
                    } else {
                        beacon.numValidSamples ++;
                    }
                }
            }

            // calc some stats on the remaining recent beacon history samples
            DescriptiveStatistics stats = new DescriptiveStatistics();

            for(HistorySample s : beacon.history) {
                if(s != null)
                    stats.addValue(s.distance);
            }

            //beacon.distanceGeometricMean = stats.getGeometricMean();
            beacon.distanceMean = stats.getMean();
            beacon.distanceKurtosis = stats.getKurtosis();
            beacon.distanceSTD = stats.getStandardDeviation();
            beacon.distanceVariance = stats.getVariance();
            beacon.distanceSkewness = stats.getSkewness();
            beacon.distancePercentile = stats.getPercentile(80);

//            Log.debug("beacon '%s %s' has %d recent samples", beacon.beaconName, beacon.uniqueBleDeviceId, beacon.history.size());
//            Log.debug("  recent samples: %s", TextUtils.join(",", beacon.history));
//            Log.debug("  mean: %.2f / geo-mean: %.2f / variance: %.2f / skewness: %.2f / percentile: %.2f", beacon.distanceMean, beacon.distanceGeometricMean, beacon.distanceVariance, beacon.distanceSkewness, beacon.distancePercentile);
        }

        beaconStats.stop();

        Timer beaconSorting = new Timer("sorting beacon list");

        // expire long-unseen beacons
        for(Iterator<BeaconInfo> beaconsIter = beacons.iterator(); beaconsIter.hasNext(); ) {
            BeaconInfo beacon = beaconsIter.next();
            if(beacon.numValidSamples ==0) {
                beaconsIter.remove();
            }
        }

        List<BeaconInfo> sortedBeaconList = new ArrayList<>(beacons);

        Collections.sort(sortedBeaconList, new Comparator<BeaconInfo>() {
            @Override
            public int compare(BeaconInfo b1, BeaconInfo b2) {
                return b1.minor - b2.minor;
            }
        });

        beaconSorting.stop();

        return sortedBeaconList;
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
        BeaconInfo beaconInfo = beaconInfos.get(uniqueBeaconId);
        if(beaconInfo == null)
            beaconInfo = new BeaconInfo();

        beaconInfo.uniqueBleDeviceId = uniqueBeaconId;
        beaconInfo.beaconName = device.getName() != null ? device.getName() : "<unnamed>";
        beaconInfo.lastSeen = What.timey();

        if(!beaconInfos.containsKey(beaconInfo.uniqueBleDeviceId)) {
            // the first time we locate a new device, parse it for useful information
            Log.debug("Found new device: " + device.getName() + " @ " + device.getAddress());

            if(Objects.equals(device.getName(), "estimote")) {
                EstimoteBeaconInfo estimoteBeaconInfo = EstimoteBeaconInfo.getFromScanRecord(scanRecord);

                if(estimoteBeaconInfo == null) {
                    Log.error("Sanity Failure: failed to parse estimote's scan-record.");
                    return;
                }

                beaconInfo.major = estimoteBeaconInfo.major;
                beaconInfo.minor = estimoteBeaconInfo.minor;
                beaconInfo.measuredPower = estimoteBeaconInfo.measuredPower;
                beaconInfo.beaconType = BeaconInfo.BeaconType.Estimote;
            } else {
                Log.debug("Device not recognized. Treating as generic.");
                beaconInfo.beaconType = BeaconInfo.BeaconType.Unknown;
            }
        }

        // calculate approximate distance using rssi and power parameters
        beaconInfo.distance = computeAccuracy(rssi, beaconInfo.measuredPower);

        //Log.debug("Pinged device: type=" + beaconInfo.beaconType + " rssi=" + rssi + " power=" + beaconInfo.measuredPower + " dist=" + String.format("%.2f", beaconInfo.distance));

        // only commit recognized beacons
        if(beaconInfo.beaconType != BeaconInfo.BeaconType.Unknown) {
            onBeaconDiscovered(beaconInfo);
        }
    }



    /* will be called from BluetoothAdapter's own thread */
    private void onBeaconDiscovered(BeaconInfo beacon) {
        beaconInfos.put(beacon.uniqueBleDeviceId, beacon);

        HistorySample sample = new HistorySample();
        sample.distance = beacon.distance;
        sample.timestamp = What.timey();

        // adds to a fixed size FIFO queue
        beacon.addHistorySample(sample);
    }

    /* ripped out of Estimotes' SDK */
    private static double computeAccuracy(double rssi, double power) {
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
