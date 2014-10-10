package com.augmate.sdk.logger.Local;

import android.util.Log;
import com.augmate.sdk.logger.ILogAppender;
import com.augmate.sdk.logger.LogLevel;
import com.augmate.sdk.logger.What;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalFileAppender implements ILogAppender {

    private final String sessionId;
    private final String deviceId;
    private BufferedWriter logFile = null;

    public LocalFileAppender(String sessionId, String deviceId) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;

        File augmateDir = new File("sdcard/augmate");

        augmateDir.mkdir();

        try {
            logFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(augmateDir, getNewLogName()), true)));
        } catch (FileNotFoundException e) {
            Log.e(getTag(), e.getMessage());
        }

        cleanUpOldLogs(augmateDir);
    }

    @Override
    public void append(LogLevel level, String formattedMsg) {
        String tag = getTag();
        String thread = Thread.currentThread().getName();
        String formatted = deviceId + " | #" + sessionId + " | " + String.format("%-10s", thread) + " | " + What.frameAt(0).shortPath + "()";
        String finalMsg = formatted + "; " + formattedMsg + "\n";

        try {
            logFile.append(tag + " " + level.toString().toUpperCase() + " " + finalMsg);
        } catch (IOException e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    @Override
    public void flush() {
        try {
            logFile.flush();
        } catch (IOException e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        try {
            logFile.close();
        } catch (IOException e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    private void cleanUpOldLogs(File logDir) {
        long weekAgo = new Date().getTime() - 604800000;

        for(File file: logDir.listFiles()){
            if(file.getName().endsWith(".log") && file.lastModified() < weekAgo){
                file.delete();
            }
        }
    }

    private String getTag() {
        return new SimpleDateFormat("MM-dd HH:mm:ss.SSS ").format(new Date()) +
                (What.frameAt(0).packageName.contains("com.augmate.sdk") ? "AugmateSDK" : "AugmateApp");
    }

    private String getNewLogName() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log";
    }
}
