package com.augmate.sdk.logger;

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
        String caller = LogEntriesFormat.getFrame(0);
        String thread = Thread.currentThread().getName();
        String formatted = "[Device=" + deviceId + "] #" + sessionId + " | " + thread + " | " + caller + "()";

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
