package com.augmate.texturetest.app;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;
import com.google.android.glass.media.Sounds;
import com.augmate.sdk.scanner.IScannerResultListener;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TextureScannerActivity extends FragmentActivity  implements IScannerResultListener {
    public static final String BARCODE_STRING = "bcs";
    public static final int RESULT_TIMED_OUT = 10;
    public static final int RESULT_ERROR = 11;

    boolean success = false;
    private ViewFlipper viewFlipper;
    private TextView cameraScanResult;
    private TextView beaconScanResults;
    private ProgressBar beaconStrength1;

    private Timer beaconTimer = new Timer();
    private BeaconDistance beaconDistance = new BeaconDistance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_beacon);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        cameraScanResult = (TextView) findViewById(R.id.cameraScanResult);
        beaconScanResults = (TextView) findViewById(R.id.beaconScanResults);
        //beaconStrength1 = (ProgressBar) findViewById(R.id.beaconStrength1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconDistance.configureFromContext(this);
        beaconDistance.startListening();
        beaconTimer = new Timer();
        beaconTimer.scheduleAtFixedRate(new taskUpdateBeaconDist(), 0, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing..");

        beaconTimer.cancel();
        beaconDistance.stopListening();
    }

    private Handler displayMsgUpdater = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String displayMsg = msg.getData().getString("msg");
            beaconScanResults.setText(displayMsg);
        }
    };

    class taskUpdateBeaconDist extends TimerTask{
        @Override
        public void run() {

            List<BeaconInfo> beaconDistances = beaconDistance.getLatestBeaconDistances();
            String displayMsg = String.format("%d beacons found\n", beaconDistances.size());


            for (BeaconInfo beacon : beaconDistances) {

                displayMsg += String.format("#%d lastestPower=%.02f\n", beacon.minor,  beacon.lastestPower);

                switch (beacon.minor) {
                    case 1:
                       // beaconStrength1.setProgress();
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    default:
                        break;
                }
            }

            //displayMsg += String.format("Truck 1: samples=%d dist=%.1f\n", truck1.numOfBeacons, truck1.minDistance);
            //displayMsg += String.format("Truck 2: samples=%d dist=%.1f\n", truck2.numOfBeacons, truck2.minDistance);

            Message msg = displayMsgUpdater.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("msg", displayMsg);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            flipToScanner();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void flipToScanner() {
        success = false;
        viewFlipper.setDisplayedChild(1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!success) onError(true);
            }
        }, 10000);
    }

    @Override
    public void onBarcodeScanSuccess(String result) {
        success = true;
        playSoundEffect(Sounds.SUCCESS);
        viewFlipper.setDisplayedChild(0);
        cameraScanResult.setText(result);
    }

    @Override
    public void onBackPressed() {
        success = true;
        playSoundEffect(Sounds.DISMISSED);
        viewFlipper.setDisplayedChild(0);
        finish();
    }

    public void onError(boolean timedOut){
        playSoundEffect(Sounds.ERROR);
        viewFlipper.setDisplayedChild(0);
    }

    private AudioManager mAudioManager;
    protected void playSoundEffect(int soundId) {
        if (mAudioManager == null){
            mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        }
        mAudioManager.playSoundEffect(soundId);
    }
}
