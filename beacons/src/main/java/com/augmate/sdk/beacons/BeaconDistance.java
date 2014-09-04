package com.augmate.sdk.beacons;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.text.TextUtils;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.rits.cloning.Cloner;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

public class BeaconDistance implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    @SuppressLint("UseSparseArrays")
    private Map<String, BeaconInfo> beaconInfos = new HashMap<String, BeaconInfo>();

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

    /* may be called from UI-thread or a periodic timer's thread */
    public List<BeaconInfo> getLatestBeaconDistances() {

        // deep-clone the beacons list and its history fifo queue
        Collection<BeaconInfo> beacons = new Cloner().deepClone(beaconInfos.values());
        if(beacons == null) {
            Log.error("Sanity Failure: deep-cloned beacons collection IS NULL");
            return new ArrayList<>(); // return an empty list on error
        }

        if(beacons.size() == 0)
            return new ArrayList<>();

        long now = What.timey();

        Log.debug("-------------------------------------------------");

        // process a local-thread copy of the latest beacon list
        for(BeaconInfo beacon : beacons) {

            // expire long unseen beacons
            for(Iterator<HistorySample> samples = beacon.history.iterator(); samples.hasNext(); ) {
                HistorySample sample = samples.next();
                if(now - sample.timestamp > 10000) {
                    samples.remove();
                }
            }

            if(beacon.history.size() == 0)
                continue;

            // calc some stats on the remaining recent beacon history samples
            DescriptiveStatistics stats = new DescriptiveStatistics();

            for(HistorySample s : beacon.history) {
                stats.addValue(s.distance);
            }

            beacon.distanceGeometricMean = stats.getGeometricMean();
            beacon.distanceMean = stats.getMean();
            beacon.distanceKurtosis = stats.getKurtosis();
            beacon.distanceSTD = stats.getStandardDeviation();
            beacon.distanceVariance = stats.getVariance();
            beacon.distanceSkewness = stats.getSkewness();
            beacon.distancePercentile = stats.getPercentile(80);

            Log.debug("beacon '%s %s' has %d recent samples", beacon.beaconName, beacon.uniqueId, beacon.history.size());
            Log.debug("  recent samples: %s", TextUtils.join(",", beacon.history));
            Log.debug("  mean: %.2f / geo-mean: %.2f / variance: %.2f / skewness: %.2f / percentile: %.2f", beacon.distanceMean, beacon.distanceGeometricMean, beacon.distanceVariance, beacon.distanceSkewness, beacon.distancePercentile);
        }

        // expire long-unseen beacons
        for(Iterator<BeaconInfo> beaconsIter = beacons.iterator(); beaconsIter.hasNext(); ) {
            BeaconInfo beacon = beaconsIter.next();
            if(beacon.history.size() == 0) {
                beaconsIter.remove();
            }
        }

        List<BeaconInfo> sortedBeaconList = new ArrayList<>(beacons);

        Collections.sort(sortedBeaconList, new Comparator<BeaconInfo>() {
            @Override
            public int compare(BeaconInfo b1, BeaconInfo b2) {
                return (int)(b1.distance - b2.distance);
            }
        });

        return sortedBeaconList;
    }

    /* callback from BluetoothAdapter comes from its own thread */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        int measuredPower = -74; // TODO: extract the real power value from the scanRecord blob
                                 // relevant payload processing is in estimotes-sdk-preview.jar Utils::beaconFromLeScan()

        final double distance = computeAccuracy(rssi, measuredPower);
        //Log.debug("Pinged device: " + device.getName() + " @ " + device.getAddress() + " rssi=" + rssi + " dist=" + String.format("%.2f", distance));

        // TODO: extract unique minor/major numbers from estimotes

        String uniqueBeaconId = device.getAddress();
        uniqueBeaconId = uniqueBeaconId.substring(uniqueBeaconId.length()-2, uniqueBeaconId.length());

        // grab existing beacon entry or create a new one
        BeaconInfo beaconInfo = beaconInfos.get(uniqueBeaconId);
        if(beaconInfo == null)
            beaconInfo = new BeaconInfo();

        beaconInfo.uniqueId = uniqueBeaconId;
        beaconInfo.distance = distance;
        beaconInfo.beaconName = device.getName() != null ? device.getName() : "<unnamed>";
        beaconInfo.lastSeen = What.timey();

        if(!beaconInfos.containsKey(beaconInfo.uniqueId)) {
            Log.debug("* Found new device: " + device.getName() + " @ " + device.getAddress());

            // estimotes has the following records: Flags,Unknown Structure: -1,Name,Service Data
            List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
            if(records.size() != 0 ) {
                Log.debug("  scan record contains: " + TextUtils.join(",", records));
            }
        }

        onBeaconDiscovered(beaconInfo);
    }

    /* will be called from BluetoothAdapter's own thread */
    private void onBeaconDiscovered(BeaconInfo beacon) {
        beaconInfos.put(beacon.uniqueId, beacon);

        HistorySample sample = new HistorySample();
        sample.distance = beacon.distance;
        sample.timestamp = What.timey();

        // adds to a fixed size FIFO queue
        beacon.history.add(sample);
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
