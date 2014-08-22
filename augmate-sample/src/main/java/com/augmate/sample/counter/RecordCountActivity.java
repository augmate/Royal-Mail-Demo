package com.augmate.sample.counter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.sample.R;
import com.augmate.sample.common.ErrorPrompt;
import com.augmate.sample.common.SoundHelper;
import com.augmate.sample.common.activities.MessageActivity;
import com.augmate.sample.voice.VoiceActivity;

/**
 * Created by cesaraguilar on 8/19/14.
 */
public class RecordCountActivity extends VoiceActivity {

    private enum RecordState {
        LISTENING, CONFIRM
    };
    private RecordState currentState = RecordState.LISTENING;
    BinModel bin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordcount);
        bin = (BinModel) getIntent().getSerializableExtra(MessageActivity.DATA);
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
        bin.setCount(text);
        TextView bigText = (TextView) findViewById(R.id.big_text);
        bigText.setText(getString(R.string.confirme,text));
        findViewById(R.id.big_image_state).setVisibility(View.GONE);
        findViewById(R.id.big_text_state).setVisibility(View.VISIBLE);
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

    @Override
    public void handlePromptReturn() {
        startRecording();
    }

    public void stopRecording(boolean isError) {
        if (isError) {
            SoundHelper.error(this);
            showError(ErrorPrompt.TRY_AGAIN);
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
                BinManager.getSharedInstance().saveBin(bin);
                SoundHelper.success(this);
                showConfirmation(getString(R.string.bin_confirmed),null,null);
                finish();
            } else {
                enterTextState();
                currentState = RecordState.LISTENING;
                startRecording();
            }
        }
    }

}
