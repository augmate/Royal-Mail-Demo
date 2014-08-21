package com.augmate.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sample.counter.CycleCountActivity;
import com.google.android.glass.view.WindowUtils;

public class ApplicationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            return onOptionsItemSelected(item);
        }
        return super.onMenuItemSelected(featureId,item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.location_history:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        openLocationHistory();
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
            default:
                return false;
        }
    }

    private void openLocationHistory() {
        openOptionsMenu();
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
        finish();
    }

}
