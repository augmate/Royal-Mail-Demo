package com.augmate.beacontest.app;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.BluetoothBarcodeScannerService;

public class BluetoothPairingActivity extends Activity {
    private BluetoothBarcodeScannerService bluetoothScannerService;
    private ServiceConnection bluetoothScannerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothScannerService = ((BluetoothBarcodeScannerService.ScannerBinder) service).getService();
            Log.debug("Service connected: " + name.flattenToShortString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.debug("Service disconnected: " + name.flattenToShortString());
            bluetoothScannerService = null;
        }
    };
    /**
     * Receive simple messages from BluetoothBarcodeScannerService
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED:
                    String barcode = intent.getStringExtra(BluetoothBarcodeScannerService.EXTRA_BARCODE_STRING);
                    Log.debug("Received barcode value: [%s]", barcode);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).append(String.format("\nScanned barcode: [%s]", barcode));
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).append("\nScanner Connected");
                    break;

                case BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED:
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
                    ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
                    ((TextView) findViewById(R.id.barcodeScannerResults)).append("\nScanner Disconnected");
                    break;

                default:
                    Log.warn("Unhandled intent action: %s", action);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // keys bound for testing on glass and phone
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MENU) {
            if (bluetoothScannerService != null)
                bluetoothScannerService.reconnect();
        }

        return super.onKeyDown(keyCode, event);
    }

    // NOTE: bindService/unbindService can be moved to onResume/onPause to disconnect from the scanner on app sleep

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        Log.start(this);

        ((TextView) findViewById(R.id.barcodeScannerResults)).setMovementMethod(new ScrollingMovementMethod());

        Log.debug("Binding from scanner service.");
        bindService(new Intent(this, BluetoothBarcodeScannerService.class), bluetoothScannerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");
        unbindService(bluetoothScannerConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug("Resuming");

        // register for scanner notifications
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_BARCODE_SCANNED)); // barcode scanned
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_CONNECTED)); // barcode-scanner connected
        this.registerReceiver(receiver, new IntentFilter(BluetoothBarcodeScannerService.ACTION_SCANNER_DISCONNECTED)); // barcode-scanner disconnected
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing");

        // unregister from scanner notifications
        unregisterReceiver(receiver);
    }
}
