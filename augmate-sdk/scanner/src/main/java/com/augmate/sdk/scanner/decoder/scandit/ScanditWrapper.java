package com.augmate.sdk.scanner.decoder.scandit;

import android.os.Build;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.decoder.BarcodeResult;
import com.augmate.sdk.scanner.decoder.DecodingJob;
import com.augmate.sdk.scanner.decoder.zxing.IBarcodeScannerWrapper;
import com.mirasense.scanditsdk.ScanditSDKBarcodeReader;

// all reader interaction reversed out of scandit sdk
// TODO: add relevant copyright/license info

public class ScanditWrapper implements IBarcodeScannerWrapper {
    protected ScanditSDKBarcodeReader reader;

    public ScanditWrapper(Configuration cfg) {
        reader = new ScanditSDKBarcodeReader();
        String appKey = "r5Zv3MXCEeOFwbn5ZXY54sJhMiM33cd/9czhbX62fmA";

        reader.setLegacyDeviceId(SbSystemUtils.sha1Digest(cfg.deviceId));
        reader.setDeviceId(SbSystemUtils.sha1Digest(cfg.deviceId));
        reader.setDeviceModel(Build.MODEL);
        reader.setPlatform("android");
        reader.setPlatformVersion(Build.VERSION.RELEASE);
        reader.setUsedFramework(ScanditSDKGlobals.usedFramework);

        reader.initialize(cfg.filesDir);
        reader.setupLicenseInformation(appKey, cfg.platformAppId);
        reader.setEnable2dRecognition(true);
    }

    // straight out of scandit.
    private static String[] getCodeAndSymbology(String concatString) {
        if ((concatString == null) || (concatString.trim().length() == 0)) {
            return null;
        }
        String[] result = new String[2];
        int i = concatString.indexOf(":symbology:");
        if (i < 0) {
            return null;
        }
        result[0] = concatString.substring(0, i).trim();
        result[1] = concatString.substring(i + 11).trim();

        return result;
    }

    @Override
    public void process(DecodingJob job) {
        byte[] data = job.getLuminance();
        int width = job.getWidth();
        int height = job.getHeight();

        job.decodeStartedAt = What.timey();

        //Log.debug("asking scandit to processImage()..");
        reader.processImage(data, width * height, width, height);
        //Log.debug("asking scandit to processImage().. Done");

        byte[][] results = reader.fetchResults();

        String bestResultValue = null;
        BarcodeResult.Format bestResultFormat = BarcodeResult.Format.UNKNOWN;

        job.result.confidence = 0;

        // this is fugly
        if (results != null) {
            if (results.length > 0) {
                Log.debug("result set has %d items", results.length);

                for (byte[] fetchedBytes : results) {
                    //Log.debug("  item bytes = " + fetchedBytes);
                    if (fetchedBytes == null)
                        continue;
                    String fetchedResult = new String(fetchedBytes);
                    String[] barcodeStr = getCodeAndSymbology(fetchedResult);

                    Log.debug("    -> fetchedResult: [%s]", fetchedResult);

                    if (barcodeStr != null && barcodeStr.length >= 2) {
                        Log.debug("    -> barcodeStr: [%s] [%s]", barcodeStr[0], barcodeStr[1]);
                        bestResultValue = barcodeStr[0];
                        if (barcodeStr[1] == "QR")
                            bestResultFormat = BarcodeResult.Format.QRCode;
                    }
                }
            }
        } else {
            Log.debug("got null results list");
        }

        if (reader.getCodeCenter() != null) {
            int[] center = reader.getCodeCenter();
            int[] locationSize = reader.getCodeSize();

            job.result.setAABB(center[0], center[1], locationSize[0], locationSize[1]);

            // confidence of 5 = barcode decoded
            // values of 1 and 2 = sees something that might be a barcode
            // value of 0 or -1 = nothing in the image worth looking at
            job.result.confidence = Math.min(5, Math.max(0, reader.getCodeConfidence() / 5));
            job.result.value = bestResultValue;
            job.result.format = bestResultFormat;
            job.result.timestamp = What.timey();

            Log.debug("found code center center: %d,%d  size: %d,%d  confidence: %d", center[0], center[1], locationSize[0], locationSize[1], reader.getCodeConfidence());
        }

        job.decodeCompletedAt = What.timey();
    }
}
