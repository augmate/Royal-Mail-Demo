package com.augmate.beacontest.app;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class BeaconRegion {
    public int numOfBeacons = 0;
    public double minDistance = 100;
    private List<Integer> beaconIds;
    public int regionId = 0;

    public BeaconRegion(Integer... beaconIds) {
        this.beaconIds = Arrays.asList(beaconIds);
    }

    public BeaconRegion setRegionId(int regionId) {
        this.regionId = regionId;
        return this;
    }

    public boolean containsBeaconId(int id) {
        return beaconIds.contains(id);
    }

    @Override
    public String toString() {
        return String.format("Region %d: beacons=[%s] found=%d minDist=%.1f", regionId, TextUtils.join(",", beaconIds), numOfBeacons, minDistance);
    }
}
