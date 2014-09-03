package com.augmate.sdk.data;

import android.content.Context;
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
        testObject.put(PARSE_KEY_PAYLOAD, new Gson().toJson(obj));
        testObject.saveInBackground();
    }

    public String read(String className) {
        ParseObject object = null;

        try {
            object = new ParseQuery<>(className).orderByDescending("updatedAt").getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return object.getString(PARSE_KEY_PAYLOAD);
    }
}
