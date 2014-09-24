package com.augmate.apps.nonretailtouching;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.BluetoothScannerConnector;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;

public class NonRetailTouchActivity extends BaseActivity implements IBluetoothScannerEvents {

    private BluetoothScannerConnector bluetoothScannerConnector = new BluetoothScannerConnector(this);
    private NutsApiService nutsApi = new RestAdapter.Builder().setEndpoint("http://nuts.googlex.augmate.com:6969/").build().create(NutsApiService.class);

    private ArrayList<String> recordedBarcodes = new ArrayList<>();
    private ArrayList<String> submittedBarcodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_retail_touch);

        bluetoothScannerConnector.start();
    }

    private void processResultFromScan(String result) {
        if (!recordedBarcodes.contains(result)) {
            recordedBarcodes.add(result);

            Log.debug("-> unique scanning result: [%s] (array size: %d)", result, recordedBarcodes.size());

            // we have a new unique barcode
            TextView nrtCounter = (TextView) findViewById(R.id.nrt_counter);
            nrtCounter.setText("" + recordedBarcodes.size());
            nrtCounter.setTextColor(0xFFFFFF00);

            queueBarcodeCommit(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bluetoothScannerConnector.stop();
    }

    /**
     * must only be called when we have a unique barcode to submit
     * @param barcode String
     */
    private void queueBarcodeCommit(final String barcode) {
        if(submittedBarcodes.contains(barcode))
            return;

        submittedBarcodes.add(barcode);

        NrtScannerFragment nrtScannerFragment = (NrtScannerFragment) getFragmentManager().findFragmentById(R.id.scanner);
        nrtScannerFragment.onSuccess();

        SoundHelper.success(getBaseContext());

        nutsApi.touchNonRetailPiece(barcode, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                Log.debug("Got response from server: code=" + response.getStatus());

                if(!submittedBarcodes.contains(barcode))
                    return;

                submittedBarcodes.remove(barcode);

                if (submittedBarcodes.size() == 0) {
                    Log.debug("Synced with Nuts API");
                    TextView nrtCounter = (TextView) findViewById(R.id.nrt_counter);
                    nrtCounter.setTextColor(0xFF00FF00);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.error("Nuts API touch failed: " + error);

                // TODO: queue up another nuts api call after a timeout (500ms)
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // reset the barcode counts and pending
            Log.debug("User requested counter reset");

            recordedBarcodes.clear();
            submittedBarcodes.clear();

            SoundHelper.dismiss(getBaseContext());

            TextView nrtCounter = (TextView) findViewById(R.id.nrt_counter);
            nrtCounter.setText("0");
            nrtCounter.setTextColor(0xFF00FF00);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBtScannerResult(String barcode) {
        Log.debug("NRT - Received scan from barcode %s", barcode);

        ((TextView)findViewById(R.id.scan_content_view)).setText(barcode);

        processResultFromScan(barcode);
    }

    @Override
    public void onBtScannerConnecting() {
        Log.debug("NRT - BT Scanner Connecting...");
    }

    @Override
    public void onBtScannerConnected() {
        Log.debug("NRT - BT Scanner Connected!");
    }

    @Override
    public void onBtScannerDisconnected() {
        Log.debug("NRT - BT Scanner Disconnected!");
    }
}