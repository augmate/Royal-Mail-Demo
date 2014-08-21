package com.augmate.sample.counter;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.augmate.sample.R;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.UserUtils;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;

public class CycleCountActivity extends BaseActivity {
    boolean allowOneTap = true;
    ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cyclecount);

        flipper = ((ViewFlipper) findViewById(R.id.flipper));
        flipper.setInAnimation(this, android.R.anim.slide_in_left);
        flipper.setOutAnimation(this, android.R.anim.slide_out_right);

        flipper.setFlipInterval(2000);
        flipper.setAutoStart(true);
        flipper.getInAnimation().setAnimationListener(getAnimationListener(flipper));

    }

    @Override
    public void processBarcodeScanning(String barcodeString, boolean wasExited, boolean wasSuccessful) {
        findViewById(R.id.scan_response).setVisibility(View.VISIBLE);
        findViewById(R.id.tap_to_scan).setVisibility(View.GONE);

        ImageView responseImage = (ImageView) findViewById(R.id.response_image);
        TextView responseText = (TextView) findViewById(R.id.response_text);
        if (wasSuccessful) {
            Log.debug("Got barcode value=%s", barcodeString);
            if (wasExited) {
                SoundHelper.dismiss(this);
                resetView();
            } else {
                SoundHelper.success(this);
                responseImage.setImageResource(android.R.drawable.ic_menu_add);
                responseText.setText(getString(R.string.bin_confirmed));
                recordCount(barcodeString);
            }
        } else {
            //generic error
            SoundHelper.error(this);
            responseImage.setImageResource(android.R.drawable.ic_menu_camera);
            responseText.setText(R.string.scan_error);
            rescan();
        }
    }

    private void resetView() {
        findViewById(R.id.scan_response).setVisibility(View.GONE);
        findViewById(R.id.tap_to_scan).setVisibility(View.VISIBLE);
        allowOneTap = true;
    }

    private void recordCount(String barcode) {
        Intent intent = new Intent(CycleCountActivity.this, RecordCountActivity.class);
        BinModel model = new BinModel();
        model.setBinBarcode(barcode);
        model.setUser(UserUtils.getUser());
        intent.putExtra(BinModel.TAG, model);
        startActivity(intent);
        resetView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && allowOneTap) {
            SoundHelper.tap(this);
            allowOneTap = false;
            startScanner();
        }
        return super.onKeyDown(keyCode, event);
    }
}
