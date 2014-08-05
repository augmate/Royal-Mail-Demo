package com.augmate.sdk.voice;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.augmate.sdk.logger.Log;

import java.util.ArrayList;

public class VoiceCaptorPlaceholder extends Activity implements IAudioDoneCallback {
    AugmateRecognitionListener listener;
    SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    private TextView promptText, resultsText;
    private ImageView logo, pulse_ring, error_icon;
    private SliderView mProgress;
    private Animation voiceAnim;
    private MediaPlayer start_sound, success_sound, error_sound;
    private String[] recog_errors = {"NETWORK TIMEOUT","NETWORK","AUDIO","SERVER","CLIENT",
            "SPEECH_TIMEOUT","NO_MATCH","RECOGNIZER_BUSY","INSUFFICIENT_PERMISSIONS"};
    final Runnable showLoadingBar = new Runnable()
    {
        public void run()
        {
            if(listener.isProcessing()) mProgress.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.voice_capture);
        listener = new AugmateRecognitionListener(this);
        resultsText = (TextView) findViewById(R.id.results_field);
        promptText = (TextView) findViewById(R.id.prompt_field);
        logo = (ImageView) findViewById(R.id.imageView);
        pulse_ring = (ImageView) findViewById(R.id.imageView2);
        error_icon = (ImageView) findViewById(R.id.imageView3);
        mProgress = (SliderView) findViewById(R.id.indeterm_slider);
        voiceAnim = AnimationUtils.loadAnimation(this, R.anim.grow_then_fade);
        start_sound = MediaPlayer.create(this, R.raw.start_sound);
        success_sound = MediaPlayer.create(this, R.raw.correct_sound);
        error_sound = MediaPlayer.create(this, R.raw.wrong_sound);
        mProgress.startIndeterminate();
    }

    private void startListening() {
        speechRecognizer.setRecognitionListener(listener);
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName())
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public boolean onKeyDown(int keycode, @SuppressWarnings("NullableProblems") KeyEvent event) {
        Log.debug("Caught key-down on key=" + KeyEvent.keyCodeToString(keycode));
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER && !listener.isProcessing()) {
            resultsText.setText(null);
            promptText.setText("Listening...");
            logo.setImageResource(R.drawable.augmate_logo_blue_solid);
            error_icon.setVisibility(View.INVISIBLE);
            pulse_ring.startAnimation(voiceAnim);
            start_sound.start();
            startListening();
        }
        super.onKeyDown(keycode, event);
        return true;
    }



    @Override
    public void onPartial(ArrayList<String> results) {
        resultsText.setText(TextUtils.join(", ", results));
    }

    @Override
    public void onResults(ArrayList<String> results){
        success_sound.start();
        resultsText.setText(TextUtils.join(", ", results));
        promptText.setText("Ready");
        logo.setImageResource(R.drawable.augmate_logo_solid);
        mProgress.setVisibility(View.INVISIBLE);
        pulse_ring.clearAnimation();
    }

    @Override
    public void onEnd() {
        Handler h = new Handler();
       /* Added a tenth second delay for displaying loading bar. If the connection is strong, there is not need to have the bar show up*/
        h.postDelayed(showLoadingBar, 200);
    }

    @Override
    public void onError(int error) {
        error_sound.start();
        promptText.setText("Error " + error + ": " + recog_errors[error-1] + " ERROR");
        logo.setImageResource(R.drawable.augmate_logo_red_solid);
        mProgress.setVisibility(View.INVISIBLE);
        error_icon.setVisibility(View.VISIBLE);
        pulse_ring.clearAnimation();
        switch (error) {
            case 1:
                resultsText.setText("No network connection available");
                break;
            case 2:
                resultsText.setText("No network connection available");
                break;
            case 3:
                resultsText.setText("Audio Recording error");
                break;
            case 4:
                resultsText.setText("Server error");
                break;
            case 5:
                resultsText.setText("Client error. Please try again or check your network connection.");
                break;
            case 6:
                resultsText.setText("I didn't catch that. Please try again.");
                break;
            case 7:
                resultsText.setText("No recognition result matched. Please try again.");
                break;
            case 8:
                resultsText.setText("Speech recognizer is busy. Please wait then try again.");
                break;
            case 9:
                resultsText.setText("Insufficient permissions. You do not have access to this device's audio recorder.");
                break;
            default:
                promptText.setText("Unknown error '" + error + "'. Try again?");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy(){
        closeMediaPlayer(start_sound);
        closeMediaPlayer(error_sound);
        closeMediaPlayer(success_sound);
        if(speechRecognizer!=null)
            speechRecognizer.destroy();
        super.onDestroy();
    }

    private void closeMediaPlayer(MediaPlayer m){
        m.reset();
        m.release();
    }
}
