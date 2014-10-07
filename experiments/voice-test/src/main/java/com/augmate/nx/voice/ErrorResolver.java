package com.augmate.nx.voice;

import android.speech.SpeechRecognizer;

public class ErrorResolver {
    public static String getErrorString(int errorId) {
        switch (errorId) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network operation timed out";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server sent an error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client-side error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No recognition result matched";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService is busy";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            default:
                return "Unknown error";
        }
    }
}
