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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.common.ErrorPrompt;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.UserUtils;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.bluetooth.IBluetoothScannerEvents;
import com.augmate.sdk.scanner.bluetooth.IncomingConnector;
import com.google.android.glass.media.Sounds;

public class CycleCountActivity extends BaseActivity implements IBluetoothScannerEvents {

    private IncomingConnector bluetoothScannerConnector = new IncomingConnector(this, this);
    public static final String BARCODE_STRING = "bcs";
    public boolean btListening = true;

    ConnectivityManager cm;
    private ProgressBar loading_icon;
    private Animation rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structured_cyclecount);

        FontHelper.updateFontForBrightness((TextView) findViewById(R.id.barcodeScannerStatus));
        loading_icon = (ProgressBar) findViewById(R.id.loading_icon);
        rotation = AnimationUtils.loadAnimation(this, R.anim.spin);

        cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

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
        return super.onGenericMotionEvent(event);
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
                showConfirmation(getString(R.string.bin_confirmed,barcodeString), RecordCountActivity.class, model);
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
            } else {
                SoundHelper.error(this);
                showError(ErrorPrompt.NETWORK_ERROR);
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
    public void onBtScannerSearching() {
        startLoader(true);
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Searching for scanner...");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFFFFFF00);
        //((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
    }

    @Override
    public void onBtScannerConnected() {
        startLoader(false);
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("Scanner Connected");
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setTextColor(0xFF00FF00);
        //((TextView) findViewById(R.id.barcodeScannerResults)).setText("at " + DateTime.now().toString(DateTimeFormat.mediumDateTime()));
        //beaconDistanceMeasurer.startListening();
    }

    @Override
    public void onBtScannerDisconnected() {
        startLoader(false);
        ((TextView) findViewById(R.id.barcodeScannerStatus)).setText("No Scanner Detected");
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
            //bluetoothScannerConnector.reconnect();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private void startLoader(boolean start){
        if(start){
            loading_icon.setVisibility(ImageView.VISIBLE);
            //loading_icon.startAnimation(rotation);
        }
        else{
            //loading_icon.clearAnimation();
            //rotation.cancel();
            loading_icon.setVisibility(ImageView.INVISIBLE);
        }

    }

}
