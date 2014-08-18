package com.augmate.employeescanner;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * Created by premnirmal on 8/18/14.
 */
public class MainActivity extends Activity {

    private Employee employee;
    private Handler handler;

    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;

    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                mAudioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);

        employee = getIntent().getParcelableExtra(Constants.EMPLOYEE_KEY);
        setContentView(R.layout.activity_main_welcome);
        ((TextView) findViewById(R.id.welcome_text)).setText(getString(R.string.welcome_employee, employee.getName()));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openOptionsMenu();
            }
        }, Constants.PROMPT_DURATION_MS);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        openLocationHistory();
                    }
                });
                return true;
            case R.id.cycle_count:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        openCycleCount();
                    }
                });
                return true;
            default:
                return false;
        }
    }

    private void openLocationHistory() {
        
    }

    private void openCycleCount() {

    }


}
