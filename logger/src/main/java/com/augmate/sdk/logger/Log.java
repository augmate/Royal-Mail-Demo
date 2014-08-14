package com.augmate.sdk.logger;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.augmate.sdk.logger.Local.LocalAppender;
import com.augmate.sdk.logger.Local.LocalFormat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Log wraps multiple logger systems into one neat package.
 * Currently it supports:
 * + Logentries integration
 * + Standard LogCat compatible dbg-output
 *
 * It comes with a custom PatternLayout that resolves "class::method()"
 * faster than LocationPatternConverter and lets us control amount of frames
 * popped off the stack to identify the exact caller we care about.
 *
 * Current we build on log4j. But eventually will move to a lighter, faster, and more lint-friendly logger
 */
public class Log {
    private static Logger loggerInstance;

    /**
     * Must be called on application startup
     * @param ctx Context of the application. If not provided, deviceId will be N/A
     */
    public static void start(Context ctx) {
        if (loggerInstance == null) {
            String deviceId = "N/A";

            if (ctx == null) {
                android.util.Log.w("Augmate", "Log::start(null); called without a ctx; creating Log without device-id");
            } else {
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            loggerInstance = createInstance(deviceId);
        }
    }

    /**
     * Must be called on application shutdown to clean-up socket-based loggers (eg: LogEntries)
     */
    public static void shutdown() {
        LogManager.shutdown();
    }

    private static Logger createInstance(String deviceId) {

        // not super random or collision free, but good enough for quick grouping/filtering of logs by unique runs
        String sessionId = Long.toString(Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits()), 36).substring(0, 6);

        /* FIXME: LogEntries library needs to be updated
        // remote output
        LogentriesAppender logentriesAppender = new LogentriesAppender();
        logentriesAppender.setToken("c3a45763-9854-43cc-838a-7a1b71418c6c");
        logentriesAppender.setDebug(true);
        logentriesAppender.setLayout(new LogentriesFormat(sessionId, deviceId));
        logentriesAppender.setSsl(false);
        */

        // local output
        LocalAppender localAppender = new LocalAppender();
        localAppender.setLayout(new LocalFormat(sessionId, deviceId));

        Logger logger = Logger.getRootLogger();
        logger.addAppender(localAppender);
        //lgr.addAppender(logentriesAppender);

        logger.debug("Started session #" + sessionId + " + logging on " + Build.MANUFACTURER + " " + Build.MODEL + " version=" + Build.ID);

        return logger;
    }

    private static Logger getLogger() {
        if (loggerInstance == null)
            start(null);

        return loggerInstance;
    }

    public static void exception(Exception err, String format, Object... args) {
        getLogger().error(String.format(format, args));
        getLogger().error(ExceptionUtils.getStackTrace(err));
    }

    public static void error(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            getLogger().error(String.format(format, args));
    }

    public static void debug(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            getLogger().debug(str);
    }

    public static void info(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            getLogger().info(str);
    }
    public static void warn(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            getLogger().warn(str);
    }

    private static String safeFormat(String format, Object... args) {
        String str = null;
        try {
            str = String.format(format, args);
        } catch(Exception err) {
            getLogger().error("Error formatting string: " + format);
            getLogger().error(ExceptionUtils.getStackTrace(err));
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
