package com.augmate.sdk.scanner;

/**
 * Provides barcode decoding results to a parent Activity (on its own thread)
 * Must be implemented by parent Activity
 */
public interface IScannerResultListener {
    public void onBarcodeScanSuccess(String result);
}
