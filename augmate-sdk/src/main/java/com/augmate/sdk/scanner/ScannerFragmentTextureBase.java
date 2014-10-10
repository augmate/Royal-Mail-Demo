package com.augmate.sdk.scanner;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import com.augmate.sdk.R;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.What;
import com.augmate.sdk.scanner.decoder.DecodingJob;

public abstract class ScannerFragmentTextureBase extends Fragment implements TextureView.SurfaceTextureListener {
    private CameraSettings frameBufferSettings = new CameraSettings(720, 480);
    private CameraController cameraController = new CameraController();
    private boolean isProcessingCapturedFrames;
    private IScannerResultListener mListener;
    private boolean readyForNextFrame = true;
    private ScannerVisualDebugger dbgVisualizer;
    private DecoderThread decoderThread;
    private TextureView textureView;
    private int framesSkipped = 0;

    /**
     * Must call this method from a place like onCreateView() for the scanner to work
     *
     * @param textureView           TextureView is required
     * @param scannerVisualDebugger ScannerVisualDebugger is optional
     */
    public void setupScannerActivity(TextureView textureView, ScannerVisualDebugger scannerVisualDebugger) {
        //Log.debug("Scanner fragment configured.");
        this.textureView = textureView;
        this.dbgVisualizer = scannerVisualDebugger;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (textureView == null) {
            Log.error("textureView is null. Must setupScannerActivity() with a valid TextureView in onCreateView().");
            return;
        }

        isProcessingCapturedFrames = true;
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IScannerResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnScannerResultListener");
        }

        // decoding thread lives so long as the fragment is attached to an activity
        // when paused, we simply don't send anything to it, and the thread idles, taking up no real resources
        startDecodingThread();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        shutdownDecodingThread();
    }

    private void startDecodingThread() {
        if (decoderThread == null) {
            // spawn a decoding thread and connect it to our message pump
            decoderThread = new DecoderThread(new ScannerFragmentMessages(this));
            decoderThread.start();
        }
    }

    private void shutdownDecodingThread() {

        if (decoderThread != null) {
            // shutdown
            Log.debug("Asking message pump to exit..");
            decoderThread.getMessagePump()
                    .obtainMessage(R.id.decodingThreadShutdown)
                    .sendToTarget();

            Log.debug("Waiting on decoding-thread to exit (timeout: 5s)");
            try {
                decoderThread.join(5000);
            } catch (InterruptedException e) {
                Log.exception(e, "Interrupted while waiting on decoding thread");
            }

            decoderThread = null;
            Log.debug("Decoding-thread has been shutdown.");
        }
    }

    private void onJobCompleted(DecodingJob job) {
        Log.debug("Job stats: skipped frames=%d, binarization=%d msec, total=%d msec", framesSkipped, job.binarizationDuration(), job.totalDuration());

        if (job.result != null && job.result.confidence > 0) {
            Point[] pts = job.result.corners;
            Log.info("  Result={%s} with confidence=%.2f", job.result.value, job.result.confidence);

            if (dbgVisualizer != null) {
                dbgVisualizer.setPoints(pts);
                dbgVisualizer.setBarcodeValue(job.result.value);
            }

            if (mListener != null) {
                mListener.onBarcodeScanSuccess(job.result.value);
            }
        }

        // tell debugger they can use the buffer we wrote decoding debug data to
        if (dbgVisualizer != null)
            dbgVisualizer.flipDebugBuffer();
        // tell frame-grabber we can push next (or most recently grabbed) frame to the decoder
        readyForNextFrame = true;

        // TODO: try pushing next frame (if we got one) from here
        // may reduce delays by length of one frame (ie 50ms at 20fps)
    }

    public final class ScannerFragmentMessages extends Handler {
        private ScannerFragmentTextureBase fragment;

        ScannerFragmentMessages(ScannerFragmentTextureBase fragment) {
            this.fragment = fragment;
            Log.debug("Msg Pump created on thread=%d", Thread.currentThread().getId());
        }

        @Override
        public void handleMessage(Message msg) {
            //Log.debug("On thread=%d got msg=%d", Thread.currentThread().getId(), msg.what);
            if (msg.what == R.id.scannerFragmentJobCompleted) {
                fragment.onJobCompleted((DecodingJob) msg.obj);
            }
        }
    }

    //Texture Listening
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (dbgVisualizer != null)
            dbgVisualizer.setFrameBufferSettings(frameBufferSettings.width, frameBufferSettings.height);

        cameraController.endFrameCapture();
        cameraController.beginFrameCapture(null, null, frameBufferSettings.width, frameBufferSettings.height, surface);
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        cameraController.endFrameCapture();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        if (!isProcessingCapturedFrames) {
            Log.debug("Ignoring new frame because we are paused");
            return;
        }


        if (readyForNextFrame) {
            // kick-off frame decoding in a dedicated thread

            //byte[] bytes = byteBuffer.array();

            long start = What.timey();

            Bitmap bitmap = textureView.getBitmap(frameBufferSettings.width, frameBufferSettings.height);
            long fetchBitmap = What.timey();
            Log.debug("textureview bitmap fetch = %d ms", fetchBitmap - start);

            byte[] bytes = getNV21(frameBufferSettings.width, frameBufferSettings.height,bitmap);
            long convertBitmap = What.timey();
            Log.debug("textureview bitmap convert = %d ms", convertBitmap - fetchBitmap);

            DecodingJob job = new DecodingJob(frameBufferSettings.width, frameBufferSettings.height,
                    bytes, dbgVisualizer != null ? dbgVisualizer.getNextDebugBuffer() : null);

            decoderThread.getMessagePump().obtainMessage(R.id.decodingThreadNewJob, job).sendToTarget();

            // change capture-buffer to prevent camera from modifying buffer sent to decoder
            // this avoids copying buffers on every frame
            //cameraController.changeFrameBuffer();
            readyForNextFrame = false;
            framesSkipped = 0;
        } else {
            framesSkipped++;
        }
    }

    //http://stackoverflow.com/questions/5960247/convert-bitmap-array-to-yuv-ycbcr-nv21
    byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height * width; j++) {

            R = (argb[index] & 0xff0000) >> 16;
            G = (argb[index] & 0xff00) >> 8;
            B = (argb[index] & 0xff) >> 0;

            // well known RGB to YUV algorithm
            Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;

            yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

            index++;
        }
    }

}
