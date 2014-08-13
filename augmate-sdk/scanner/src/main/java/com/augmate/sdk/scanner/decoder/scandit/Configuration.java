package com.augmate.sdk.scanner.decoder.scandit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import java.io.File;

/**
 * Configuration for Scandit Decoder Wrapper
 */
public class Configuration {
    public File filesDir;
    public String platformAppId;
    public String deviceId;

    public static Configuration createFromContext(final Context ctx) {
        return new Configuration() {
            {
                filesDir = ctx.getFilesDir();
                platformAppId = ctx.getPackageName();
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        };
    }

    @SuppressLint("SdCardPath")
    public static Configuration createHardcoded() {
        return new Configuration() {
            {
                filesDir = new File("/data/data/com.augmate.counter/files");
                platformAppId = "com.augmate.counter";
                deviceId = "672be28ec4178d40";
            }
        };
    }
}
