package com.augmate.apps.datastore;

import android.content.Context;
import android.util.Log;
import com.augmate.sdk.data.AugmateData;
import com.augmate.sdk.data.PackageCarLoad;
import com.parse.SaveCallback;

public class CarLoadingDataStore {
    public static final String TRACKING_NUMBER_KEY = "TrackingNumber";

    private final AugmateData<PackageCarLoad> augmateData;

    public CarLoadingDataStore(Context context) {
        augmateData = new AugmateData<>(context);
    }

    public PackageCarLoad findLoadForTrackingNumber(String trackingNumber) {
        PackageCarLoad packageCarLoad = augmateData.find(PackageCarLoad.class, TRACKING_NUMBER_KEY, trackingNumber);

        Log.i(getClass().getName(), packageCarLoad.getLoadPosition());
        Log.i(getClass().getName(), packageCarLoad.getTrackingNumber());

        return packageCarLoad;
    }

    public void pullToCache(SaveCallback callback){
        augmateData.clearCache();
        augmateData.pullToCache(PackageCarLoad.class, callback);
    }

    public int numberOfPackages() {
        return augmateData.count(PackageCarLoad.class);
    }
}
