package com.augmate.sample.counter;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ViewFlipper;

import com.augmate.sample.R;
import com.augmate.sample.common.ErrorPrompt;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.UserUtils;
import com.augmate.sample.common.activities.BaseActivity;
import com.augmate.sdk.logger.Log;

public class CycleCountActivity extends BaseActivity {
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
        if (wasSuccessful) {
            Log.debug("Got barcode value=%s", barcodeString);
            if (wasExited) {
                SoundHelper.dismiss(this);
            } else {
                SoundHelper.success(this);
                BinModel model = new BinModel();
                model.setBinBarcode(barcodeString);
                model.setUser(UserUtils.getUser());
                showConfirmation(getString(R.string.bin_confirmed), RecordCountActivity.class, model);
            }
        } else {
            //generic error
            SoundHelper.error(this);
            showError(ErrorPrompt.SCAN_ERROR);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            SoundHelper.tap(this);
            startScanner();
        }
        return super.onKeyDown(keyCode, event);
    }
}
