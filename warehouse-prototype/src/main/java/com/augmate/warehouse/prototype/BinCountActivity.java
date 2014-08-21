package com.augmate.warehouse.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.List;

/**
 * @author James Davis (Fuzz)
 */
public class BinCountActivity extends BaseActivity implements GestureDetector.BaseListener {
    String mBinId = "";
    int mBinNumber = 0;
    private static final int SPEECH_REQUEST = 15;
    private static final int CONFIRM_REQUEST = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_count);
        getGestureDetector().setBaseListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mBinId = extras.getString(MessageActivity.DATA, "");
        }

        TextView numTextView = (TextView) findViewById(R.id.numberTextView);
        if (numTextView != null){
            numTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        boolean handled = false;

        if (gesture == Gesture.TAP){
            TextView numTextView = (TextView) findViewById(R.id.numberTextView);
            if (numTextView != null){
                if (numTextView.getVisibility() == View.VISIBLE){
                    Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Yes or No?");
                    startActivityForResult(speechIntent, CONFIRM_REQUEST);
                    playSoundEffect(Sounds.TAP);
                    handled = true;
                } else {
                    Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.prompt_number_of_items_2));
                    startActivityForResult(speechIntent, SPEECH_REQUEST);
                    playSoundEffect(Sounds.TAP);
                    handled = true;
                }
            }
        } else if (gesture == Gesture.SWIPE_DOWN){
            playSoundEffect(Sounds.DISMISSED);
            handled = true;
            finish();
        }

        return handled;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        resetLayout();

        if (requestCode == SPEECH_REQUEST){
            if (resultCode == RESULT_OK){
                try {
                    List<String> results = data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    String spokenText = results.get(0);

                    mBinNumber = Integer.parseInt(spokenText);
                    setNumberLayout(mBinNumber);
                } catch (NumberFormatException nfe){
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra(MessageActivity.ERROR, true);
                    intent.putExtra(MessageActivity.MESSAGE, "Let's");
                    startActivityForResult(intent, 999);
                }
            }
        } else if (requestCode == CONFIRM_REQUEST){
            if (resultCode == RESULT_OK){
                List<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                String spokenText = results.get(0);

                if (spokenText.equalsIgnoreCase("yes")) {
                    Data.addBin(mBinId, mBinNumber);
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra(MessageActivity.ERROR, false);
                    intent.putExtra(MessageActivity.MESSAGE, getString(R.string.message_count_confirmed));
                    intent.putExtra(MessageActivity.CLASS, "com.augmate.warehouse.prototype.BinScanActivity");
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra(MessageActivity.ERROR, true);
                    intent.putExtra(MessageActivity.MESSAGE, "Let's");
                    startActivityForResult(intent, 999);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return getGestureDetector().onMotionEvent(event);
    }

    private void setNumberLayout(int number){
        TextView numTextView = (TextView) findViewById(R.id.numberTextView);
        TextView promptTextView = (TextView) findViewById(R.id.itemPrompt);
        if (numTextView != null){
            numTextView.setVisibility(View.VISIBLE);
            numTextView.setText(number + "?");
        }
        if (promptTextView != null){
            promptTextView.setText("Tap to confirm");
        }
    }

    private void resetLayout(){
        TextView numTextView = (TextView) findViewById(R.id.numberTextView);
        TextView promptTextView = (TextView) findViewById(R.id.itemPrompt);
        if (numTextView != null){
            numTextView.setVisibility(View.GONE);
            numTextView.setText("");
        }
        if (promptTextView != null){
            promptTextView.setText(R.string.prompt_number_of_items);
        }
    }
}
