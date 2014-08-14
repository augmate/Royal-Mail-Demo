package com.augmate.sdk.logger.Local;

import com.augmate.sdk.logger.Logentries.LogentriesFormat;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class LocalFormat extends Layout
{
    private String sessionId;
    private String deviceId;
    
    public LocalFormat(String sessionId, String deviceId) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;
    }
    
    @Override
    public String format(LoggingEvent event) {
        String caller = LogentriesFormat.getFrame(0);
        String thread = Thread.currentThread().getName();
        String formatted = "" + deviceId + " | #" + sessionId + " | " + String.format("%-9s", thread) + " | " + caller + "()";

        return formatted + "; " + event.getMessage();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }
}
