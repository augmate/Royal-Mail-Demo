package com.augmate.sdk.data;

import android.content.Context;
import com.augmate.sdk.logger.Log;
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

        Log.debug("Saving data: " + objJson);
    }

    // TODO: update to use T instead of strings
    public String read(String className) {
        ParseObject object = null;

        try {
            object = new ParseQuery<>(className).orderByDescending("updatedAt").getFirst();
        } catch (ParseException e) {
            Log.error(e.getMessage());
        }

        String objJson = object.getString(PARSE_KEY_PAYLOAD);

        Log.debug("Read data: " + objJson);

        return objJson;
    }

    public void pullToCache(Class<T> clazz, String key, String value, SaveCallback callback) {
        List<T> objs = null;

        try {
            objs = new ParseQuery<>(clazz)
                    .whereEqualTo(key, value)
                    .setLimit(1000)
                    .find();
        } catch (ParseException e) {
            Log.error(e.toString());
        }

        Log.info("Caching data for %s->%s (%d elements)", key, value, objs.size());

        ParseObject.pinAllInBackground(objs, callback);
    }

    public T cacheFind(Class<T> clazz, String key, String value){
        T foundObj = null;

        try {
            foundObj = ParseQuery.getQuery(clazz)
                    .fromLocalDatastore()
                    .whereEqualTo(key, value)
                    .setLimit(1000)
                    .getFirst();
        } catch (ParseException e) {
            Log.error(e.toString());
        }

        return foundObj;
    }

    public List<T> cacheFind(Class<T> clazz){
        List<T> foundObjs = null;

        try {
            foundObjs = ParseQuery.getQuery(clazz)
                    .fromLocalDatastore()
                    .setLimit(1000)
                    .find();
        } catch (ParseException e) {
            Log.error(e.toString());
        }

        return foundObjs;
    }

    public int count(Class<T> clazz) {
        int count = -1;

        try {
            count = ParseQuery.getQuery(clazz)
                    .fromLocalDatastore()
                    .count();
        } catch (ParseException e) {
            Log.error(e.toString());
        }

        return count;
    }

    public void clearCache(Class<T> clazz) {
        try {
            List<T> cacheObjs = cacheFind(clazz);

            Log.info("Removing all %s elements from cache (%d elements)", clazz.getName(), cacheObjs.size());

            ParseObject.unpinAll(cacheObjs);
        } catch (ParseException e) {
            Log.error(e.getMessage());
        }
    }
}
