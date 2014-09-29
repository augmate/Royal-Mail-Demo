package com.augmate.nx.scanners.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.Utils;

import java.lang.reflect.Method;

public class DeviceBondingActivity extends Activity {
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    onDeviceBondStateChanged(bondState);
                    break;

                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    Log.debug("Received device pairing request. Sending pin number and pairing confirmation..");
                    device.setPin(Utils.convertPinToBytes("1234"));
                    device.setPairingConfirmation(true);
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.debug("Device %s connection established", device.getAddress());
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.debug("Device %s disconect requested", device.getAddress());
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.debug("Device %s disconnected", device.getAddress());
                    break;
            }
        }
    };
    int attempts = 0;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();
    private String address;
    private final Runnable pairingAttempt = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("bt-bond");
            Log.debug("Try to pair with device: %s", address);

            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
            boolean bond = remoteDevice.createBond();
            Log.debug("Bonding began: %s", bond ? "true" : "false");
        }
    };

    private void retryBonding() {
        handler.postDelayed(pairingAttempt, 350);
    }

    private void finishBonding() {
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        remoteDevice.setPin(Utils.convertPinToBytes("1234"));
        remoteDevice.setPairingConfirmation(true);
        setResult(RESULT_OK);
        finish();
    }

    private void onDeviceBondStateChanged(int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.scannerBondingStatus)).setText("Not bonded with " + address + "\nPlease put the scanner in discoverable mode");
                        retryBonding();
                    }
                });

                Log.debug("Device %s not paired.", address);
                break;
            case BluetoothDevice.BOND_BONDING:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        attempts++;
                        ((TextView) findViewById(R.id.scannerBondingStatus)).setText("Bond with " + address + " attempts=" + attempts);
                    }
                });

                Log.debug("Device %s attempting pairing. Attempt #%d", address, attempts);
                break;
            case BluetoothDevice.BOND_BONDED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.scannerBondingStatus)).setText("Successfully bonded with " + address);
                        finishBonding();
                    }
                });

                Log.debug("Device %s pairing successful.", address);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bond);
        Log.start(this);

        address = getIntent().getStringExtra("address");
        Log.debug("Starting pairing sequence with %s", address);

        ((TextView) findViewById(R.id.scannerBondingStatus)).setText("Attempting to bond with " + address);

        bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);

        if (remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            // device is already bonded
            Log.debug("Device is already bonded. Returning early.");
            setResult(RESULT_OK);
            finish();
            return;
        }

        handler.post(pairingAttempt);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
