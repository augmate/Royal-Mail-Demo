package com.augmate.apps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.augmate.apps.SplashActivity;

/**
 * @author James Davis (Fuzz)
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent launchIntent = new Intent(context, SplashActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }
}
