package com.augmate.beacontest.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import com.augmate.sdk.logger.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;

public class BluetoothPairingActivity extends Activity {
    private Timer timer = new Timer();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcastReceiver mReceiver;

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.debug("Found device: [%s] @ [%s]", device.getName(), device.getAddress());

                if (
                        "00:1C:97:90:8A:4F".equals(device.getAddress()) // ecom handheld barcode scanner
                        || "F8:A9:D0:AC:51:77".equals(device.getAddress()) // alex's phone
                    ) {
                    Log.debug("device.getBondState= " + device.getBondState());
                    Log.debug("device.getBluetoothClass = " + device.getBluetoothClass());
                    Log.debug("device.getType = " + device.getType());
                    Log.debug("device.getName = " + device.getName());

                    try {
                        Log.debug("attempting to unpair device..");
                        device.getClass().getMethod("removeBond", (Class[]) null).invoke(device, (Object[]) null);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        Log.exception(e, "Error unpairing device!");
                    }

                    Log.debug("starting pairing..");
                    boolean bCreateBond = device.createBond();
                    Log.debug("bCreateBond = " + bCreateBond);
                }
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                Log.debug("previous bond state=" + prevBondState);
                Log.debug("current bond state=" + bondState);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.debug("got device = " + device);

                if (bondState == BluetoothDevice.BOND_BONDING) {
                    Log.debug("confirming pairing.."); // not sure if this is necessary
                    device.setPairingConfirmation(true);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing..");
        this.unregisterReceiver(mReceiver);

        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug("Resuming..");

        final BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        bluetoothAdapter.startDiscovery();

        mReceiver = new BluetoothBroadcastReceiver();

        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }
}
