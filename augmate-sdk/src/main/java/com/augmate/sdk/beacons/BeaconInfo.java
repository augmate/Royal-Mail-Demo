package com.augmate.sdk.beacons;

public class BeaconInfo {
    public String beaconName = "unnamed";
    public String uniqueBleDeviceId = "unidentified";
    public int measuredPower = -74; // measured at 1m (power setting on beacon)
    public long lastSeen = 0;
    public int major = 0;
    public int minor = 0;
    public int regionId = 0;
    public double weightedAvgDistance = 0;
    public double lastestPower = 0;

    public enum BeaconType { Unknown, Estimote, SensorTag }
    public BeaconType beaconType = BeaconType.Unknown;

    // assuming beacons emit 1 sample every 300ms
    // 10 samples will hold 3 seconds worth of data
    public static final int NumHistorySamples = 10;
    public HistorySample[] history = new HistorySample[NumHistorySamples];
    public int lastHistoryIdx = 0;
    public int numValidSamples = 0;

    public void addHistorySample(HistorySample sample) {
        lastestPower = sample.power;
        history[lastHistoryIdx] = sample;
        lastHistoryIdx = (lastHistoryIdx + 1) % NumHistorySamples;
    }

    public BeaconInfo duplicate() {
        BeaconInfo info = new BeaconInfo();
        info.beaconName = beaconName;
        info.uniqueBleDeviceId = uniqueBleDeviceId;
        info.measuredPower = measuredPower;
        info.lastSeen = lastSeen;
        info.major = major;
        info.minor = minor;
        info.beaconType = beaconType;
        info.regionId = regionId;
        info.lastestPower = lastestPower;
        info.history = history.clone();

        return info;
    }
}
