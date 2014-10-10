package com.augmate.sdk.logger;

import android.os.SystemClock;
import org.apache.commons.lang3.ClassUtils;

public class What {

    /**
     * Current time
     *
     * @return
     */
    public static long timey() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Current thread's frame stack
     *
     * @return
     */
    public static StackTraceElement[] stack() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * Greatest song ever.
     *
     * @return
     */
    public static String isLove() {
        return "baby don't hurt me.";
    }

    /**
     * Finds the top and most interesting caller frame
     *
     * @param popFrames number of frames to ignore from the top of the stack
     * @return Frame ready for debug output
     */
    public static Frame frameAt(int popFrames) {
        StackTraceElement[] stack = stack();
        StackTraceElement callerFrame = null;

        boolean nextFrameOut = false;

        for (StackTraceElement frame : stack) {
            String frameClass = frame.getClassName();

            // ignore Log class, find who is calling it.

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

        Frame frame = new Frame();

        if (callerFrame != null) {
            // trim namespace down to just the classname
            frame.fullClassName = callerFrame.getClassName();
            frame.trimmedClassName = frame.fullClassName.substring(frame.fullClassName.lastIndexOf('.') + 1);
            frame.shortPath = frame.trimmedClassName + "::" + callerFrame.getMethodName();
            frame.packageName = ClassUtils.getPackageName(frame.fullClassName);
        }

        return frame;
    }

    public static class Frame {
        public String fullClassName;
        public String trimmedClassName;
        public String packageName;
        public String shortPath = "<N/A>";
    }
}
