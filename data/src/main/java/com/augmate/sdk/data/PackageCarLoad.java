package com.augmate.sdk.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("CarLoadingPackageLoad")
public class PackageCarLoad extends ParseObject{
    public static final String TRACKING_NUMBER_CLASS = "TrackingNumber";
    public static final String LOAD_POSITION_CLASS = "LoadPosition";

    public PackageCarLoad() {}

    public String getTrackingNumber(){
        return getString(TRACKING_NUMBER_CLASS);
    }

    public String getLoadPosition(){
        return getString(LOAD_POSITION_CLASS);
    }

}
