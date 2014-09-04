package com.augmate.sdk.beacons;

public class HistorySample {
    public long timestamp;
    public double distance;

    @Override
    public String toString() {
        //return String.format("%.2f (%d msec ago)", distance, What.timey() - timestamp);
        return String.format("%.2f", distance);
    }
}
