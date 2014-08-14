package com.augmate.sdk.logger;

import android.os.SystemClock;

/**
 * A good timer should be hierarchical. This is a placeholder.
 * Being able to create timers inside the scope of other timers will be a great way to build out
 * easy to read performance profiles.
 */
public class Timer {
    public long start;
    public long span;
    public String str;

    public Timer(String timerName) {
        this.start = SystemClock.elapsedRealtime();
        this.str = timerName;
    }

    /**
     * stop timer and commit it's data to a hierarchical timer manager
     */
    public void stop() {
        span = SystemClock.elapsedRealtime() - start;
        Log.debug(str, span);
    }
}
