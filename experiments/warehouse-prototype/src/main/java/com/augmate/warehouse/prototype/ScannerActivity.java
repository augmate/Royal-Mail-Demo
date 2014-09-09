package com.augmate.warehouse.prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.augmate.sdk.scanner.ScannerFragmentBase;
import com.google.android.glass.media.Sounds;

/**
 * @author James Davis (Fuzz)
 */
public class ScannerActivity extends FragmentActivity implements ScannerFragmentBase.OnScannerResultListener {
    public static final String BARCODE_STRING = "bcs";

    public static final int RESULT_TIMED_OUT = 10;
    public static final int RESULT_ERROR = 11;

    boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

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
    public void onBarcodeScanSuccess(String result) {
        success = true;
        Intent resultIntent = new Intent();
        resultIntent.putExtra(BARCODE_STRING, result);
        setResult(RESULT_OK, resultIntent);
        playSoundEffect(Sounds.SUCCESS);
        finish();
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
}
