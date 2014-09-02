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

    // global histogram binarization to packed int bit-buffer
    public static native void globalHistogramBinarizeToIntBuffer(byte[] src, int[] dst, int width, int height);
    public static native int estimateBlackPoint(int[] buckets, int numBuckets);

    static {
        System.loadLibrary("native-scanner");
    }
}
