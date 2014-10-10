package com.augmate.sdk.scanner.decoder.zxing;

public class ZXingJavaCLI {

    public static native void zxingNativeDecode(byte[] src, int width, int height);

    static {
        System.loadLibrary("native-scanner");
    }

    public static void main(String[] args) {
        System.out.println("Native decoder library loaded.");

        // TODO: execute it against a sample framebuffer
    }
}
