package com.augmate.sdk.scanner.bluetooth;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import com.augmate.sdk.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class IncomingConnection implements Runnable {
    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private CountDownLatch threadExitSignal = new CountDownLatch(1);
    private BluetoothServerSocket listeningServer;
    private BluetoothSocket listeningSocket;
    private Context parentContext;
    private boolean alive = true;

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

    public IncomingConnection(Context parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public void run() {

        while (alive) {
            try {
                BluetoothAdapter bluetoothAdapter = ((BluetoothManager) parentContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                bluetoothAdapter.cancelDiscovery();

                Log.debug("Listening for SPP connections..");
                listeningServer = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", UUID_SPP);
                listeningSocket = listeningServer.accept();

                onConnected(listeningSocket.getRemoteDevice(), listeningSocket);

                listeningSocket.close();
                listeningServer.close();

                Thread.sleep(500);

            } catch (IOException e) {
                Log.exception(e, "Error while listening for an incoming bluetooth connection");
            } catch (InterruptedException e) {
                Log.exception(e, "Caught exception while napping");
            }

            parentContext.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_DISCONNECTED));
        }

        Log.debug("Bluetooth barcode listener thread exiting.");
        threadExitSignal.countDown();
    }

    private void onConnected(BluetoothDevice remoteDevice, BluetoothSocket socket) throws IOException {
        Log.debug("Accepted connection from '%s' @ %s", remoteDevice.getName(), remoteDevice.getAddress());

        // broadcast new connection state
        parentContext.sendBroadcast(new Intent(ServiceEvents.ACTION_SCANNER_CONNECTED)
                .putExtra(ServiceEvents.EXTRA_BARCODE_SCANNER_DEVICE, remoteDevice));

        int read;
        byte[] buffer = new byte[128];
        InputStream inputStream = socket.getInputStream();

        try {
            while ((read = inputStream.read(buffer)) >= 0) {
                scannerParser.onNewData(read, buffer);
            }
        } catch (IOException exception) {
            Log.debug("Barcode streamer interrupted: %s", exception.getMessage());
        }
    }

    /**
     * can be called from any thread
     */
    public void shutdown() {
        alive = false;

        if (listeningServer != null) {
            try {
                listeningServer.close();
            } catch (IOException e) {
                Log.exception(e, "Caught while shutting down listening server");
            }
        }

        if (listeningSocket != null) {
            try {
                listeningSocket.close();
            } catch (IOException e) {
                Log.exception(e, "Caught while shutting down listening socket");
            }
        }

        try {
            threadExitSignal.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.exception(e, "Interrupted waiting for thread-exit");
        }
    }
}
