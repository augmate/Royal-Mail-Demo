package com.augmate.sdk.data;

import android.content.Context;
import com.parse.Parse;
import com.parse.ParseObject;

public class AugmateData {

    private static boolean initialized = false;

    public AugmateData(Context context) {

        synchronized (this) {
            if (AugmateData.initialized == false) {
                Parse.initialize(context, "dXdrCRra51kK5zV2LT7fxT3Q1dnYOM79AmxXvguP", "6On5YIMRg6VAH7w7Svy6WnYmt2fYqBU5qSU0OQEE");
                AugmateData.initialized = true;
            }
        }

    }

    public void test() {
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }
}
