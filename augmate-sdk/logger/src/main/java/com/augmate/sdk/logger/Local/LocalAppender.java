package com.augmate.sdk.logger.Local;

import com.augmate.sdk.logger.ILogAppender;
import com.augmate.sdk.logger.LogLevel;
import com.augmate.sdk.logger.What;

public class LocalAppender implements ILogAppender {

    private final String sessionId;
    private final String deviceId;

    public LocalAppender(String sessionId, String deviceId) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;
    }

    @Override
    public void append(LogLevel level, String formattedMsg) {
        What.Frame callerFrame = What.frameAt(0);

        String tag = callerFrame.packageName.contains("com.augmate.sdk") ? "AugmateSDK" : "AugmateApp";
        String thread = Thread.currentThread().getName();
        String formatted = "#" + sessionId + " | " + String.format("%-9s", thread) + " | " + callerFrame.shortPath + "()";
        String finalMsg = formatted + "; " + formattedMsg;

        switch (level) {
            case Performance:
                android.util.Log.d(tag, finalMsg);
                break;
            case Debug:
                android.util.Log.d(tag, finalMsg);
                break;
            case Analytics:
                android.util.Log.d(tag, finalMsg);
                break;
            case Info:
                android.util.Log.i(tag, finalMsg);
                break;
            case Warning:
                android.util.Log.w(tag, finalMsg);
                break;
            case Error:
                android.util.Log.e(tag, finalMsg);
                break;
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void shutdown() {

    }
}
