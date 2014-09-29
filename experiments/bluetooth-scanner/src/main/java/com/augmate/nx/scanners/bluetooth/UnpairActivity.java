package com.augmate.nx.scanners.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.reflect.Method;

/**
 * Unpair all bonded devices
 */
public class UnpairActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        Log.start(this);

        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText(
                String.format("Unpairing Devices..")
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        int numOfBondedDevices = bluetoothAdapter.getBondedDevices().size();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            removeBond(device);
        }

        // at this point the devices haven't finished unpairing
        // we would have to listen and handle messages to get confirmation that the unpairing completed
        // but we can assume for now and give it a second
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText(
                String.format("Unpairing %d devices\nat %s", numOfBondedDevices, DateTime.now().toString(DateTimeFormat.mediumDateTime()))
        );
    }

    private boolean removeBond(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            return true;
        } catch (Exception e) {
            Log.exception(e, "Failed removing bond on device");
        }
        return false;
    }
}
