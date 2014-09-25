package com.augmate.nx.scanners.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.OutgoingConnector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class PairActivity extends Activity implements IBluetoothScannerEvents {
    private OutgoingConnector connector = new OutgoingConnector(this);

    // captures keys before UI elements can steal them :)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            Log.debug("User requested scanner reconnect.");
            connector.reconnect();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    // NOTE: bindService/unbindService can be moved to onResume/onPause to disconnect from the scanner on app sleep
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        Log.start(this);

        // prep beacon ranger but don't start it until we have a scanner bonded and connected.
        // ranging seems to interfere with ordinary bluetooth connections :(
        connector.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");
        connector.stop();
    }

    @Override
    public void onBtScannerResult(String barcode) {
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText(
                String.format("Scanned barcode: [%s]\nat %s", barcode, DateTime.now().toString(DateTimeFormat.mediumDateTime()))
        );
    }

    @Override
    public void onBtScannerConnecting() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connecting");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFFFF00);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerConnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerDisconnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
        ((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }
}
