package com.augmate.sdk.logger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * a way to track whether application has any open activities
 * this can be used by the logger service to shutdown when no activities are found
 */
public class AppActivityMonitor {
    private static int activityStackSize = 0;
    private static void monitorApplicationActivity(Application ctx) {
        ctx.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.debug("Activity Started: " + activity.getLocalClassName());
                activityStackSize++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.debug("Activity Stopped: " + activity.getLocalClassName());
                activityStackSize--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static int getActivityStackSize() {
        return activityStackSize;
    }
}
