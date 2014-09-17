package com.augmate.sdk.data;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SampleDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SampleAugmateData obj1 = new SampleAugmateData("obj1 in array", 999);
        SampleAugmateData obj2 = new SampleAugmateData("obj2 in array", 99);

        ArrayList<SampleAugmateData> sampleAugmateDatas = new ArrayList<>();
        sampleAugmateDatas.add(obj1);
        sampleAugmateDatas.add(obj2);

        String expected = new Gson().toJson(sampleAugmateDatas);

        AugmateData<PackageCarLoad> augmateData = new AugmateData<>(this);

        augmateData.save("ArrayList", sampleAugmateDatas);
        String readData = augmateData.read("ArrayList");

        Log.d(getClass().getName(), "Does write data equal read data (should be true)? " + expected.equals(readData));

        augmateData.refreshPackageLoadData();

        PackageCarLoad packageCarLoad = new CarLoadingDataStore().findLoadForTrackingNumber("1Z0031340358655382");

        augmateData.find(PackageCarLoad.class, PackageCarLoad.TRACKING_NUMBER_CLASS, "1Z0031340358655382");

        Log.d(getClass().getName(), "(Should be 109) Load position: " + packageCarLoad.getLoadPosition());
    }

    private class CarLoadingDataStore {
        private final AugmateData<PackageCarLoad> augmateData;

        public CarLoadingDataStore() {
            augmateData = new AugmateData<>(SampleDataActivity.this);
        }

        public PackageCarLoad findLoadForTrackingNumber(String trackingNumber) {
            PackageCarLoad packageCarLoad = augmateData.find(PackageCarLoad.class, "TrackingNumber", trackingNumber);

            Log.i(getClass().getName(), packageCarLoad.getLoadPosition());
            Log.i(getClass().getName(), packageCarLoad.getTrackingNumber());

            return packageCarLoad;
        }
    }

    static private class SampleAugmateData {
        public SampleAugmateData(String str, int integer) {
            this.str = str;
            this.integer = integer;
        }

        String str;
        int integer;
    }
}
