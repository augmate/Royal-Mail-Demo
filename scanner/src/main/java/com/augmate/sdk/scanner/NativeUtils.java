package com.augmate.sdk.scanner;

/**
 * JNI interface for native barcode scanning
 */
public class NativeUtils {

    // binarization experimentation
    public static native void binarize(byte[] src, byte[] dst, int width, int height);
    public static native void binarizeToIntBuffer(byte[] src, int[] dst, int width, int height);

    // ZXing native port
    public static native void zxingNativeDecode(byte[] src, int width, int height);

    static {
        System.loadLibrary("native-scanner");
    }
}
