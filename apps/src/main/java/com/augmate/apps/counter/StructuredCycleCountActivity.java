package com.augmate.apps.counter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.augmate.apps.R;
import com.augmate.apps.common.ErrorPrompt;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.UserUtils;
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
    public boolean btListening = true;

    ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structured_cyclecount);


        FontHelper.updateFontForBrightness((TextView) findViewById(R.id.barcodeScannerStatus));

        cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

//        TouchResponseListener responseListener = new TouchResponseListener(findViewById(R.id.touch));
//        mGestureDetector = new GestureDetector(this)
//                .setBaseListener(responseListener)
//                .setScrollListener(responseListener)
//                .setFingerListener(responseListener);
//        flipper = ((ViewFlipper) findViewById(R.id.flipper));

        //SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_prefs),MODE_PRIVATE);
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



//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                processBarcodeScanning("bin_3", false,true,false);
//            }
//        }, 10000);

        btListening = true;
        bluetoothScannerConnector.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");
        bluetoothScannerConnector.stop();
    }

    @Override
    protected void onResume() {
        btListening = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        btListening = false;
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("LoadAnimation", false);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //mGestureDetector.onMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }

    private boolean handlePromptReturn = true;
    @Override
    public void handlePromptReturn() {
        if (handlePromptReturn) {
            //rescan();
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
                //startScanner();
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
        if (!btListening) {
            return;
        }

        btListening = false;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.debug("Got scanning result: [%s]", barcode);

        // TODO: do we need this?
        setResult(RESULT_OK, new Intent().putExtra(BARCODE_STRING, barcode));

        playSoundEffect(Sounds.SUCCESS);

        // always pass the barcode
        // eventually.. start "get count" voice activity
        processBarcodeScanning(barcode, false, true, false);
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            Log.debug("User requested scanner reconnect.");
            bluetoothScannerConnector.reconnect();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

}
