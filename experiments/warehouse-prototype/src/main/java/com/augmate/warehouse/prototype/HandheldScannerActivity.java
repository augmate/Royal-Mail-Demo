package com.augmate.warehouse.prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.google.android.glass.media.Sounds;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import roboguice.inject.InjectView;

/**
 * @author James Davis (Fuzz)
 */
public class HandheldScannerActivity extends Activity implements IBluetoothScannerEvents {

    private IncomingConnector incomingConnector = new IncomingConnector(this);
    public static final String BARCODE_STRING = "bcs";
    public static final int RESULT_TIMED_OUT = 10;
    public static final int RESULT_ERROR = 11;

    boolean success = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            Log.debug("User requested scanner reconnect.");
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_handheld);
        incomingConnector.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!success) {
                    onError(true);
                }
            }
        }, 10000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");

        incomingConnector.stop();
    }



    @Override
    public void onBackPressed() {
        success = true;
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        playSoundEffect(Sounds.DISMISSED);
        finish();
    }

    public void onError(boolean timedOut){
        Intent resultIntent = new Intent();
        setResult(timedOut? RESULT_TIMED_OUT : RESULT_ERROR, resultIntent);
        playSoundEffect(Sounds.ERROR);
        finish();
    }

    private AudioManager mAudioManager;
    protected void playSoundEffect(int soundId) {
        if (mAudioManager == null){
            mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        }
        mAudioManager.playSoundEffect(soundId);
    }

    @Override
    public void onBtScannerResult(String barcode) {
        success = true;
        Log.debug("Got scanning result: [%s]", barcode);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(BARCODE_STRING, barcode);
        setResult(RESULT_OK, resultIntent);
        playSoundEffect(Sounds.SUCCESS);
        finish();
    }

    @InjectView(R.id.barcodeScannerStatus)
    TextView barcodeScannerStatus;

    @InjectView(R.id.barcodeScannerResults)
    TextView barcodeScannerResults;


    @Override
    public void onBtScannerSearching() {
        barcodeScannerStatus.setText("Scanner Connecting");
        barcodeScannerStatus.setTextColor(0xFFFFFF00);
        barcodeScannerResults.setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerConnected() {
        barcodeScannerStatus.setText("Scanner Connected");
        barcodeScannerStatus.setTextColor(0xFF00FF00);
        barcodeScannerResults.setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        //beaconDistanceMeasurer.startListening();
    }

    @Override
    public void onBtScannerDisconnected() {
        barcodeScannerStatus.setText("No Scanner");
        barcodeScannerStatus.setTextColor(0xFFFF0000);
        barcodeScannerResults.setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        //beaconDistanceMeasurer.stopListening();
    }
}
