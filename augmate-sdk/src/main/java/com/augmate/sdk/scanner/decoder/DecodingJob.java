package com.augmate.sdk.scanner.decoder;

import android.os.SystemClock;

public class DecodingJob {

    // filled out by job producer (android activity, java CLI, unit-test)
    private final int width;
    private final int height;
    private final byte[] luminance;
    private int[] debugOutputBuffer;
    public final long requestedAt;

    // filled out by decoder (consumer)
    // TODO: this should be decoder-specific (zxing is more transparent than scandit)
    public long decodeStartedAt;
    public long binarizationAt;
    public long locatingAt;
    public long parsingAt;
    public long decodeCompletedAt;

    public BarcodeResult result = new BarcodeResult();

    public DecodingJob(int width, int height, byte[] luminance, int[] debugOutputBuffer) {
        this.width = width;
        this.height = height;
        this.luminance = luminance;
        this.debugOutputBuffer = debugOutputBuffer;
        this.requestedAt = SystemClock.elapsedRealtime();
    }

    public long binarizationDuration() {
        return locatingAt - binarizationAt;
    }

    public long totalDuration() {
        return decodeCompletedAt - requestedAt;
    }

    public long queueDuration() {
        return decodeStartedAt - requestedAt;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getLuminance() {
        return luminance;
    }

    public int[] getDebugOutputBuffer() {
        return debugOutputBuffer;
    }

    public long localizationDuration() {
        return parsingAt - locatingAt;
    }
}
