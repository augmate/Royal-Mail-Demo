package com.augmate.apps.datastore;

import android.content.Context;
import android.util.Log;
import com.augmate.sdk.data.AugmateData;
import com.augmate.sdk.data.PackageCarLoad;

public class CarLoadingDataStore {
    private final AugmateData<PackageCarLoad> augmateData;

    public CarLoadingDataStore(Context context) {
        augmateData = new AugmateData<>(context);
    }

    public PackageCarLoad findLoadForTrackingNumber(String trackingNumber) {
        PackageCarLoad packageCarLoad = augmateData.find(PackageCarLoad.class, "TrackingNumber", trackingNumber);

        Log.i(getClass().getName(), packageCarLoad.getLoadPosition());
        Log.i(getClass().getName(), packageCarLoad.getTrackingNumber());

        return packageCarLoad;
    }

    public void refreshCarLoadData(){
        augmateData.refreshPackageLoadData();
    }
}
