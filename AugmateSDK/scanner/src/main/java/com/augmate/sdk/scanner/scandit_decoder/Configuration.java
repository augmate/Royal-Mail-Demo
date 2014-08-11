package com.augmate.sdk.scanner.scandit_decoder;

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
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), "android_id");
            }
        };
    }

    @SuppressLint("SdCardPath")
    public static Configuration createHardcoded() {
        return new Configuration() {
            {
                filesDir = new File("/data/data/com.augmate.cycle.ups/files");
                platformAppId = "com.augmate.cycle.ups";
                deviceId = "672be28ec4178d40";
            }
        };
    }
}