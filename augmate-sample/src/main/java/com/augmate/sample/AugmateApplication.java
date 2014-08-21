package com.augmate.sample;

import android.content.Context;

import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.DecoderManager;
import com.augmate.sdk.scanner.decoder.scandit.Configuration;

public class AugmateApplication extends android.app.Application {

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        Log.start(this);
        Log.debug("Application started");
        // if using the scandit scanner, configure it here
        DecoderManager.ScanditConfiguration = Configuration.createFromContext(getBaseContext());
    }

    // never called in production
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.debug("Application ended");
        Log.shutdown();
    }
}
