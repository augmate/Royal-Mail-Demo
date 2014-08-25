package com.augmate.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.augmate.sample.common.SoundHelper;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * Created by premnirmal on 8/22/14.
 */
public class BrightnessActivity extends Activity {

    private int brightness;
    private GestureDetector mGestureDetector;
    private final GestureDetector.ScrollListener mBaseListener = new GestureDetector.ScrollListener() {

        @Override
        public boolean onScroll(float displacement, float delta, float velocity) {
            if (displacement < 0) {
                brightness -= 10;
            } else if (displacement > 0) {
                brightness += 10;
            }
            setBrightness();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this).setScrollListener(mBaseListener);
        setContentView(R.layout.slider);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(100);
        brightness = 80;
        setBrightness();
    }

    private void setBrightness() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.screenBrightness = (float) brightness / 100f;
        if (lp.screenBrightness < .01f) {
            lp.screenBrightness = .01f;
            brightness = 1;
        } else if (lp.screenBrightness > 1f) {
            lp.screenBrightness = 1f;
            brightness = 100;
        }
        w.setAttributes(lp);
        seekBar.setProgress(brightness);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            SoundHelper.tap(this);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
