package com.augmate.sdk.scanner.bluetooth;

public interface IBluetoothScannerEvents {
    public void onBtScannerResult(String barcode);

    public void onBtScannerConnecting();

    public void onBtScannerConnected();

    public void onBtScannerDisconnected();
}
