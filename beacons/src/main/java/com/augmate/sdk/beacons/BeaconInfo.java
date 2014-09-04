package com.augmate.sdk.beacons;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class BeaconInfo implements Cloneable {
    public String beaconName;
    public String uniqueId;
    public double distance;
    public long lastSeen;

    // assuming beacons emit 1 sample every 500ms
    // 5 samples = 2.5 seconds worth of data
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
