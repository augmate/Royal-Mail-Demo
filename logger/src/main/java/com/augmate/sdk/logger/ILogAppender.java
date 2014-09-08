package com.augmate.sdk.logger;

public interface ILogAppender {
    /**
     * Appends log message to queue
     *
     * @param level
     * @param formattedMsg
     */
    public void append(LogLevel level, String formattedMsg);

    /**
     * Flushes message from queue out to a remote server or local console/syslog
     */
    public void flush();

    public void shutdown();
}
