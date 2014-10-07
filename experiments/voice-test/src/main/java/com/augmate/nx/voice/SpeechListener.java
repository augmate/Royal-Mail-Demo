package com.augmate.nx.voice;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import com.augmate.sdk.logger.Log;

import java.util.ArrayList;

class SpeechListener implements RecognitionListener {
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.debug("Is ready for speech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.debug("Speech capture started");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.debug("Volume level of audio stream changed to: %.2f", rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.debug("Received partial audio buffer of length: %d bytes", buffer.length);
    }

    @Override
    public void onEndOfSpeech() {
        Log.debug("Speech capture ended");
    }

    @Override
    public void onError(int errorId) {
        Log.error("Hit speech recognizer error: %s (code=%d)", ErrorResolver.getErrorString(errorId), errorId);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> stringArrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.debug("Found %d final results", stringArrayList.size());

        for (String result : stringArrayList) {
            Log.debug("  final: [%s]", result);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> stringArrayList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.debug("Found %d partial results", stringArrayList.size());

        for (String result : stringArrayList) {
            Log.debug("  partial: [%s]", result);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.debug("Hit eventType=%d", eventType);
    }
}
