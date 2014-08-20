package com.augmate.sample.voice;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import com.augmate.sample.common.activities.BaseActivity;
import java.util.ArrayList;

/**
 * Created by cesaraguilar on 8/20/14.
 */
public class VoiceActivity extends BaseActivity implements RecognitionListener {

    SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startRecording();
    }

    public void startRecording() {
        speechRecognizer.setRecognitionListener(this);
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName())
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        isRecording();
    }

    @Override
    public void onBeginningOfSpeech() {
        isRecording();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        isRecording();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        isRecording();
    }

    @Override
    public void onEndOfSpeech() {
        stopRecording(false);
    }

    @Override
    public void onError(int error) {
        stopRecording(true);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> stringArrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (stringArrayList != null && stringArrayList.size() > 0) {
            onResult(stringArrayList.get(0));
            stopRecording(false);
        } else {
            stopRecording(true);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        isRecording();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public void isRecording() {

    }

    public void stopRecording(boolean isError) {

    }

    public void onResult(String resultString) {

    }
}
