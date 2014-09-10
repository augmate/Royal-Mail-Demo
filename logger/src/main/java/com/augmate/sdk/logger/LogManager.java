package com.augmate.sdk.logger;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.augmate.sdk.logger.Local.LocalAppender;
import com.augmate.sdk.logger.Logentries.LogentriesAppender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LogManager {

    // thread-safe log-appenders list
    private List<ILogAppender> logAppenderList = Collections.synchronizedList(new ArrayList<ILogAppender>());

    // thread-safe blocking fifo queue of log messages
    private Semaphore broadcastSignal = new Semaphore(1);
    private LogBroadcastThread broadcastThread;

    /**
     * Must be called on application startup.
     * Safe to call multiple times. Only the first time will matter.
     *
     * @param ctx Context of the application. If not provided, deviceId will be N/A
     */
    public void start(Context ctx) {

        if (logAppenderList.size() == 0) {
            String deviceId;

            if (ctx != null) {
                deviceId = "ID=" + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            } else {
                deviceId = "S=" + Build.SERIAL;
                android.util.Log.w(Log.SdkLogTag, "Log::start() not called with Application/Activity context. Using build.serial instead of android_id.");
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

    /**
     * Must be called on application shutdown to clean-up socket-based loggers (eg: LogEntries)
     */
    public void shutdown() {
        broadcastThread.shutdown();
    }

    /**
     * Primary feeding mechanism from Log facade
     * @param level message error-level
     * @param msg the message as a string
     */
    public void append(LogLevel level, String msg) {
        // append() can be called without start() having been called earlier
        // we can start it now with degraded functionality
        if (broadcastThread == null)
            start(null);

        for (ILogAppender appender : logAppenderList) {
            appender.append(level, msg);
        }

        broadcastThread.interrupt();
    }

    private void flushAppenders() {
        //android.util.Log.d(Log.SdkLogTag, "flushing appenders on thread: id=" + Thread.currentThread().getId());
        for (ILogAppender appender : logAppenderList) {
            appender.flush();
        }
    }

    class LogBroadcastThread implements Runnable {
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
                    // try to flushAppenders every 5 seconds or when signaled, whichever happens first
                    barrier.tryAcquire(5000, TimeUnit.MILLISECONDS);
                    flushAppenders();
                } catch (Exception ignored) {
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

        // wakes thread from semaphore block and forces parent class's appender flushAppenders() to run
        public void interrupt() {
            barrier.release();
        }

        public void shutdown() {
            isAlive = false;
            barrier.release();
        }
    }
}
