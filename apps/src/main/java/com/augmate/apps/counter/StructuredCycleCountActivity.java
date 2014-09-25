package com.augmate.apps.counter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.augmate.apps.R;
import com.augmate.apps.common.*;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.OutgoingConnector;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.GestureDetector;

public class StructuredCycleCountActivity extends BaseActivity implements IBluetoothScannerEvents {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;
    private OutgoingConnector bluetoothScannerConnector = new OutgoingConnector(this);
    public static final String BARCODE_STRING = "bcs";
    public static final int RESULT_TIMED_OUT = 10;
    public static final int RESULT_ERROR = 11;


    ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structured_cyclecount);


        android.util.Log.d("WTF", "onCreate has started");

        FontHelper.updateFontForBrightness((TextView) findViewById(R.id.barcodeScannerStatus));

        cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

        TouchResponseListener responseListener = new TouchResponseListener(findViewById(R.id.touch));
        mGestureDetector = new GestureDetector(this)
                .setBaseListener(responseListener)
                .setScrollListener(responseListener)
                .setFingerListener(responseListener);
        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_prefs),MODE_PRIVATE);
        //boolean animationsOn = prefs.getBoolean(getString(R.string.pref_animation_toggle),true);
//        boolean animationsOn = true;
//        flipper.setFlipInterval(FlowUtils.VIEWFLIPPER_TRANSITION_TIMEOUT_LONG);
//        if (animationsOn) {
//            flipper.setInAnimation(this, android.R.anim.slide_in_left);
//            flipper.setOutAnimation(this, android.R.anim.slide_out_right);
//            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
//        } else {
//            flipper.setInAnimation(this, R.anim.no_slide_anim);
//            flipper.setOutAnimation(this, R.anim.no_slide_anim);
//            flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));
//        }
//        if (savedInstanceState == null || savedInstanceState.getBoolean("LoadAnimation",true)) {
//            flipper.setAutoStart(true);
//        } else {
//            flipper.setDisplayedChild(2);
//        }



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                processBarcodeScanning("bin_3", false,true,false);
            }
        }, 10000);

        bluetoothScannerConnector.start();
        bluetoothScannerConnector.reconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");
        bluetoothScannerConnector.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("LoadAnimation", false);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }

    private boolean handlePromptReturn = true;
    @Override
    public void handlePromptReturn() {
        if (handlePromptReturn) {
            rescan();
        }
        handlePromptReturn = true;
    }

    @Override
    public void processBarcodeScanning(String barcodeString, boolean wasExited,
                                       boolean wasSuccessful, boolean wasTimedOut) {
        if (wasSuccessful) {
            Log.debug("Got barcode value=%s", barcodeString);
            if (wasExited) {
                SoundHelper.dismiss(this);
            } else {
                SoundHelper.success(this);
                BinModel model = new BinModel();
                model.setBinBarcode (barcodeString);
                model.setUser(UserUtils.getUser());
                showConfirmation(getString(R.string.bin_confirmed), RecordCountActivity.class, model);
            }
        } else {
            //generic error
            SoundHelper.error(this);
            if (wasTimedOut) {
                showError(ErrorPrompt.TIMEOUT_ERROR);
            } else {
                showError(ErrorPrompt.SCAN_ERROR);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (networkInfo != null && networkInfo.isConnected()) {
                SoundHelper.tap(this);
                startScanner();
            } else {
                SoundHelper.error(this);
                showError(ErrorPrompt.NETWORK_ERROR);
                handlePromptReturn = false;
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onBtScannerResult(String barcode) {
        Log.debug("Got scanning result: [%s]", barcode);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(BARCODE_STRING, barcode);
        setResult(RESULT_OK, resultIntent);
        playSoundEffect(Sounds.SUCCESS);
        //finish();
        processBarcodeScanning(barcode, false,true,false);
    }

    private AudioManager mAudioManager;
    protected void playSoundEffect(int soundId) {
        if (mAudioManager == null){
            mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        }
        mAudioManager.playSoundEffect(soundId);
    }

    @Override
    public void onBtScannerConnecting() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connecting");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFFFF00);
        //((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerConnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
        //((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        //beaconDistanceMeasurer.startListening();
    }

    @Override
    public void onBtScannerDisconnected() {
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFF0000);
        //((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        //beaconDistanceMeasurer.stopListening();
    }
}
