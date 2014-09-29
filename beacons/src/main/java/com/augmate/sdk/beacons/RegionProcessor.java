package com.augmate.sdk.beacons;

import com.augmate.sdk.logger.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RegionProcessor {

    private ArrayList<BeaconRegion> beaconRegions;

    public RegionProcessor(ArrayList<BeaconRegion> beaconRegions) {

        this.beaconRegions = beaconRegions;
    }

    public int getNearestRegionId(List<BeaconInfo> beaconDistances) {

        // accumulate beacons into regions
        for (BeaconInfo beacon : beaconDistances) {
            for (BeaconRegion region : beaconRegions) {
                if (region.containsBeaconId(beacon.minor)) {
                    // found our region
                    region.numOfBeacons++;
                    region.minDistance = Math.min(region.minDistance, beacon.weightedAvgDistance);
                }
            }
        }

        // drop regions with zero beacons (completely out of range, yey!)
        ArrayHelpers.removeWhere(beaconRegions, new ArrayHelpers.Predicate<BeaconRegion>() {
            @Override
            public boolean evaluate(BeaconRegion item) {
                return item.numOfBeacons == 0;
            }
        });

        if (beaconRegions.size() == 0) {
            // no regions detected
            Log.warn("No regions detected!");
            return -1;
        }

        for (BeaconRegion region : beaconRegions) {
            Log.debug(region.toString());
        }

        Collections.sort(beaconRegions, new Comparator<BeaconRegion>() {
            @Override
            public int compare(BeaconRegion lhs, BeaconRegion rhs) {
                return (int) (5000 * lhs.minDistance - 5000 * rhs.minDistance);
            }
        });

        return beaconRegions.get(0).regionId;
    }
}