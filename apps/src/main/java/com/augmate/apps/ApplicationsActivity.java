package com.augmate.apps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.UserUtils;
import com.augmate.apps.common.activities.BaseActivity;
import com.augmate.apps.counter.CycleCountActivity;
import com.augmate.apps.nonretailtouching.NonRetailTouchActivity;
import com.augmate.apps.truckloading.TruckLoadingActivity;
import com.google.android.glass.view.WindowUtils;

public class ApplicationsActivity extends BaseActivity {

    private boolean isAttached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_applications);
        TextView textView = (TextView) findViewById(R.id.welcomeText);
        textView.setText(getString(R.string.welcome, UserUtils.getUser()));

        FontHelper.updateFontForBrightness(
                (TextView) findViewById(R.id.tap_for_options));
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.apps, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAttached) {
            openOptionsMenu();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.isAttached = true;
        openOptionsMenu();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            return onOptionsItemSelected(item);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.truck_loading:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        goToTruckLoading();
                    }
                });
                return true;
            case R.id.cycle_count:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        goToCycleCounter();
                    }
                });
                return true;
            case R.id.non_retail_touch:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        goToNonRetailTouching();
                    }
                });
                return true;
            case R.id.adjust_brightness:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        adjustBrightness();
                    }
                });
                return true;
            case R.id.toggle_animations:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        toggleAnimation();
                    }
                });
            default:
                return false;
        }
    }

    private void adjustBrightness() {
        Intent intent = new Intent(this, BrightnessActivity.class);
        startActivity(intent);
    }

    private void toggleAnimation() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_prefs),MODE_PRIVATE);
        boolean oldValue = prefs.getBoolean(getString(R.string.pref_animation_toggle),true);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.pref_animation_toggle),!oldValue);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            SoundHelper.tap(this);
            openOptionsMenu();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToCycleCounter() {
        Intent intent = new Intent(this, CycleCountActivity.class);
        startActivity(intent);
    }

    private void goToTruckLoading() {
        Intent intent = new Intent(this, TruckLoadingActivity.class);
        startActivity(intent);
    }

    private void goToNonRetailTouching() {
        startActivity(new Intent(this, NonRetailTouchActivity.class));
    }
}
