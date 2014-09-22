package com.augmate.warehouse.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author James Davis (Fuzz)
 */
public class BinScanActivity extends BaseActivity implements GestureDetector.BaseListener {
    ViewFlipper flipper;
    private static int BIN_SCANNER_REQUEST = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_scan);
        getGestureDetector().setBaseListener(this);

        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        flipper.setInAnimation(this, android.R.anim.slide_in_left);
        flipper.setOutAnimation(this, android.R.anim.slide_out_right);

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (flipper != null){
                    flipper.showNext();
                }
            }
        }, 2000);
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        boolean handled = false;

        if (gesture == Gesture.TAP){
            Intent scannerIntent = new Intent(this, HandheldScannerActivity.class);
            startActivityForResult(scannerIntent, BIN_SCANNER_REQUEST);
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

        if (requestCode == BIN_SCANNER_REQUEST){
            if (resultCode == RESULT_OK){
                String binId = data.getStringExtra(HandheldScannerActivity.BARCODE_STRING);
                if (!binId.contains("bin_")){
                    intent.putExtra(MessageActivity.ERROR, true);
                    intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_bin));
                    startActivityForResult(intent, 999);
                } else {
                    intent.putExtra(MessageActivity.CLASS, "com.augmate.warehouse.prototype.BinCountActivity");
                    intent.putExtra(MessageActivity.MESSAGE, getString(R.string.message_bin_confirmed));
                    intent.putExtra(MessageActivity.DATA, binId);
                    startActivity(intent);
                    finish();
                }
            } else if (resultCode == RESULT_CANCELED){

            } else if (resultCode == HandheldScannerActivity.RESULT_ERROR){
                intent.putExtra(MessageActivity.ERROR, true);
                intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_scan));
                startActivityForResult(intent, 999);
            } else if (resultCode == HandheldScannerActivity.RESULT_TIMED_OUT){
                intent.putExtra(MessageActivity.ERROR, true);
                intent.putExtra(MessageActivity.MESSAGE, getString(R.string.error_timed_out));
                startActivityForResult(intent, 999);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
