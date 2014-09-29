package com.augmate.sdk.scanner.bluetooth;

public class ServiceEvents {
    // callbacks from dedicated thread
    public static final String ACTION_BARCODE_SCANNED = "com.augmate.sdk.scanner.bluetooth.action.SCANNED";
    public static final String ACTION_SCANNER_FOUND = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_FOUND";
    public static final String ACTION_SCANNER_CONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_CONNECTED";
    public static final String ACTION_SCANNER_DISCONNECTED = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_DISCONNECTED";
    public static final String EXTRA_BARCODE_STRING = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_STRING";
    public static final String EXTRA_BARCODE_SCANNER_DEVICE = "com.augmate.sdk.scanner.bluetooth.extra.BARCODE_SCANNER";
    public static final String ACTION_SCANNER_CONNECTING = "com.augmate.sdk.scanner.bluetooth.action.BARCODE_SCANNER_CONNECTING";
}
