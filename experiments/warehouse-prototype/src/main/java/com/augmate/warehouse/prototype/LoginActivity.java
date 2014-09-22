package com.augmate.warehouse.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author James Davis (Fuzz)
 */
public class LoginActivity extends BaseActivity implements GestureDetector.BaseListener {
    private static int LOGIN_SCANNER_REQUEST = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt);
        getGestureDetector().setBaseListener(this);
    }


    @Override
    public boolean onGesture(Gesture gesture) {
        boolean handled = false;

        if (gesture == Gesture.TAP){
            Intent scannerIntent = new Intent(this, ScannerActivity.class);
            startActivityForResult(scannerIntent, LOGIN_SCANNER_REQUEST);
            playSoundEffect(Sounds.TAP);
            handled = true;
        } else if (gesture == Gesture.SWIPE_DOWN){
            playSoundEffect(Sounds.DISMISSED);
            handled = true;
            finish();
        }

        return handled;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return getGestureDetector().onMotionEvent(event);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this, MessageActivity.class);

        if (requestCode == LOGIN_SCANNER_REQUEST){
            if (resultCode == RESULT_OK){
                String user = data.getStringExtra(ScannerActivity.BARCODE_STRING);
                if (!user.contains("User_")){
                    intent.putExtra(MessageActivity.ERROR, true);
                    intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_scan));
                    startActivityForResult(intent, 999);
                } else {
                    Data.setUser(user);
                    user = user.replace("user_", "");
                    if (user.length() > 1) {
                        user = user.substring(0, 1).toUpperCase() + user.substring(1);
                    }
                    intent.putExtra(MessageActivity.CLASS, "com.augmate.warehouse.prototype.BinScanActivity");
                    intent.putExtra(MessageActivity.MESSAGE, getString(R.string.message_welcome, user));
                    startActivity(intent);
                    finish();
                }
            } else if (resultCode == RESULT_CANCELED){

            } else if (resultCode == ScannerActivity.RESULT_ERROR){
                intent.putExtra(MessageActivity.ERROR, true);
                intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_scan));
                startActivityForResult(intent, 999);
            } else if (resultCode == ScannerActivity.RESULT_TIMED_OUT){
                intent.putExtra(MessageActivity.ERROR, true);
                intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_timed_out));
                startActivityForResult(intent, 999);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
