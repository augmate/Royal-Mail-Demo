// TODO: add relevant copyright/license info

// HACK: have to use this package for the JNI calls to work out of the box
package com.mirasense.scanditsdk;

import java.io.File;

// reversed out of scandit sdk
public class ScanditSDKBarcodeReader {
    public ScanditSDKBarcodeReader() {

    }

    static {
        System.loadLibrary("scanditsdk-android-4.1.0");
    }

    public void initialize(File filesDir) {
        initializeRecognitionEngine(filesDir.getAbsolutePath(), filesDir.getAbsolutePath(), "Android SDK built for x86"); // hardcoded Build.MODEL to avoid android dependency
    }

    private native void initializeRecognitionEngine(String paramString1, String paramString2, String paramString3);

    public native void deallocRecognitionEngine();

    public native void stopProfiler();

    public native void setupLicenseInformation(String paramString1, String paramString2);

    public native void setCameraUsed(int paramInt);

    public native void setAutoFocusStrategy(int paramInt);

    public native void setOrientation(int paramInt);

    public native void setDeviceModel(String paramString);

    public native void setLegacyDeviceId(String paramString);

    public native void setDeviceId(String paramString);

    public native void setPlatform(String paramString);

    public native void setPlatformVersion(String paramString);

    public native void setUsedFramework(String paramString);

    public native void didStartAutoFocus();

    public native void didFinishAutoFocus();

    public native void setScanLocation(String paramString);

    public native void reportCancellation();

    public native void enableEan13Upc12(boolean paramBoolean);

    public native void enableEan8(boolean paramBoolean);

    public native void enableUpce(boolean paramBoolean);

    public native void enableCode39(boolean paramBoolean);

    public native void enableCode93(boolean paramBoolean);

    public native void enableEan128(boolean paramBoolean);

    public native void enableItf(boolean paramBoolean);

    public native void enableQR(boolean paramBoolean);

    public native void enableDataMatrix(boolean paramBoolean);

    public native void enablePdf417(boolean paramBoolean);

    public native void enableMsiPlessey(boolean paramBoolean);

    public native void enableGS1DataBar(boolean paramBoolean);

    public native void enableGS1DataBarExpanded(boolean paramBoolean);

    public native void enableCodabar(boolean paramBoolean);

    public native void setMsiPlesseyChecksumType(int paramInt);

    public native void setInverseDetectionActive(boolean paramBoolean);

    public native void setImprovedMicroDataMatrixRecognitionActive(boolean paramBoolean);

    public native void force2dRecognition(boolean paramBoolean);

    public native void setDetectionStrategy(int paramInt);

    public native void setMaxNumberOfCodesPerFrame(int paramInt);

    public native void setRelativeRestrictedArea(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

    public native void tryingToDealloc(int paramInt);

    public native boolean canDealloc();

    public native boolean shouldIndicatorStayAtDefaultLocation();

    public native void processImage(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);

    public native void resetEngine();

    public native byte[] fetchResult();

    public native byte[][] fetchResults();

    public native boolean needsFocusedImage();

    public native int[] getCodeCenter();

    public native int[] getCodeSize();

    public native double getCodeAngle();

    public native int getCodeConfidence();

    public native void setHotSpot(float paramFloat1, float paramFloat2);

    public native void setDefaultDetectorAngle(int paramInt);

    public native void setEnableBlurryRecognition(boolean paramBoolean);

    public native void setEnableSharpRecognition(boolean paramBoolean);

    public native void setEnable2dRecognition(boolean paramBoolean);

    public native void setEnableCheckDefaultLocation(boolean paramBoolean);

    public native String getDebugInfoText();

    public native int getLocalizationDebugImageWidth();

    public native int getLocalizationDebugImageHeight();

    public native byte[] getLocalizationDebugImageBytes();

    public native int getLocalizationDebugImage2dWidth();

    public native int getLocalizationDebugImage2dHeight();

    public native byte[] getLocalizationDebugImage2dBytes();
}

