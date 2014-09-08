package com.augmate.sdk.logger.Logentries;

import com.augmate.sdk.logger.ILogAppender;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.LogLevel;
import com.augmate.sdk.logger.What;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogentriesAppender implements ILogAppender {
    private final String sessionId;
    private final String deviceId;
    private PrintWriter printWriter;
    private String token;

    // thread-safe fifo log queue
    private ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();

    public LogentriesAppender(String sessionId, String deviceId, String logentriesToken) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.token = logentriesToken;
    }

    private boolean sendMessage(String token, String message) {
        if (printWriter == null || printWriter.checkError()) {
            reconnect();
        }

        if (printWriter != null) {
            printWriter.println(token + " " + message);
            return !printWriter.checkError();
        }

        return false;
    }

    private boolean reconnect() {
        try {
            //android.util.Log.d(Log.SdkLogTag, "Reconnecting to data.logentries.com");
            printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new Socket("data.logentries.com", 80).getOutputStream())));
        } catch (Exception e) {
            printWriter = null;
            android.util.Log.w(Log.SdkLogTag, "Error connecting to data.logentries.com", e);
            return false;
        }
        return true;
    }

    /**
     * called from any thread, UI, networking, image-decoding, or a random anonymous async by root logger
     *
     * @param level        type of message
     * @param moduleName   source module (sdk, app-name, helper library)
     * @param formattedMsg wanted log message
     */
    @Override
    public void append(LogLevel level, String moduleName, String formattedMsg) {
        What.Frame callerFrame = What.frameAt(0);

        String tag = callerFrame.packageName.contains("com.augmate.sdk") ? "AugmateSDK" : "AugmateApp";

        String thread = Thread.currentThread().getName();
        String formatted = tag + " | " + deviceId + " | #" + sessionId + " | " + String.format("%-9s", thread) + " | " + callerFrame.shortPath + "()";
        String finalMsg = formatted + "; " + formattedMsg;

        logQueue.add(finalMsg);
    }

    /**
     * called in a network-safe thread by root logger
     */
    @Override
    public void flush() {
        // FIXME: only remove message from the queue if it has been sent

        while (logQueue.size() > 0) {
            String logEntry = logQueue.poll();
            if (logEntry != null) {
                if (!sendMessage(token, logEntry))
                    break;
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
