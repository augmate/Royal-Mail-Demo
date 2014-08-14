package com.augmate.sdk.scanner.decoder;

import android.graphics.Point;

/**
 * Generic barcode decoding result
 */
public class BarcodeResult {
    public BarcodeResult() {
        for (int i = 0; i < 4; i++)
            corners[i] = new Point(0, 0);
    }

    // 4 points representing an unaligned bounding box
    public Point[] corners = new Point[4];

    // normalized confidence for various scanners
    public float confidence = 0; // 0 = nothing, 0.5 = that's probably a barcode, 1 = found and decoded

    // extracted value
    public String value = null;

    public Format format = Format.UNKNOWN;

    public void setDirectly(Point pt1, Point pt2, Point pt3, Point pt4) {
        corners[0] = pt1;
        corners[1] = pt2;
        corners[2] = pt3;
        corners[3] = pt4;
    }

    public void setAABB(int x, int y, int width, int height) {
        corners[0].x = x - width / 2;
        corners[0].y = y - height / 2;

        corners[1].x = x + width / 2;
        corners[1].y = y - height / 2;

        corners[2].x = x + width / 2;
        corners[2].y = y + height / 2;

        corners[3].x = x - width / 2;
        corners[3].y = y + height / 2;
    }

    public long timestamp = 0;

    public enum Format {
        UNKNOWN, Code_39, Code_128, QRCode, MaxiCode
    }
}
