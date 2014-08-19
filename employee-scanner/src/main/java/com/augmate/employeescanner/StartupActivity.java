package com.augmate.employeescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.augmate.employeescanner.scanner.IDScannerActivity;
import com.augmate.sdk.logger.Log;


public class StartupActivity extends Activity {

    public static final int REQUEST_BOX_SCAN = 0x01;

    private Handler handler;
    private IEmployeeBin employeeBin = MockEmployeeBin.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // turn screen on when application is deployed (makes testing easier)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        handler = new Handler();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BOX_SCAN && resultCode == RESULT_OK) {
            String value = data.getStringExtra("employeeString");
            if (value.startsWith("user_")) {
                scanSuccessful(value);
            } else {
                scanError();
            }
        } else {
            scanError();
        }
    }

    private void scanSuccessful(final String id) {
        Log.debug("Got employee id=%s", id);

        // update result view
        ((TextView) findViewById(R.id.instructionText)).setText(R.string.scan_success);
        Employee employee = employeeBin.getEmployee(id);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.EMPLOYEE_KEY, employee);
        startActivity(intent);
    }

    private void scanError() {
        Log.debug("Got no employee results");
        ((TextView) findViewById(R.id.instructionText)).setText(R.string.scan_error);
        ((TextView) findViewById(R.id.instructionText)).setTextColor(getResources().getColor(R.color.red));
        handler.removeCallbacks(launchScanRunnable);
        handler.postDelayed(launchScanRunnable, Constants.PROMPT_DURATION_MS * 2);
    }

    private Runnable launchScanRunnable = new Runnable() {
        @Override
        public void run() {
            ((TextView) findViewById(R.id.instructionText)).setText(R.string.tap_to_scan);
            ((TextView) findViewById(R.id.instructionText)).setTextColor(getResources().getColor(R.color.white));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        employeeBin = null;
        handler = null;
    }

    private void launchScanActivity() {
        Log.debug("Starting employee scanning activity..");
        Intent intent = new Intent(this, IDScannerActivity.class);
        startActivityForResult(intent, REQUEST_BOX_SCAN);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            launchScanActivity();
        }
        return super.onKeyDown(keyCode, event);
    }
}
