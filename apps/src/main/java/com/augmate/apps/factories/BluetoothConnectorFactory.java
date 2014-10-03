package com.augmate.apps.factories;

import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;
import com.google.inject.assistedinject.Assisted;

public interface BluetoothConnectorFactory {
    IncomingConnector createIncomingConnector(@Assisted IBluetoothScannerEvents bluetoothListener);
}
