package com.augmate.sample.counter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.sample.R;
import com.augmate.sample.common.FlowUtils;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.voice.VoiceActivity;

/**
 * Created by cesaraguilar on 8/19/14.
 */
public class RecordCountActivity extends VoiceActivity {

    private enum RecordState {
        LISTENING, CONFIRM
    };
    private RecordState currentState = RecordState.LISTENING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordcount);
        enterTextState();
    }

    private void enterTextState() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(android.R.drawable.ic_btn_speak_now);
        TextView textView = (TextView) findViewById(R.id.big_image_text);
        textView.setText("\"#\"?");
        findViewById(R.id.big_image_state).setVisibility(View.VISIBLE);
        findViewById(R.id.big_text_state).setVisibility(View.GONE);
    }

    private void confirmTextState(String text) {
        TextView bigText = (TextView) findViewById(R.id.big_text);
        bigText.setText(getString(R.string.confirme,text));
        findViewById(R.id.big_image_state).setVisibility(View.GONE);
        findViewById(R.id.big_text_state).setVisibility(View.VISIBLE);
    }

    private void errorState() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        TextView textView = (TextView) findViewById(R.id.big_image_text);
        textView.setText(R.string.lets_try_again);
        findViewById(R.id.big_image_state).setVisibility(View.VISIBLE);
        findViewById(R.id.big_text_state).setVisibility(View.GONE);
    }

    private void confirmState() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(android.R.drawable.ic_menu_camera);
        TextView textView = (TextView) findViewById(R.id.big_image_text);
        textView.setText(R.string.count_confirmed);
        findViewById(R.id.big_image_state).setVisibility(View.VISIBLE);
        findViewById(R.id.big_text_state).setVisibility(View.GONE);
    }

    private void startRecordingAnimation() {
        //TODO create animation
    }

    public void isRecording() {
        if (currentState == RecordState.LISTENING) {
            enterTextState();
            startRecordingAnimation();
        }
    }

    public void stopRecording(boolean isError) {
        if (isError) {
            SoundHelper.error(this);
            errorState();
            recordWithDelay();
        }
    }

    public void onResult(String resultString) {
        if (currentState == RecordState.LISTENING) {
            SoundHelper.success(this);
            confirmTextState(resultString);
            currentState = RecordState.CONFIRM;
            startRecording();
        } else if (currentState == RecordState.CONFIRM) {
            if (resultString.equalsIgnoreCase("YES")) {
                SoundHelper.success(this);
                confirmState();
                finishWithDelay();
            } else {
                enterTextState();
                currentState = RecordState.LISTENING;
                startRecording();
            }
        }
    }

    private void recordWithDelay() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterTextState();
                currentState = RecordState.LISTENING;
                startRecording();
            }
        }, FlowUtils.TRANSITION_TIMEOUT);
    }

    private void finishWithDelay() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, FlowUtils.TRANSITION_TIMEOUT);
    }

}
