package com.augmate.apps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import com.augmate.apps.common.SoundHelper;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Created by premnirmal on 8/22/14.
 */
public class BrightnessActivity extends RoboActivity {

    private int brightness;
    private GestureDetector mGestureDetector;

    @Inject
    private SharedPreferences prefs;

    @InjectView(R.id.seekbar)
    SeekBar seekBar;

    private final class Listener implements GestureDetector.ScrollListener {

        @Override
        public boolean onScroll(float displacement, float delta, float velocity) {
            if (displacement < 0) {
                brightness -= 3;
            } else if (displacement > 0) {
                brightness += 3;
            }
            setBrightness();
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this).setScrollListener(new Listener());
        setContentView(R.layout.slider);

        seekBar.setMax(100);
        float set_brightness = prefs.getFloat("BRIGHTNESS",0.5f);
         //getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE).getFloat("BRIGHTNESS", 0.5f);


        brightness = (int) (set_brightness * 100f);
        setBrightness();
    }

    private void setBrightness() {
        TextView brightnessText = (TextView) findViewById(R.id.brightness_value);
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.screenBrightness = (float) brightness / 100f;
        if (lp.screenBrightness <= .01f) {
            brightness = 0;
        } else if (lp.screenBrightness > 1f) {
            brightness = 100;
        }
        lp.screenBrightness = brightness / 100f;
        prefs.edit().putFloat("BRIGHTNESS", lp.screenBrightness).commit();
        //(getApplication().getPackageName(), MODE_PRIVATE).edit().putFloat("BRIGHTNESS", lp.screenBrightness).apply();

        w.setAttributes(lp);
        seekBar.setProgress(brightness);
        String brightnessString = (brightness * 230 / 100 + 25) + ""; // make the scale from 25 to 255
        brightnessText.setText(brightnessString);
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
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
