package com.augmate.sdk.data;

import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TestDataActivity extends Activity {

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

        new AugmateData(this).save("ArrayList", sampleAugmateDatas);
        String readData = new AugmateData(this).read("ArrayList");

        assert(expected.equals(readData));
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
