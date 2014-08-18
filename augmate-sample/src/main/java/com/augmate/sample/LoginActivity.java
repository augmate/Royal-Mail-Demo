package com.augmate.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.sample.common.FlowUtils;
import com.augmate.sample.scanner.ScannerActivity;
import com.augmate.sdk.logger.Log;

public class LoginActivity extends BaseActivity {
    public static final int REQUEST_BARCODE_SCAN = 0x01;
    boolean allowOneTap = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        findViewById(R.id.login_response).setVisibility(View.VISIBLE);
        findViewById(R.id.login_help).setVisibility(View.GONE);

        ImageView responseImage = (ImageView) findViewById(R.id.response_image);
        TextView responseText = (TextView) findViewById(R.id.response_text);

        if (requestCode == REQUEST_BARCODE_SCAN && resultCode == RESULT_OK) {
            String value = data.getStringExtra("barcodeString");
            boolean exited = data.getBooleanExtra("exited",false);
            Log.debug("Got barcode value=%s", value);
            if (exited) {
                findViewById(R.id.login_response).setVisibility(View.GONE);
                findViewById(R.id.login_help).setVisibility(View.VISIBLE);
                allowOneTap = true;
            } else if (value.startsWith("user_")) {
                String employeeName = value.replace("user_","");
                responseImage.setImageResource(android.R.drawable.ic_menu_add);
                responseText.setText(getString(R.string.welcome,employeeName));
            } else {
                //invalid code
                responseImage.setImageResource(android.R.drawable.ic_menu_camera);
                responseText.setText(R.string.invalid_scan);
                rescan();
            }
        } else {
            //generic error
            responseImage.setImageResource(android.R.drawable.ic_menu_camera);
            responseText.setText(R.string.scan_error);
            rescan();
        }
    }

    private void rescan() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startScanner();
            }
        }, FlowUtils.TRANSITION_TIMEOUT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && allowOneTap) {
           allowOneTap = false;
           startScanner();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startScanner() {
        Log.debug("Starting scanner activity..");
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, REQUEST_BARCODE_SCAN);
    }
}
