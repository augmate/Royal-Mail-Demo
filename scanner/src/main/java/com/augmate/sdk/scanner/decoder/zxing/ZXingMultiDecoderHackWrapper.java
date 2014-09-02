package com.augmate.sdk.scanner.decoder.zxing;

import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.decoder.BarcodeResult;
import com.augmate.sdk.scanner.decoder.DecodingJob;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Wrapper around the hacked (partly native) open-source ZXing java barcode detector
 */
public class ZXingMultiDecoderHackWrapper implements IBarcodeScannerWrapper {
    protected BitMatrix binarizedBuffer = new BitMatrix(1280, 720);

    /**
     * Run this on app start to preallocate necessary buffers before scan is requested
     */
    public static void initialize() {
        Log.debug("Preloading..");
    }


    @Override
    public void process(DecodingJob job) {
        byte[] data = job.getLuminance();
        int width = job.getWidth();
        int height = job.getHeight();

        // zero confidence unless we detect and decode a qrcode
        job.result.confidence = 0;

        job.decodeStartedAt = What.timey();
        job.binarizationAt = job.decodeStartedAt;

        if(binarizedBuffer.getWidth() != width || binarizedBuffer.getHeight() != height) {
            Log.debug("Resizing binarization buffer to: %d x %d", width, height);
            binarizedBuffer = new BitMatrix(width, height);
        }

        // binarization is where an incredible amount of time is wasted
        // bitmap.getBlackMatrix() is very expensive as a result of HybridBinarizer (and the global one too)
        // a native port of just this step makes things 2-3x faster right off the bat
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);

        // using a simple native rewrite of the global-histogram binarizer
        BinaryBitmap bitmap = new BinaryBitmap(new SimpleBinarizer(source, binarizedBuffer));
        //BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));

        try {

            // force binarization now. result gets cached.
            // this way we can measure how long this process takes. (it's pretty slow)
            bitmap.getBlackMatrix();

            job.locatingAt = What.timey();
            job.parsingAt = What.timey();

            Collection<BarcodeFormat> formats = new ArrayList<>();
            formats.add(BarcodeFormat.CODE_39);
            formats.add(BarcodeFormat.CODE_128);
            formats.add(BarcodeFormat.UPC_A);
            formats.add(BarcodeFormat.UPC_E);
            formats.add(BarcodeFormat.EAN_13);
            formats.add(BarcodeFormat.EAN_8);
            formats.add(BarcodeFormat.QR_CODE);

            HashMap<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap, hints);

            if (result != null) {

                // bottomLeft, topLeft, topRight
                ResultPoint[] pts = result.getResultPoints();

//                Point pt1 = null;
//                Point pt2 = null;
//                Point pt3 = null;
//                Point pt4 = null;
//
//                if (pts.length == 2) {
//                    // pts[0] and pts[1] form a horizontal line within the image
//                    // so we pad it vertically up and down and create a small rectangle
//                    pt1 = new Point((int) pts[0].getX(), (int) pts[0].getY() - 10);
//                    pt2 = new Point((int) pts[1].getX(), (int) pts[1].getY() - 10);
//                    pt3 = new Point((int) pts[0].getX(), (int) pts[0].getY() + 10);
//                    pt4 = new Point((int) pts[1].getX(), (int) pts[1].getY() + 10);
//                }
//
//                if (pts.length == 3) {
//                    // pts[0,1,2] represent 3 points of a triangle capturing the finder points of a qr-code
//                    // the 4th point is reconstructed, forming a square in 3d space
//                    pt1 = new Point((int) pts[0].getX(), (int) pts[0].getY());
//                    pt2 = new Point((int) pts[1].getX(), (int) pts[1].getY());
//                    pt3 = new Point((int) pts[2].getX(), (int) pts[2].getY());
//
//                    Point rightVector = new Point(pt3.x - pt2.x, pt3.y - pt2.y);
//                    pt4 = new Point(pt1.x + rightVector.x, pt1.y + rightVector.y); // bottom right
//                }
//
//                job.result.setDirectly(pt1, pt2, pt3, pt4);

                // confidence values [0,1]
                job.result.confidence = 1;
                job.result.value = result.getText();
                job.result.format = BarcodeResult.Format.QRCode;
                job.result.timestamp = What.timey();
            }

        } catch (NotFoundException err) {
            // expected failure case when zxing-lib doesn't locate a qr-code. don't log.
        } catch (Exception err) {
            // unexpected failure. worth noting.
            Log.exception(err, "Error detecting QR code using ZXing");
        }

        job.decodeCompletedAt = What.timey();
    }
}
