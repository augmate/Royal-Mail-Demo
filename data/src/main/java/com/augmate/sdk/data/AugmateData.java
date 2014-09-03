package com.augmate.sdk.data;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AugmateData {

    public static final String PARSE_KEY_PAYLOAD = "payload";
    private static boolean initialized = false;

    public AugmateData(Context context) {
        synchronized (this) {
            if (AugmateData.initialized == false) {
                Parse.initialize(context, "dXdrCRra51kK5zV2LT7fxT3Q1dnYOM79AmxXvguP", "6On5YIMRg6VAH7w7Svy6WnYmt2fYqBU5qSU0OQEE");
                AugmateData.initialized = true;
            }
        }
    }

    public void save(String className, Object obj) {
        ParseObject testObject = new ParseObject(className);
        String objJson = new Gson().toJson(obj);
        testObject.put(PARSE_KEY_PAYLOAD, objJson);
        testObject.saveInBackground();

        Log.d(this.getClass().getName(), "Saving data: " + objJson);
    }

    public String read(String className) {
        ParseObject object = null;

        try {
            object = new ParseQuery<>(className).orderByDescending("updatedAt").getFirst();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }

        String objJson = object.getString(PARSE_KEY_PAYLOAD);

        Log.d(this.getClass().getName(), "Read data: " + objJson);

        return objJson;
    }
}
