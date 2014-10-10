package com.augmate.sdk.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import com.augmate.sdk.logger.What;

public class ScannerVisualDebugger extends View {
    public int rawImgWidth = 0;
    public int rawImgHeight = 0;
    public int[][] debugImg = null;
    public int currentDebugBufferIdx = 0;

    // bounding box corners
    private Point[] boxCorners;
    private Paint boxLineColor;
    private Paint boxPointColor;
    private float scaleX;
    private float scaleY;
    private String barcodeValue;
    private float barcodeConfidence = 1;
    private long lastFrameRendered = 0;
    private Paint textShadowColor;

    public ScannerVisualDebugger(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScannerVisualDebugger(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScannerVisualDebugger(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        preparePaints();
    }

    private void preparePaints() {
        boxLineColor = new Paint();
        boxLineColor.setColor(0xEE22AA99);
        boxLineColor.setStrokeWidth(2.0f);

        boxPointColor = new Paint();
        boxPointColor.setColor(0xAA22AA99);
        boxPointColor.setStrokeWidth(5.0f);

        textShadowColor = new Paint();
        textShadowColor.setAntiAlias(true);
        textShadowColor.setShadowLayer(2.0f, 0, 0.0f, Color.BLACK);
        textShadowColor.setColor(Color.WHITE);
        textShadowColor.setStrokeWidth(4.0f);
        textShadowColor.setTextSize(14.0f);
    }

    /**
     * Provides a safe-to-write buffer rgba buffer
     * Meanwhile the other buffer can now be used for reading and drawing-to-screen
     *
     * @return int[] reference to the next buffer
     */
    public int[] getNextDebugBuffer() {
        return debugImg[(currentDebugBufferIdx + 1) % 2];
    }

    /**
     * Switch current buffer
     * Current buffer is read from
     * Next buffer is written to
     * This avoids overwriting a buffer while it's being drawn out to screen
     */
    public void flipDebugBuffer() {
        currentDebugBufferIdx = (currentDebugBufferIdx + 1) % 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = What.timey();
        long frameLength = now - lastFrameRendered;
        lastFrameRendered = now;
        //float frameTicks = (float) frameLength / 16.0f; // assume 60hz
        float frameScale = (float) frameLength / 1000.0f;

        scaleX = (float) canvas.getWidth() / (float) rawImgWidth;
        scaleY = (float) canvas.getHeight() / (float) rawImgHeight;

        boxLineColor.setAlpha((int) (barcodeConfidence * barcodeConfidence * 255));
        boxPointColor.setAlpha((int) (barcodeConfidence * 255));

        if (debugImg != null) {
            canvas.drawBitmap(debugImg[currentDebugBufferIdx], 0, rawImgWidth, 0, 0, rawImgWidth, rawImgHeight, true, null);
        }

        if (boxCorners != null && boxCorners.length == 4) {

            for (int i = 0; i < 4; i++) {
                drawLine(canvas, boxLineColor, boxCorners[i], boxCorners[(i + 1) % 4]);
            }

            for (int i = 0; i < 4; i++) {
                canvas.drawCircle(boxCorners[i].x, boxCorners[i].y, 4, boxPointColor);
            }

            int minX = Math.min(Math.min(boxCorners[0].x, boxCorners[2].x), Math.min(boxCorners[1].x, boxCorners[3].x));
            int minY = Math.min(Math.min(boxCorners[0].y, boxCorners[2].y), Math.min(boxCorners[1].y, boxCorners[3].y));

            canvas.drawText("QR Code: [" + barcodeValue + "]", minX - 20, minY - 20, textShadowColor);
        }

        barcodeConfidence = Math.max(0, barcodeConfidence - 3.0f * frameScale);

        postInvalidate();
    }

    private void drawPoint(Canvas canvas, Paint paint, Point pt) {
        canvas.drawPoint(pt.x, pt.y, paint);
    }

    private void drawLine(Canvas canvas, Paint paint, Point a, Point b) {
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
    }

    /**
     * barcode bounding box
     *
     * @param pts 4 corner points
     */
    public void setPoints(Point[] pts) {
        boxCorners = pts.clone();
        for (Point pt : boxCorners) {
            pt.x *= scaleX;
            pt.y *= scaleY;
        }
        barcodeConfidence = 1;
    }

    /**
     * reallocates rgba debug output buffer that scanner paints unto
     *
     * @param width
     * @param height
     */
    public void setFrameBufferSettings(int width, int height) {
        debugImg = new int[2][width * height];
        rawImgWidth = width;
        rawImgHeight = height;
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = barcodeValue;
    }
}
