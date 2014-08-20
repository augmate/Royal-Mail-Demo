package com.augmate.sample.common;

import android.content.Context;
import android.media.AudioManager;

import com.google.android.glass.media.Sounds;

/**
 * Created by cesaraguilar on 8/20/14.
 */
public class SoundHelper {

    public static void tap(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
    }

    public static void dismiss(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.DISMISSED);
    }

    public static void success(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
    }

    public static void error(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.ERROR);
    }

    public static void disallow(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.DISALLOWED);
    }

}
