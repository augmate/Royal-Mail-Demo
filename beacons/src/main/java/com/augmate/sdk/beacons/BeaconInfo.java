package com.augmate.sdk.beacons;

public class BeaconInfo {
    public String beaconName = "unnamed";
    public String uniqueBleDeviceId = "unidentified";
    public int measuredPower = -74;
    public double distance = 100;
    public long lastSeen = 0;
    public int major = 0;
    public int minor = 0;
    public double weightedAvgDistance = 0;

    public enum BeaconType { Unknown, Estimote, SensorTag };
    public BeaconType beaconType = BeaconType.Unknown;

    // assuming beacons emit 1 sample every 300ms
    // 10 samples will hold 3 seconds worth of data
    //public CircularFifoQueue<HistorySample> history = new CircularFifoQueue<>(10);
    public final int NumHistorySamples = 10;
    public HistorySample[] history = new HistorySample[NumHistorySamples];
    public int lastHistoryIdx = 0;
    public int numValidSamples = 0;

    // stats crushed on the last X samples above
    public double distanceMean;
    public double distanceKurtosis;
    public double distanceSTD;
    public double distanceSkewness;
    public double distancePercentile;
    public double distanceGeometricMean;
    public double distanceVariance;

    public void addHistorySample(HistorySample sample) {
        history[lastHistoryIdx] = sample;
        lastHistoryIdx = (lastHistoryIdx + 1) % NumHistorySamples;
    }

    public BeaconInfo duplicate() {
        BeaconInfo info = new BeaconInfo();
        info.beaconName = beaconName;
        info.uniqueBleDeviceId = uniqueBleDeviceId;
        info.measuredPower = measuredPower;
        info.distance = distance;
        info.lastSeen = lastSeen;
        info.major = major;
        info.minor = minor;
        info.beaconType = beaconType;

        //info.history = new CircularFifoQueue<>(Arrays.asList((HistorySample[]) history.toArray()));
        info.history = history.clone();

        info.distanceMean = distanceMean;
        info.distanceKurtosis = distanceKurtosis;
        info.distanceSTD = distanceSTD;
        info.distanceSkewness = distanceSkewness;
        info.distancePercentile = distancePercentile;
        info.distanceGeometricMean = distanceGeometricMean;
        info.distanceVariance = distanceVariance;

        return info;
    }
}
