package com.augmate.warehouse.prototype;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author James Davis (Fuzz)
 */
public abstract class BaseActivity extends Activity {
    Handler mHandler = new Handler();
    GestureDetector mDetector;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetector = new GestureDetector(this);
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
    }

    public Handler getHandler(){
        return mHandler;
    }

    public GestureDetector getGestureDetector(){
        return mDetector;
    }

    protected void playSoundEffect(int soundId) {
        mAudioManager.playSoundEffect(soundId);
    }
}
