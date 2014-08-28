package com.augmate.sample.voice;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.WindowManager;

import com.augmate.sample.common.activities.BaseActivity;
import java.util.ArrayList;

/**
 * Created by cesaraguilar on 8/20/14.
 */
public class VoiceActivity extends BaseActivity implements RecognitionListener {

    boolean callIsRecordingOnce = true;
    SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        startRecording();
    }

    public void startRecording() {
        callIsRecordingOnce = true;
        speechRecognizer.setRecognitionListener(this);
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName())
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        _isRecording();
    }

    @Override
    public void onBeginningOfSpeech() {
        _isRecording();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        _isRecording();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        _isRecording();
    }

    @Override
    public void onEndOfSpeech() {
        stopRecording(false,0);
    }

    @Override
    public void onError(int error) {
        stopRecording(true,error);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> stringArrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (stringArrayList != null && stringArrayList.size() > 0) {
            stopRecording(false,0);
            onResult(stringArrayList.get(0));
        } else {
            stopRecording(true,SpeechRecognizer.ERROR_NO_MATCH);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        isRecording();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void _isRecording() {
        if (callIsRecordingOnce) {
            callIsRecordingOnce = false;
            isRecording();
        }
    }

    public void isRecording() {
    }

    public void stopRecording(boolean isError, int errorCode) {

    }

    public void onResult(String resultString) {

    }
}
