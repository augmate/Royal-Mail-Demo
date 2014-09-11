package com.augmate.apps.counter;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.apps.R;
import com.augmate.apps.common.ErrorPrompt;
import com.augmate.apps.common.FontHelper;
import com.augmate.apps.common.SoundHelper;
import com.augmate.apps.common.activities.MessageActivity;
import com.augmate.apps.voice.VoiceActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cesaraguilar on 8/19/14.
 */
public class RecordCountActivity extends VoiceActivity {

    private enum RecordState {
        LISTENING, CONFIRM
    };
    private RecordState currentState = RecordState.LISTENING;
    BinModel bin;
    ViewPropertyAnimator mAnimator;
    final Object mLock = new Object();
    boolean wasNetworkIssue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordcount);
        bin = (BinModel) getIntent().getSerializableExtra(MessageActivity.DATA);
        enterTextState();
        setRecordingAnimation(true);

        FontHelper.updateFontForBrightness(
                (TextView)findViewById(R.id.big_image_text)
                ,(TextView)findViewById(R.id.big_text_text));
    }

    private void enterTextState() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(R.drawable.mic);
        bigImage.setRotation(0);
        TextView textView = (TextView) findViewById(R.id.big_image_text);
        textView.setText(R.string.report_count);
        findViewById(R.id.big_image_state).setVisibility(View.VISIBLE);
        findViewById(R.id.big_text_state).setVisibility(View.GONE);
    }

    private void confirmTextState(String text) {
        bin.setCount(text);
        TextView bigText = (TextView) findViewById(R.id.big_text);
        bigText.setText(getString(R.string.confirme, text));
        ImageView processing = (ImageView) findViewById(R.id.processing);
        bigText.setVisibility(View.VISIBLE);
        processing.setVisibility(View.GONE);
        findViewById(R.id.big_image_state).setVisibility(View.GONE);
        findViewById(R.id.big_text_state).setVisibility(View.VISIBLE);
    }

    private void startRecordingAnimation() {
        ImageView bigImage = (ImageView) findViewById(R.id.big_image);
        bigImage.setImageResource(R.drawable.processing2);
        TextView textView = (TextView) findViewById(R.id.big_image_text);
        textView.setText(R.string.report_count);
        findViewById(R.id.big_image_state).setVisibility(View.VISIBLE);
        findViewById(R.id.big_text_state).setVisibility(View.GONE);
        animate(bigImage);
    }

    private void startRecordingAnimation2() {
        TextView bigText = (TextView) findViewById(R.id.big_text);
        ImageView processing = (ImageView) findViewById(R.id.processing);
        bigText.setVisibility(View.GONE);
        processing.setVisibility(View.VISIBLE);
        findViewById(R.id.big_image_state).setVisibility(View.GONE);
        findViewById(R.id.big_text_state).setVisibility(View.VISIBLE);
        animate(processing);
    }

    private void animate(final View inView) {
        mAnimator = inView.animate()
                .rotation(360)
                .setDuration(500)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mLock) {
                            if (mAnimator != null) {
                                animate(inView);
                            }
                        }
                    }
                });
        mAnimator.start();
    }

    public void isRecording() {
        if (currentState == RecordState.LISTENING) {
            enterTextState();
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
            if (currentState == RecordState.LISTENING) {
                enterTextState();
            } else {
                confirmTextState(bin.getCount());
            }
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
                case 11111:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.NUMBER_ERROR);
                    break;
                default:
                    wasNetworkIssue = false;
                    showError(ErrorPrompt.SOUND_ERROR);
                    break;
            }
        } else {
            if (currentState == RecordState.LISTENING) {
                startRecordingAnimation();
            } else {
                startRecordingAnimation2();
            }
        }
    }

    public void onResult(String resultString) {
        if (currentState == RecordState.LISTENING) {
            try {
                Pattern pattern = Pattern.compile("[0-9]+");
                Matcher matcher = pattern.matcher(resultString);
                matcher.find();
                resultString = matcher.group();
                Integer integer = Integer.valueOf(resultString);
                synchronized (mLock) {
                    if (mAnimator != null) {
                        mAnimator.cancel();
                        mAnimator = null;
                    }
                }
                SoundHelper.success(this);
                confirmTextState(resultString);
                currentState = RecordState.CONFIRM;
                startRecording();
            } catch (Throwable ignored) {
                stopRecording(true, 11111);
            }
        } else if (currentState == RecordState.CONFIRM) {
            if (SoundHelper.isAffirmative(resultString)) {
                BinManager.getSharedInstance().saveBin(bin);
                SoundHelper.success(this);
                showConfirmation(getString(R.string.count_confirmed),null,null);
                finish();
            } else {
                enterTextState();
                currentState = RecordState.LISTENING;
                startRecording();
            }
        }
    }

    public void setRecordingAnimation(boolean recording){
        View microphone = findViewById(R.id.big_image);
        if (microphone != null) {
            if (recording) {
                AlphaAnimation animation = new AlphaAnimation(1f, 0f);
                animation.setDuration(750);
                animation.setRepeatCount(Animation.INFINITE);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());

                microphone.startAnimation(animation);
            } else {
                microphone.clearAnimation();
            }
        }
    }
}
