package com.augmate.sdk.scanner.decoder.zxing;

import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.NativeUtils;
import com.augmate.sdk.scanner.decoder.BarcodeResult;
import com.augmate.sdk.scanner.decoder.DecodingJob;

public class ZXingNativeWrapper implements IBarcodeScannerWrapper {

    @Override
    public void process(DecodingJob job) {
        byte[] data = job.getLuminance();
        int width = job.getWidth();
        int height = job.getHeight();

        job.decodeStartedAt = What.timey();

        job.binarizationAt = What.timey();
        job.locatingAt = What.timey();
        job.parsingAt = What.timey();

        String result = NativeUtils.zxingNativeDecode(data, width, height);

        if(result != null && !result.equals("")) {
            // confidence values [0,1]
            job.result.confidence = 1;
            job.result.value = result;
            job.result.format = BarcodeResult.Format.QRCode;
            job.result.timestamp = What.timey();
        }

        job.decodeCompletedAt = What.timey();
    }
}
