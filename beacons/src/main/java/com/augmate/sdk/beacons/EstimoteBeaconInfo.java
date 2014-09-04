package com.augmate.sdk.beacons;

import com.augmate.sdk.logger.Log;

import java.util.List;

public class EstimoteBeaconInfo {

    public int major;
    public int measuredPower;
    public int minor;

    public static EstimoteBeaconInfo getFromScanRecord(byte[] scanRecord) {

        // estimotes has the following records: Flags,Unknown Structure: -1,Name,Service Data
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);

        // we will look for record of type -1 (estimote's custom info record)
        for (AdRecord record : records) {
            //Log.debug("  record '%s' has type=%d and length=%d", record.toString(), record.getType(), record.getLength());
            if (record.getType() == -1 && record.getLength() == 26) {
                byte[] data = record.getData();

                // calculate and verify signature
                int signature = (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];

                if( signature != 0x4C000215 ) {
                    Log.error("Sanity Failure: Estimote beacon data signature is wrong!");
                    Log.error("-> bytes: %d %d %d %d", data[0], data[1], data[2], data[3]);
                    return null;
                }

                EstimoteBeaconInfo estimote = new EstimoteBeaconInfo();
                estimote.major = data[20] << 16 | data[21];
                estimote.minor = data[22] << 16 | data[23];
                estimote.measuredPower = (int) data[24];

                Log.debug("major=%d minor=%d measuredPower=%d", estimote.major, estimote.minor, estimote.measuredPower);

                return estimote;
            }
        }

        return null;
    }
}
