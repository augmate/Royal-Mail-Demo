package com.augmate.sdk.beacons;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class BeaconInfo {
    public String beaconName = "unnamed";
    public String uniqueBleDeviceId = "unidentified";
    public int measuredPower = -74;
    public double distance = 100;
    public long lastSeen = 0;
    public int major = 0;
    public int minor = 0;

    public enum BeaconType { Unknown, Estimote, SensorTag };
    public BeaconType beaconType = BeaconType.Unknown;

    // assuming beacons emit 1 sample every 300ms
    // 10 samples will hold 3 seconds worth of data
    public CircularFifoQueue<HistorySample> history = new CircularFifoQueue<>(10);

    // stats crushed on the last X samples above
    public double distanceMean;
    public double distanceKurtosis;
    public double distanceSTD;
    public double distanceSkewness;
    public double distancePercentile;
    public double distanceGeometricMean;
    public double distanceVariance;
}
