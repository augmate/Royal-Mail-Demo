package com.augmate.sdk.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("CarLoadingPackageLoad")
public class PackageCarLoad extends ParseObject{
    public static final String TRACKING_NUMBER_KEY = "TrackingNumber";
    public static final String LOAD_POSITION_KEY = "LoadPosition";

    public PackageCarLoad() {}

    public String getTrackingNumber(){
        return getString(TRACKING_NUMBER_KEY);
    }

    public String getLoadPosition(){
        return getString(LOAD_POSITION_KEY);
    }

}
