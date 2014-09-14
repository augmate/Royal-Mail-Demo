package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

class BluetoothBarcodeConnection implements Runnable {
    private String accumulationBuffer = "";
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private Context parentContext;

    public BluetoothBarcodeConnection(BluetoothDevice device, Context parentContext) {
        this.device = device;
        this.parentContext = parentContext;
    }

    @Override
    public void run() {
        Log.debug("Barcode streaming thread entered.");

        InputStream stream = null;

        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            socket.connect();
            stream = socket.getInputStream();
        } catch (IOException e) {
            Log.exception(e, "Could not open rfcomm socket and stream to device.");
        }

        if (socket != null && stream != null) {
            // broadcast that we connected to the device
            parentContext.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));

            // start processing input stream
            processBarcodeScannerStream(stream);
        }

        socket = null;

        // broadcast that we disconnected from the device and are no longer processing its stream
        parentContext.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));

        Log.debug("Barcode streaming thread exiting.");
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
                onReadScannerData(read, buffer);
            }
        } catch (IOException exception) {
            //Log.expected_exception(exception, "Barcode streamer interrupted. (by shutdown request?)");
            Log.debug("Barcode streamer interrupted: %s", exception.getMessage());
        }
    }

    /**
     * accumulate data, split by line-terminator, extract complete-lines as a barcode payload
     *
     * @param read   number of bytes
     * @param buffer byte buffer
     */
    private void onReadScannerData(int read, byte[] buffer) {
        accumulationBuffer += new String(buffer, 0, read);

        if (accumulationBuffer.contains("\r\n")) {
            String barcode = accumulationBuffer.substring(0, accumulationBuffer.indexOf("\r\n"));
            accumulationBuffer = accumulationBuffer.substring(accumulationBuffer.indexOf("\r\n")).trim();
            //Log.debug("Scanned barcode: [%s]", barcode);

            // broadcast scanned code
            parentContext.sendBroadcast(
                    new Intent(BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED)
                            .putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_STRING, barcode)
            );
        }
    }

    /**
     * can be called from any thread
     */
    public void shutdown() {
        if (socket == null)
            return;

        try {
            socket.close();
        } catch (IOException e) {
            // this is expected. we are disrupting the bluetooth socket and input stream.
            Log.exception(e, "Caught exception while closing bluetooth socket.");
        }
    }
}
