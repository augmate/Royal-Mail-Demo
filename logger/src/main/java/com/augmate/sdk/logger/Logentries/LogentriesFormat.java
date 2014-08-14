package com.augmate.sdk.logger.Logentries;

import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class LogentriesFormat extends Layout
{
    private final DateTimeFormatter timestampFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private String sessionId;
    private String deviceId;
    
    public LogentriesFormat(String sessionId, String deviceId) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;
    }
    
    /*
        when calling Log4Aug.debug():
        skip frames until arriving to app.utils
        skip log4j frames
        skip any left-over app.utils frames (or specifically the public static logger class everyone will use)
        the very next frame is the caller
        allow an optional extra pop
        
            D/Log     ( 2590): dumping stack:
            D/Log     ( 2590):   frame: dalvik.system.VMStack:getThreadStackTrace
            D/Log     ( 2590):   frame: java.lang.Thread:getStackTrace
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.LogPatternLayout:getFrame
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.LogPatternLayout:format
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.AndroidDbgAppender:append
            D/Log     ( 2590):   frame: org.apache.log4j.AppenderSkeleton:doAppend
            D/Log     ( 2590):   frame: org.apache.log4j.helpers.AppenderAttachableImpl:appendLoopOnAppenders
            D/Log     ( 2590):   frame: org.apache.log4j.Category:callAppenders
            D/Log     ( 2590):   frame: org.apache.log4j.Category:forcedLog
            D/Log     ( 2590):   frame: org.apache.log4j.Category:debug
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.Log4Aug:debug
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.activities.WelcomeActivity:onCreate
            D/Log     ( 2590):   frame: android.app.Activity:performCreate
        
        it's also possible to call the logger directly via log4j:

            D/Log     ( 2590): dumping stack:
            D/Log     ( 2590):   frame: dalvik.system.VMStack:getThreadStackTrace
            D/Log     ( 2590):   frame: java.lang.Thread:getStackTrace
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.LogPatternLayout:getFrame
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.LogPatternLayout:format
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.utils.AndroidDbgAppender:append
            D/Log     ( 2590):   frame: org.apache.log4j.AppenderSkeleton:doAppend
            D/Log     ( 2590):   frame: org.apache.log4j.helpers.AppenderAttachableImpl:appendLoopOnAppenders
            D/Log     ( 2590):   frame: org.apache.log4j.Category:callAppenders
            D/Log     ( 2590):   frame: org.apache.log4j.Category:forcedLog
            D/Log     ( 2590):   frame: org.apache.log4j.Category:debug
            D/Log     ( 2590):   frame: com.augmate.gct_mtg_client.app.activities.WelcomeActivity:onCreate
            D/Log     ( 2590):   frame: android.app.Activity:performCreate
     */
    
    public static String getFrame(int popFrames) {
        StackTraceElement[] stack = What.stack();
        StackTraceElement callerFrame = null;
        String callerPath = "(N/A)";

        boolean nextFrameOut = false;

        for (StackTraceElement frame : stack) {
            String frameClass = frame.getClassName();

            if (nextFrameOut && !frameClass.equals(Log.class.getName()) && popFrames-- <= 0) {
                callerFrame = frame;
                break;
            }

            // wait until we are out of the logging system before counting frames
            if (!nextFrameOut && frameClass.equals(Log.class.getName())) {
                nextFrameOut = true;
            }
        }

        // impossible case? if the top caller was the log itself, use it as the caller source
        if (callerFrame == null && stack.length > 0)
            callerFrame = stack[stack.length - 1];

        if (callerFrame != null) {
            // trim namespace down to just the classname
            String className = callerFrame.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);

            callerPath = className + "::" + callerFrame.getMethodName();
        }

        return callerPath;
    }
    
    @Override
    public String format(LoggingEvent event) {
        String caller = getFrame(0);        
        String timestamp = org.joda.time.DateTime.now().toString(timestampFormat);
        String thread = Thread.currentThread().getName();
        String formatted = timestamp + " [" + deviceId + "] #" + sessionId + " - " + thread + " - " + caller + "(); " + event.getMessage() + '\n';
        
        return formatted;
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }
}
