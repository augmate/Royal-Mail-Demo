package com.augmate.sample.counter;

import android.content.Context;

import com.augmate.sample.AugmateApplication;

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

    private static BinManager sharedManager;
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
        read();
    }

    private void save() {
        try {
            FileOutputStream out = AugmateApplication.getContext().openFileOutput(TAG, Context.MODE_PRIVATE);
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(mBins);
        } catch (Throwable t) {
        }
    }

    private void read() {
        try {
            FileInputStream out = AugmateApplication.getContext().openFileInput(TAG);
            ObjectInputStream objectOut = new ObjectInputStream(out);
            mBins = (ArrayList<BinModel>) objectOut.readObject();
        } catch (Throwable ignored) {
            mBins = new ArrayList<>();
            save();
        }
    }
}
