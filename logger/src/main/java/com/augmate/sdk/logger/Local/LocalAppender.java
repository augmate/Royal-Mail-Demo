package com.augmate.sdk.logger.Local;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class LocalAppender extends AppenderSkeleton {
    
    @Override
    protected void append(LoggingEvent event) {
        // TODO: allow customizable per-package (and per-class override) tags
        // eg: com.augmate.sdk.* can have a tag of "Augmate.SDK"
        //     com.digital-agency.truck-loading.* can have a tag of "DigitalAgency.TruckLoading"

        String logLine = "Augmate";
        String msg = getLayout().format(event);

        if( event.getLevel() == Level.DEBUG)
            android.util.Log.d(logLine, msg);

        if( event.getLevel() == Level.INFO)
            android.util.Log.i(logLine, msg);

        if( event.getLevel() == Level.ERROR)
            android.util.Log.e(logLine, msg);

        if( event.getLevel() == Level.WARN)
            android.util.Log.wtf(logLine, msg);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
