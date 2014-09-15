package com.augmate.beacontest.app;

import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.logger.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RegionProcessor {

    static int getNearestRegionId(List<BeaconInfo> beaconDistances) {

        List<BeaconRegion> beaconRegions = Arrays.asList(
                new BeaconRegion(1, 2).setRegionId(1),
                new BeaconRegion(3, 4).setRegionId(2)
        );

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
        ArrayUtils.removeWhere(beaconRegions, new ArrayUtils.Predicate<BeaconRegion>() {
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
                return (int) (5000 * (lhs.minDistance - rhs.minDistance));
            }
        });

        return beaconRegions.get(0).regionId;
    }
}