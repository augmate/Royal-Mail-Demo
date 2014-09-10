package com.augmate.sdk.logger;

import android.content.Context;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Log wraps multiple logger systems into one neat package.
 * Currently it supports:
 * + Logentries integration
 * + Standard LogCat compatible dbg-output
 *
 * Ditched log4j and moved to a lighter, faster, and more lint-friendly logger
 */
public class Log {
    public static final String SdkLogTag = "AugmateSDK";
    private static LogManager logManager = new LogManager();

    /**
     * Must be called on application or first activity start
     * if not called, then logging will fallback to failsafe (android-logcat)
     * so worst case scenario, you get the default android Log.d() style output
     * @param ctx Context of application or activity
     */
    public static void start(Context ctx) {
        logManager.start(ctx);
    }

    /**
     * Must be called on application shutdown to clean-up socket-based loggers (eg: LogEntries)
     */
    public static void shutdown() {
        logManager.shutdown();
    }

    public static void exception(Exception err, String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            logManager.append(LogLevel.Error, str + "\n" + ExceptionUtils.getStackTrace(err));
    }

    public static void error(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            logManager.append(LogLevel.Error, str);
    }

    public static void warn(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            logManager.append(LogLevel.Warning, str);
    }

    public static void info(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            logManager.append(LogLevel.Info, str);
    }

    public static void debug(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            logManager.append(LogLevel.Debug, str);
    }

    private static String safeFormat(String format, Object... args) {
        String str = null;
        try {
            str = String.format(format, args);
        } catch(Exception err) {
            logManager.append(LogLevel.Error, "Error formatting string: \"" + format + "\"\n" + ExceptionUtils.getStackTrace(err));
        }
        return str;
    }

    /**
     * Would be nice to have an easy way to sprinkle timers throughout code
     * @param name string name of timer
     * @return Timer
     */
    public static Timer startTimer(String name) {
        return new Timer(name);
    }
}
