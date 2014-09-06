package com.augmate.sample.common;

import android.content.Context;
import android.widget.TextView;

import com.augmate.sample.AugmateApplication;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by cesaraguilar on 9/4/14.
 */
public class FontHelper {

    public static void updateFontForBrightness(TextView ...views) {
        Context context = AugmateApplication.getContext();
        float brightness = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE).getFloat("BRIGHTNESS",0.5f);
        String fontName = "fonts/GothamNarrow-Thin.otf";
        if (brightness < 0.25f) {
            fontName = "fonts/GothamNarrow-Medium.otf";
        }
        for (TextView view : views) {
            CalligraphyUtils.applyFontToTextView(view.getContext(),view,fontName);
        }
    }
}
