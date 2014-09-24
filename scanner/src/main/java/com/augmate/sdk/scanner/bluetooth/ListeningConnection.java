package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.text.TextUtils;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

class ListeningConnection implements Runnable {
    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private CountDownLatch threadExitSignal = new CountDownLatch(1);
    private BluetoothServerSocket listeningServer;
    private BluetoothSocket listeningSocket;
    private String accumulationBuffer = "";
    private Context parentContext;

    private boolean alive = true;

    public ListeningConnection(Context parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public void run() {

        while(alive) {
            try {
                BluetoothAdapter bluetoothAdapter = ((BluetoothManager) parentContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

                bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.setName("GlassTester");

                // long-running method
                // it could have been interrupted by the shutdown request
                // in which case we need to break out of the loop immediately
                //tryRestorePairedDeviceConnection(bluetoothAdapter);
                //if(!alive) return;

                // if we couldn't create an outgoing connection to a pre-bonded device
                // listen for an incoming connection

                Log.debug("Opening listening socket..");
                listeningServer = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Server", UUID_SPP);

                Log.debug("Waiting for connection..");
                listeningSocket = listeningServer.accept();

                onConnected(listeningSocket.getRemoteDevice(), listeningSocket);

                Thread.sleep(500);

            } catch (IOException e) {
                Log.exception(e, "Error while listening for an incoming bluetooth connection");
            } catch (InterruptedException e) {
                Log.exception(e, "Caught exception while napping");
            }

            parentContext.sendBroadcast(new Intent(BluetoothSimpleService.ACTION_SCANNER_DISCONNECTED));
        }

        Log.debug("Bluetooth barcode listener thread exiting.");
        threadExitSignal.countDown();
    }

    // this is hairy
    private boolean tryRestorePairedDeviceConnection(BluetoothAdapter bluetoothAdapter) {
        // try to connect back to already-paired devices
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if(!BluetoothDeviceScanner.deviceIsWhitelisted(device.getAddress()))
                continue;

            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

            Log.debug("Trying bonded device: %s (%s)", remoteDevice.getName(), remoteDevice.getAddress());

            ParcelUuid[] remoteServiceUuids = remoteDevice.getUuids();
            if(remoteServiceUuids == null) {
                // device is probably offline and there is no cached services list
                Log.warn("Sanity Failure: device '%s' has no services available!", remoteDevice.getName());
                return true;
            }

            UUID bestService = BluetoothBarcodeConnection.findBestService(remoteServiceUuids);
            if(bestService != null) {
                BluetoothSocket socket;

                // we have two different failures with different responses
                //   1) connection failed to establish - try another device
                //   2) input stream broke - abort connecting out all-together, get out of here

                // attempt connection
                try {
                    socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(bestService);
                    socket.connect();
                } catch (IOException e) {
                    Log.exception(e, "Could not connect to pre-bonded device.");
                    continue;
                }

                // listen on input-stream
                try {
                    onConnected(remoteDevice, socket);
                } catch (IOException e) {
                    Log.exception(e, "Lost outgoing connection to pre-bonded device.");
                    return false;
                }
            }
        }
        return false;
    }

    private void onConnected(BluetoothDevice remoteDevice, BluetoothSocket socket) throws IOException {
        Log.debug("Accepted connection from '%s' @ %s", remoteDevice.getName(), remoteDevice.getAddress());

        // broadcast new connection state
        parentContext.sendBroadcast(new Intent(BluetoothSimpleService.ACTION_SCANNER_CONNECTED)
                .putExtra(BluetoothSimpleService.EXTRA_BARCODE_SCANNER_DEVICE, remoteDevice));

        InputStream inputStream = socket.getInputStream();

        int read;
        byte[] buffer = new byte[128];

        try {
            while ((read = inputStream.read(buffer)) >= 0) {
                onNewData(read, buffer);
            }
        } catch (IOException exception) {
            Log.debug("Barcode streamer interrupted: %s", exception.getMessage());
        }
    }

    private void onNewData(int read, byte[] buffer) {
        String newData = new String(buffer, 0, read);
        accumulationBuffer += newData;

        tryParsingAccumulationBuffer();
    }

    private void tryParsingAccumulationBuffer() {

        // normalize line-endings
        accumulationBuffer = accumulationBuffer.replace("\r\n", "\n");
        accumulationBuffer = accumulationBuffer.replace("\r", "\n"); // scanfob likes to use \r

        // wait until we have a terminator
        int indexOfEOL = accumulationBuffer.indexOf("\n");
        if(indexOfEOL == -1)
            return;

        // grab substring without normalized terminator
        String substring = accumulationBuffer.substring(0, indexOfEOL);

        // advance buffer in case of partials
        accumulationBuffer = accumulationBuffer.substring(indexOfEOL+1);

        /**
         * typical formats
         * scanfob 2006
         *   default: prefix=STX, suffix=CR
         *                   0x02, 0x0A
         *   strangely their CR is actually \n, but it can sometimes be \r
         */

        ArrayList<String> bytes = new ArrayList<>();
        for (int i = 0; i < indexOfEOL; i++) {
            bytes.add(String.format("%X", (byte) substring.charAt(i)));
        }
        Log.debug("Raw input: [%s] (%d bytes)", TextUtils.join(",", bytes), indexOfEOL);

        // try to parse stream

        // Scanfob STX+CR
        if (substring.charAt(0) == 0x02) {
            substring = substring.substring(1);
            Log.debug("Decoded Scanfob STX+CR format: [%s]", substring);

            // broadcast scanned code
            parentContext.sendBroadcast(
                    new Intent(BluetoothSimpleService.ACTION_BARCODE_SCANNED)
                            .putExtra(BluetoothSimpleService.EXTRA_BARCODE_STRING, substring)
            );
        }
    }

    /**
     * can be called from any thread
     */
    public void shutdown() {
        alive = false;

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

        try {
            Log.debug("Waiting on thread-exit..");
            threadExitSignal.await(1000, TimeUnit.MILLISECONDS);
            Log.debug("Waiting on thread-exit.. DONE");
        } catch (InterruptedException e) {
            Log.exception(e, "Interrupted waiting for thread-exit");
        }
    }
}
