package com.augmate.apps.counter;

import android.content.Context;
import android.util.Log;
import com.augmate.apps.AugmateApplication;
import com.augmate.sdk.data.AugmateData;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by cesaraguilar on 8/21/14.
 */
public class BinManager {

    public static final String TAG = "BinManager";
    public static final String BINS_CLASS_NAME = "CycleCountingBins";

    private static BinManager sharedManager;
    private static AugmateData augmateData;
    ArrayList<BinModel> mBins;


    public void saveBin(BinModel bin) {
        mBins.add(bin);
        save();
    }

    public static synchronized BinManager getSharedInstance() {
        if (sharedManager == null) {
            sharedManager = new BinManager();
        }
        return sharedManager;
    }

    private BinManager() {
        augmateData = new AugmateData(AugmateApplication.getContext());
        read();
    }

    private void save() {
        try {
            FileOutputStream out = AugmateApplication.getContext().openFileOutput(TAG, Context.MODE_PRIVATE);
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(mBins);

            augmateData.save(BINS_CLASS_NAME, mBins);

        } catch (Throwable t) {
        }
    }

    private void read() {
        try {
            Log.d(getClass().getName(), "Attempting to read local store");

            FileInputStream out = AugmateApplication.getContext().openFileInput(TAG);
            ObjectInputStream objectOut = new ObjectInputStream(out);
            mBins = (ArrayList<BinModel>) objectOut.readObject();

            String binJson = augmateData.read(BINS_CLASS_NAME);
            ArrayList<BinModel> arrayList = new Gson().fromJson(binJson, ArrayList.class);

        } catch (Throwable ignored) {
            Log.d(getClass().getName(), "Reading failed");
            mBins = new ArrayList<>();
            save();
        }
    }
}
