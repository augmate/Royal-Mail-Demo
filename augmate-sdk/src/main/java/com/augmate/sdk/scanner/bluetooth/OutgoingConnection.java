package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UUIDs:
 * 00001124-0000-1000-8000-00805f9b34fb = HID (doesn't seem to work)
 * 00001101-0000-1000-8000-00805f9b34fb = SPP (works on android, but scanner must be in discoverable/connectable mode)
 * 00000000-0000-1000-8000-00805f9b34fb = ???
 *
 * BluetoothClasses:
 * Scanfob (opn-2006) = 114 = BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA
 */

class OutgoingConnection implements Runnable {
    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // definitely works
    public static final UUID UUID_HID = UUID.fromString("00001124-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID service;
    private Context parentContext;
    private CountDownLatch threadExitSignal = new CountDownLatch(1);

    // parses data comming in from a scanner and broadcasts the result
    private BarcodeStreamParser scannerParser = new BarcodeStreamParser(new BarcodeStreamParser.ResultHandler() {
        @Override
        public void onBarcodeDecoded(String barcode) {
            parentContext.sendBroadcast(
                    new Intent(ServiceEvents.ACTION_BARCODE_SCANNED)
                            .putExtra(ServiceEvents.EXTRA_BARCODE_STRING, barcode)
            );

        }
    });

    public OutgoingConnection(BluetoothDevice device, UUID service, Context parentContext) {
        this.parentContext = parentContext;
        this.service = service;
        this.device = device;
    }

    public static UUID findBestService(ParcelUuid[] uuids) {
        for (Parcelable parceableUuid : uuids) {
            UUID uuid = UUID.fromString(parceableUuid.toString());

            if (UUID_SPP.compareTo(uuid) == 0) {
                Log.debug("  found SPP service: %s", uuid.toString());
                return uuid;
            }

            if (UUID_HID.compareTo(uuid) == 0) {
                Log.debug("  found HID service: %s", uuid.toString());
                return uuid;
            }
        }

        return null;
    }

    @Override
    public void run() {
        Log.debug("Connecting to: %s + %s", device.getAddress(), service.toString());

        InputStream stream = null;

        try {
            socket = device.createRfcommSocketToServiceRecord(service);
            socket.connect();
            stream = socket.getInputStream();
        } catch (IOException e) {
            Log.exception(e, "Could not open rfcomm socket and stream to device.");
        }

        if (socket != null && stream != null) {
            // broadcast that we connected to the device
            parentContext.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_CONNECTED).putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, device));

            // start processing input stream
            processBarcodeScannerStream(stream);
        }

        socket = null;

        // broadcast that we disconnected from the device and are no longer processing its stream
        parentContext.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_DISCONNECTED).putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, device));

        Log.debug("Barcode streaming thread exiting.");
        threadExitSignal.countDown();
    }

    /**
     * loop on the barcode input stream until interrupted
     *
     * @param stream raw scanner data stream
     */
    private void processBarcodeScannerStream(InputStream stream) {
        int read;
        byte[] buffer = new byte[128];

        // read in chunks of bytes from the stream
        // accumulate in a buffer and parse it every time looking for a new-line terminator
        // a complete barcode payload is represented by a single-line string
        // on service shutdown, the stream will be closed and a non-fatal exception will be thrown here

        try {
            while ((read = stream.read(buffer)) >= 0) {
                scannerParser.onNewData(read, buffer);
            }
        } catch (IOException exception) {
            //Log.expected_exception(exception, "Barcode streamer interrupted. (by shutdown request?)");
            Log.debug("Barcode streamer interrupted: %s", exception.getMessage());
        }
    }

    /**
     * can be called from any thread
     */
    public void shutdown() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // this is expected. we are disrupting the bluetooth socket and input stream.
                Log.exception(e, "Caught exception while closing bluetooth socket.");
            }
        }

        try {
            Log.debug("Waiting on thread-exit..");
            threadExitSignal.await(1000, TimeUnit.MILLISECONDS);
            Log.debug("Waiting on thread-exit.. DONE");
        } catch (InterruptedException e) {
            Log.exception(e, "Interrupted waiting for thread-exit");
        }
    }
}
