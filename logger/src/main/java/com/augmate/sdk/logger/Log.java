package com.augmate.sdk.logger;

import android.content.Context;
import android.provider.Settings;
import com.augmate.sdk.logger.Local.LocalAppender;
import com.augmate.sdk.logger.Logentries.LogentriesAppender;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Log wraps multiple logger systems into one neat package.
 * Currently it supports:
 * + Logentries integration
 * + Standard LogCat compatible dbg-output
 *
 * Ditched log4j and moved to a lighter, faster, and more lint-friendly logger
 */
public class Log {
    // thread-safe log-appenders list
    private static List<ILogAppender> logAppenderList = Collections.synchronizedList(new ArrayList<ILogAppender>());
    public static final String SdkLogTag = "AugmateSDK";

    // thread-safe blocking fifo queue of log messages
    private static Semaphore broadcastSignal = new Semaphore(1);
    private static LogBroadcastThread broadcastThread;

    /**
     * Must be called on application startup.
     * Not re-entry safe
     * @param ctx Context of the application. If not provided, deviceId will be N/A
     */
    public static void start(Context ctx) {
        if(logAppenderList.size() == 0) {
            String deviceId = "N/A";

            if (ctx == null) {
                android.util.Log.w(Log.SdkLogTag, "Log::start(null); called without a ctx; creating Log without device-id");
            } else {
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            // not super random or collision free, but good enough for quick grouping/filtering of logs by unique runs
            String sessionId = Long.toString(Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits()), 36).substring(0, 6);

            logAppenderList.add(new LocalAppender(sessionId, deviceId));
            logAppenderList.add(new LogentriesAppender(sessionId, deviceId, "8f4167f8-bcd7-47b4-a1e1-8a6afbb0e8d9"));

            // start broadcasting thread
            broadcastThread = new LogBroadcastThread(broadcastSignal);
            new Thread(broadcastThread).start();
        }
    }

    private static void append(LogLevel level, String module, String msg) {
        for(ILogAppender appender : logAppenderList) {
            appender.append(level, module, msg);
        }

        if(broadcastThread == null)
            start(null);

        broadcastThread.interrupt();
    }

    private static void flush() {
        //android.util.Log.d(Log.SdkLogTag, "flushing appenders on thread: id=" + Thread.currentThread().getId());
        for(ILogAppender appender : logAppenderList) {
            appender.flush();
        }
    }

    /**
     * Must be called on application shutdown to clean-up socket-based loggers (eg: LogEntries)
     */
    public static void shutdown() {
        broadcastThread.shutdown();
    }

    public static void exception(Exception err, String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            append(LogLevel.Error, "Augmate", str + "\n" + ExceptionUtils.getStackTrace(err));
    }

    public static void error(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            append(LogLevel.Error, "Augmate", str);
    }

    public static void warn(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            append(LogLevel.Warning, "Augmate", str);
    }

    public static void info(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            append(LogLevel.Info, "Augmate", str);
    }

    public static void debug(String format, Object... args) {
        String str = safeFormat(format, args);
        if(str != null)
            append(LogLevel.Debug, "Augmate", str);
    }

    private static String safeFormat(String format, Object... args) {
        String str = null;
        try {
            str = String.format(format, args);
        } catch(Exception err) {
            append(LogLevel.Error, Log.SdkLogTag, "Error formatting string: \"" + format + "\"\n" + ExceptionUtils.getStackTrace(err));
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

    static class LogBroadcastThread implements Runnable {
        private Semaphore barrier;
        private boolean isAlive = true;

        public LogBroadcastThread(Semaphore barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("logger");
            Log.debug("Started log broadcasting thread with id=" + Thread.currentThread().getId());

            while(true) {
                try {
                    // try to flush every 5 seconds or when signaled, whichever happens first
                    barrier.tryAcquire(5000, TimeUnit.MILLISECONDS);
                    flush();
                } catch (Exception e) {
                    //android.util.Log.e(Log.SdkLogTag, "Error", e);
                }

                if(!isAlive)
                    break;
            }

            // FIXME: never reach here because android applications don't call Application::onTerminate
            //        and so spawned threads never get a chance to shutdown :(

            android.util.Log.d(Log.SdkLogTag, "Shutting down log appenders..");

            for(ILogAppender appender : logAppenderList) {
                appender.shutdown();
            }
        }

        // wakes thread from semaphore block and forces parent class's appender flush() to run
        public void interrupt() {
            barrier.release();
        }

        public void shutdown() {
            isAlive = false;
            barrier.release();
        }
    }
}
