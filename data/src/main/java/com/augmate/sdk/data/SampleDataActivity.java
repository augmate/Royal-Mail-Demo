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
