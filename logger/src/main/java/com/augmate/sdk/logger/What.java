package com.augmate.sdk.logger;

import android.os.SystemClock;

public class What {

    /**
     * Current time
     * @return
     */
    public static long timey() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Current thread's frame stack
     * @return
     */
    public static StackTraceElement[] stack() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * Greatest song ever.
     * @return
     */
    public static String isLove() {
        return "baby don't hurt me.";
    }
}
