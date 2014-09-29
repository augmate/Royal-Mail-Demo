package com.augmate.sdk.scanner.decoder.zxing;


import android.graphics.Point;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.NativeUtils;
import com.augmate.sdk.scanner.decoder.BarcodeResult;
import com.augmate.sdk.scanner.decoder.DecodingJob;
import com.google.zxing.NotFoundException;

public class ZXingHackWrapper implements IBarcodeScannerWrapper {
    protected static byte[] binaryMatrix = new byte[0];

    @Override
    public void process(DecodingJob job) {
        byte[] data = job.getLuminance();
        int width = job.getWidth();
        int height = job.getHeight();

        job.decodeStartedAt = What.timey();

        if (binaryMatrix.length < width * height)
            binaryMatrix = new byte[width * height];

        job.binarizationAt = What.timey();

        //NativeUtils.zxingNativeDecode(data, width, height);

        NativeUtils.binarize(data, binaryMatrix, width, height);
        //NativeUtils.binarizeToIntBuffer(data, job.getDebugOutputBuffer(), width, height);

        job.locatingAt = What.timey();

        // give ZXing a try
        PatternFinder finder = new PatternFinder(binaryMatrix, width, height);

        try {
            FinderPatternInfo info = finder.find(null);
            //job.result = new Result("Detect Only", null, new ResultPoint[]{info.getBottomLeft(), info.getTopLeft(), info.getTopRight()}, BarcodeFormat.QR_CODE);

            Point pt1 = new Point((int) info.getBottomLeft().getX(), (int) info.getBottomLeft().getY());
            Point pt2 = new Point((int) info.getTopLeft().getX(), (int) info.getTopLeft().getY());
            Point pt3 = new Point((int) info.getTopRight().getX(), (int) info.getTopRight().getY());
            Point pt4 = new Point(pt3.x, pt1.y); // bottom right

            job.result.setDirectly(pt1, pt2, pt3, pt4);

            // confidence of 5 = barcode decoded
            // values of 1 and 2 = sees something that might be a barcode
            // value of 0 or -1 = nothing in the image worth looking at
            job.result.confidence = 1;
            job.result.value = "Detected but not parsed";
            job.result.format = BarcodeResult.Format.QRCode;
            job.result.timestamp = What.timey();

            //Log.debug("found barcode corners: %d,%d, %d,%d, %d,%d %d,%d", pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y, pt4.x, pt4.y);

        } catch (NotFoundException err) {
            // expected failure case when zxing-lib doesn't locate a qr-code. don't log.
        } catch (Exception err) {
            // unexpected failure
            Log.exception(err, "Error while detecting QR code in image");
        }

        job.parsingAt = What.timey();

        job.decodeCompletedAt = What.timey();
    }
}
