package com.augmate.sdk.scanner.bluetooth;

import com.augmate.sdk.logger.Log;

import java.io.UnsupportedEncodingException;

public class Utils {

    /**
     * straight from the android source code
     * https://android.googlesource.com/platform/frameworks/base/+/ac2c6c3/core/java/android/bluetooth/BluetoothDevice.java
     * why is such handy method @hidden in the android sdk?
     *
     * @param pin four character/digit string
     * @return byte array or null on error
     */
    static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.exception(uee, "UTF-8 not supported?!?");  // this should not happen
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }
}