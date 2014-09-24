package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UUIDs:
 *  00001124-0000-1000-8000-00805f9b34fb = HID (doesn't seem to work)
 *  00001101-0000-1000-8000-00805f9b34fb = SPP (works on android, but scanner must be in discoverable/connectable mode)
 *  00000000-0000-1000-8000-00805f9b34fb = ???
 *
 *  BluetoothClasses:
 *      Scanfob (opn-2006) = 114 = BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA
 */

class BluetoothBarcodeConnection implements Runnable {
    private String accumulationBuffer = "";
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID service;
    private Context parentContext;
    private CountDownLatch threadExitSignal = new CountDownLatch(1);

    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // definitely works
    public static final UUID UUID_HID = UUID.fromString("00001124-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket listeningSocket;
    private BluetoothServerSocket listeningServer;

    public static UUID findBestService(ParcelUuid[] uuids) {
        for (Parcelable parceableUuid : uuids) {
            UUID uuid = UUID.fromString(parceableUuid.toString());

            if(UUID_SPP.compareTo(uuid) == 0) {
                Log.debug("  found SPP service: %s", uuid.toString());
                return uuid;
            }

            if(UUID_HID.compareTo(uuid) == 0) {
                Log.debug("  found HID service: %s", uuid.toString());
                return uuid;
            }

            //Log.debug("  unknown service: %s", uuid.toString());
        }

        return null;
    }

    public BluetoothBarcodeConnection(BluetoothDevice device, UUID service, Context parentContext) {
        this.parentContext = parentContext;
        this.service = service;
        this.device = device;
    }

    static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    public void run() {

        try {
            BluetoothAdapter bluetoothAdapter = ((BluetoothManager) parentContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

            Log.debug("Opening listening socket..");
            listeningServer = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Log.debug("Waiting for connection..");
            listeningSocket = listeningServer.accept();
            Log.debug("Accepted connection from '%s' @ %s", listeningSocket.getRemoteDevice().getName(), listeningSocket.getRemoteDevice().getAddress());
            InputStream inputStream = listeningSocket.getInputStream();

            int read;
            byte[] buffer = new byte[128];

            try {
                while ((read = inputStream.read(buffer)) >= 0) {

                    /**
                     * typical formats
                     * scanfob 2006
                     *   default: prefix=STX, suffix=CR
                     *                   0x02, 0x0A
                     *   strangely their CR is actuall \n, but it can sometimes be \r
                     */

                    ArrayList<String> bytes = new ArrayList<>();
                    for (int i = 0; i < read; i++) {
                        bytes.add(String.format("%X", buffer[i]));
                    }
                    Log.debug("Raw input: [%s] (%d bytes)", TextUtils.join(",", bytes), read);

                    if(buffer[0] == 0x02 && buffer[read-1] == 0x0A) {
                        String value = new String(Arrays.copyOfRange(buffer, 1, read-1), "ISO-8859-1");
                        Log.debug("Decoded Scanfob STX+CR format; result: [%s]", value);
                    }


                }
            } catch (IOException exception) {
                Log.debug("Barcode streamer interrupted: %s", exception.getMessage());
            }

        } catch (IOException e) {
            Log.exception(e, "Error while listening for an incoming bluetooth connection");
        }

//        Log.debug("Connecting to: %s + %s", device.getAddress(), service.toString());
//
//        InputStream stream = null;
//
//        try {
//            socket = device.createRfcommSocketToServiceRecord(service);
//            socket.connect();
//            stream = socket.getInputStream();
//        } catch (IOException e) {
//            Log.exception(e, "Could not open rfcomm socket and stream to device.");
//        }
//
//        if (socket != null && stream != null) {
//            // broadcast that we connected to the device
//            parentContext.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));
//
//            // start processing input stream
//            processBarcodeScannerStream(stream);
//        }
//
//        socket = null;
//
//        // broadcast that we disconnected from the device and are no longer processing its stream
//        parentContext.sendBroadcast(new Intent(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED).putExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_SCANNER_DEVICE, device));

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
        String newData = new String(buffer, 0, read);
        accumulationBuffer += newData;
        //Log.debug("received from scanner: [%s] (%d bytes)", newData, read);

        // normalize line-endings between various scanners
        accumulationBuffer = accumulationBuffer.replace("\r\n", "\n");
        accumulationBuffer = accumulationBuffer.replace("\r", "\n"); // scanfob likes to use \r

        if (accumulationBuffer.contains("\n")) {
            String barcode = accumulationBuffer.substring(0, accumulationBuffer.indexOf("\n"));
            accumulationBuffer = accumulationBuffer.substring(accumulationBuffer.indexOf("\n")).trim();
            Log.debug("Scanned barcode: [%s]", barcode);

            // broadcast scanned code
            parentContext.sendBroadcast(
                    new Intent(BluetoothComplexService.ACTION_BARCODE_SCANNED)
                            .putExtra(BluetoothComplexService.EXTRA_BARCODE_STRING, barcode)
            );
        }
    }

    /**
     * can be called from any thread
     */
    public void shutdown() {

        if(listeningServer != null) {
            try {
                listeningServer.close();
            } catch (IOException e) {
                Log.exception(e, "Caught while shutting down listening server");
            }
        }

        if(listeningSocket != null) {
            try {
                listeningSocket.close();
            } catch (IOException e) {
                Log.exception(e, "Caught while shutting down listening socket");
            }
        }

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
