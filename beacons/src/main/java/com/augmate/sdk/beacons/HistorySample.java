package com.augmate.sdk.beacons;

public class HistorySample {
    public long timestamp = 0;
    public double distance = 100;
    public double life = 0;
    public double power = 0;

    @Override
    public String toString() {
        //return String.format("%.2f (%d msec ago)", distance, What.timey() - timestamp);
        return String.format("%.1f", distance);
    }
}
