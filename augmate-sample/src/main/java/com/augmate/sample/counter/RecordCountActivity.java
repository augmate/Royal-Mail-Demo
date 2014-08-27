package com.augmate.sample.counter;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
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
    boolean wasNetworkIssue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordcount);
        bin = (BinModel) getIntent().getSerializableExtra(MessageActivity.DATA);
        enterTextState();
    }

    private void enterTextState() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(R.drawable.mic);
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
        if (wasNetworkIssue) {
            finish();
        } else {
            startRecording();
        }
    }

    public void stopRecording(boolean isError, int errorCode) {
        if (isError) {
            SoundHelper.error(this);
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.SOUND_ERROR);
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    wasNetworkIssue = true;
                    showError(ErrorPrompt.NETWORK_ERROR);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.SOUND_ERROR);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    wasNetworkIssue = true;
                    showError(ErrorPrompt.NETWORK_ERROR);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.TIMEOUT_ERROR);
                    break;
                default:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.SOUND_ERROR);
                    break;
            }
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
