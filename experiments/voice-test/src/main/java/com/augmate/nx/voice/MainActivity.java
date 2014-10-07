package com.augmate.nx.voice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.WindowManager;
import com.augmate.sdk.logger.Log;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends Activity {

    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.start(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startSpeechRecognition();
    }

    private void startSpeechRecognition() {
        if (speechRecognizer != null)
            return;

        Log.debug("Starting speech recognizer..");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new SpeechListener());

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                //.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-419")
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                .putExtra(RecognizerIntent.EXTRA_PROMPT, "This is a prompt.")
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName())
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.startListening(intent);
    }

    /**
     * this is supposed to provide a list of supported languages.
     * but getVoiceDetailsIntent() returns null. and manually creating the intent doesn't produce results either.
     * sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(this), null, new LanguageDetailsReceiver(), null, Activity.RESULT_OK, null, null);
     */
    private static class LanguageDetailsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);
            Log.debug("Got %d result entries", results.keySet().size());
            if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                Log.debug("Extra language pref: " + results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
            }
            if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                ArrayList<String> languages = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                Log.debug("Extra languages: %s", TextUtils.join(",", languages));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSpeechRecognition();
    }

    private void stopSpeechRecognition() {
        if (speechRecognizer == null) {
            return;
        }

        Log.debug("Stopping speech recognizer..");
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
        speechRecognizer = null;
    }

}
