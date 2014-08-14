package com.augmate.sdk.scanner;

import com.augmate.sdk.scanner.decoder.DecodingJob;
import com.augmate.sdk.scanner.decoder.scandit.Configuration;
import com.augmate.sdk.scanner.decoder.zxing.IBarcodeScannerWrapper;
import com.augmate.sdk.scanner.decoder.zxing.ZXingOriginalQrOnlyWrapper;

/**
 * Barcode decoding critical paths start here
 * must work outside of android in a standalone java CLI app
 */
public class DecoderManager {
    IBarcodeScannerWrapper barcodeScanner;

    public static Configuration ScanditConfiguration;

    public DecoderManager() {
        //barcodeScanner = new ScanditWrapper(ScanditConfiguration); // native based Scandit
        //barcodeScanner = new ZXingHackWrapper(); // java based ZXing with native hacks
        barcodeScanner = new ZXingOriginalQrOnlyWrapper(); // tried and true completely Java based
        //barcodeScanner = new ZXingNativeWrapper(); // unsupported native ZXing port that we should fork and publicly contribute to
    }

    public void process(DecodingJob job) {
        barcodeScanner.process(job);
    }
}
