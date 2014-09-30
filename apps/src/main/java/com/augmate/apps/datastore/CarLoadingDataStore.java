package com.augmate.apps.datastore;

import android.content.Context;
import com.augmate.sdk.data.AugmateData;
import com.augmate.sdk.data.PackageCarLoad;
import com.augmate.sdk.logger.Log;
import com.parse.ParseException;
import com.parse.SaveCallback;

public class CarLoadingDataStore {
    public static final String TRACKING_NUMBER_KEY = "TrackingNumber";

    private final AugmateData<PackageCarLoad> augmateData;

    public CarLoadingDataStore(Context context) {
        augmateData = new AugmateData<>(context);
    }

    public PackageCarLoad findLoadForTrackingNumber(String trackingNumber) {
        PackageCarLoad packageCarLoad = augmateData.find(PackageCarLoad.class, TRACKING_NUMBER_KEY, trackingNumber);

        if(packageCarLoad == null) {
            packageCarLoad = augmateData.remoteFind(PackageCarLoad.class, TRACKING_NUMBER_KEY, trackingNumber);
            final String loadPosition = packageCarLoad.getLoadPosition();

            pullToCache(PackageCarLoad.LOAD_POSITION_KEY, packageCarLoad.getLoadPosition(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null) {
                        Log.info("done pulling load data for %s to cache", loadPosition);
                    }else{
                        Log.error("Caching failed!" + e.getMessage());
                    }
                }
            });

        }else{
            Log.info("load information for %s available in cache", packageCarLoad.getLoadPosition());
        }

        Log.debug("tracking number %s has load position %s", packageCarLoad.getTrackingNumber(), packageCarLoad.getLoadPosition());

        return packageCarLoad;
    }

    public void pullToCache(String key, String value, SaveCallback callback){
        augmateData.pullToCache(PackageCarLoad.class, key, value, callback);
    }

    public int numberOfPackages() {
        return augmateData.count(PackageCarLoad.class);
    }
}
