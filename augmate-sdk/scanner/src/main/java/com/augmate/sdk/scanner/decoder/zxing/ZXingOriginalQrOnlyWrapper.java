package com.augmate.sdk.scanner.decoder.zxing;

import android.graphics.Point;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.decoder.BarcodeResult;
import com.augmate.sdk.scanner.decoder.DecodingJob;
import com.google.zxing.*;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * Wrapper around the original untouched open-source yet unsupported ZXing java barcode detector
 * All benchmarks for ZXing hacks will be compared against this.
 * For speed improvement, we are only handling QR codes here.
 */
public class ZXingOriginalQrOnlyWrapper implements IBarcodeScannerWrapper {
    protected QRCodeReader qrCodeReader = new QRCodeReader();

    @Override
    public void process(DecodingJob job) {
        byte[] data = job.getLuminance();
        int width = job.getWidth();
        int height = job.getHeight();

        // zero confidence unless we detect and decode a qrcode
        job.result.confidence = 0;

        job.decodeStartedAt = What.timey();
        job.binarizationAt = What.timey();

        // binarization is where an incredible amount of time is wasted
        // bitmap.getBlackMatrix() is very expensive as a result of HybridBinarizer (and the global one too)
        // a native port of just this step makes things 2-3x faster right off the bat
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
        BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));

        try {

            // force binarization now. result gets cached.
            // this way we can measure how long this process takes. (it's pretty slow)
            bitmap.getBlackMatrix();

            job.locatingAt = What.timey();
            job.parsingAt = What.timey();

            Result result = qrCodeReader.decode(bitmap);

            if (result != null) {

                // bottomLeft, topLeft, topRight
                ResultPoint[] pts = result.getResultPoints();

                Point pt1 = new Point((int) pts[0].getX(), (int) pts[0].getY());
                Point pt2 = new Point((int) pts[1].getX(), (int) pts[1].getY());
                Point pt3 = new Point((int) pts[2].getX(), (int) pts[2].getY());

                Point rightVector = new Point(pt3.x - pt2.x, pt3.y - pt2.y);
                Point pt4 = new Point(pt1.x + rightVector.x, pt1.y + rightVector.y); // bottom right

                job.result.setDirectly(pt1, pt2, pt3, pt4);

                // confidence values [0,1]
                job.result.confidence = 1;
                job.result.value = result.getText();
                job.result.format = BarcodeResult.Format.QRCode;
                job.result.timestamp = What.timey();
            }

        } catch (NotFoundException err) {
            // expected failure case when zxing-lib doesn't locate a qr-code. don't log.
        } catch (ChecksumException err) {
            // not a problem, just interesting
            //Log.debug("ZXing detected checksum error in QR code");
        } catch (Exception err) {
            // unexpected failure. worth noting.
            Log.exception(err, "Error detecting QR code using ZXing");
        }

        job.decodeCompletedAt = What.timey();
    }
}
