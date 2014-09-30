package com.augmate.sdk.data;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.parse.*;

import java.util.List;

public class AugmateData<T extends ParseObject>{

    public static final String PARSE_KEY_PAYLOAD = "payload";
    private static boolean initialized = false;

    public AugmateData(Context context) {
        synchronized (this) {
            if (AugmateData.initialized == false) {
                Parse.enableLocalDatastore(context);
                ParseObject.registerSubclass(PackageCarLoad.class);
                Parse.initialize(context, "dXdrCRra51kK5zV2LT7fxT3Q1dnYOM79AmxXvguP", "6On5YIMRg6VAH7w7Svy6WnYmt2fYqBU5qSU0OQEE");
                AugmateData.initialized = true;
            }
        }
    }

    // TODO: update to use T instead of strings
    public void save(String className, Object obj) {
        ParseObject testObject = new ParseObject(className);
        String objJson = new Gson().toJson(obj);
        testObject.put(PARSE_KEY_PAYLOAD, objJson);
        testObject.saveInBackground();

        Log.d(this.getClass().getName(), "Saving data: " + objJson);
    }

    // TODO: update to use T instead of strings
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

    public void pullToCache(Class<T> clazz, String key, String value, SaveCallback callback) {
        List<T> packageLoads = null;

        try {
            packageLoads = new ParseQuery<>(clazz)
                    .whereEqualTo(key, value)
                    .find();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        ParseObject.pinAllInBackground(packageLoads, callback);
    }

    public T find(Class<T> clazz, String key, String value){
        T foundPackage = null;

        try {
            foundPackage = ParseQuery.getQuery(clazz)
                    .fromLocalDatastore()
                    .whereEqualTo(key, value)
                    .getFirst();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        return foundPackage;
    }

    public T remoteFind(Class<T> clazz, String key, String value){
        T foundPackage = null;

        try {
            foundPackage = ParseQuery.getQuery(clazz)
                    .whereEqualTo(key, value)
                    .getFirst();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        return foundPackage;
    }


    public int count(Class<T> clazz) {
        int count = -1;

        try {
            count = ParseQuery.getQuery(clazz).fromLocalDatastore().count();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        return count;
    }

    public void clearCache() {
        ParseQuery.clearAllCachedResults();
    }
}
