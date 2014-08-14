package com.augmate.sdk.scanner;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.logger.Timer;

import java.io.IOException;

class CameraController {

    Camera camera;
    private byte[][] frameCaptureBuffers;
    private int lastCaptureBufferIdx;

    /**
     * Configures camera around a rendering context
     * Ensures optimal configuration for capturing barcodes
     *
     * @param width
     * @param height
     * @param surfaceHolder
     */
    public void beginFrameCapture(SurfaceHolder surfaceHolder, Camera.PreviewCallback callback, int width, int height) {
        assert (surfaceHolder != null);
        assert (camera == null);

        int numOfCameras = Camera.getNumberOfCameras();
        Log.debug("There are %d cameras available", numOfCameras);

        assert (numOfCameras > 0);

        int bestCameraIdx = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.debug("  Camera #%d facing=%d orientation=%d", i, cameraInfo.facing, cameraInfo.orientation);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bestCameraIdx = i;
            }
        }

        Timer cameraTimer = Log.startTimer("Opening camera took %d msec");

        // try to open the first available camera
        try {
            camera = Camera.open(bestCameraIdx);
            camera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int i, Camera camera) {
                    // i've seen error-code 100 here. sometimes camera enters a broken state
                    // in which no camera-app can work. restarting emulator/device fixes it.
                    Log.debug("Camera had an error: %d", i);
                }
            });
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            Log.exception(e, "Failed to open camera interface");
        }

        cameraTimer.stop();

        Camera.Parameters params = createCameraConfigurationParameters(camera, width, height);

        camera.setParameters(params);

        width = params.getPreviewSize().width;
        height = params.getPreviewSize().height;

        Log.debug("Camera accepted frame size: %d x %d", width, height);

        lastCaptureBufferIdx = 0;
        frameCaptureBuffers = new byte[2][width * height * 3]; // two frame buffers
        camera.addCallbackBuffer(frameCaptureBuffers[lastCaptureBufferIdx]); // start with the first buffer
        camera.setPreviewCallbackWithBuffer(callback);

        try {
            camera.startPreview();
        } catch (Exception err) {
            // FIXME: it's possible to get the emulator and glass into a state where the camera stops responding
            //        probably a problem with the underlying Camera HAL. restarting fixes it.
            Log.exception(err, "Failed to start camera frame feed!");
        }

        // zooming is back on Glass after XE16!
        camera.startSmoothZoom(14);

        Log.debug("Started camera frame-grabbing.");
    }

    /**
     * safe to call multiple times
     */
    public void endFrameCapture() {
        if (camera != null) {
            Log.debug("Stopping camera frame-grabbing");
            camera.setPreviewCallbackWithBuffer(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void changeFrameBuffer() {
        lastCaptureBufferIdx = ++lastCaptureBufferIdx % 2;
    }

    public void requestAnotherFrame() {
        camera.addCallbackBuffer(frameCaptureBuffers[lastCaptureBufferIdx]);
    }


    private Camera.Parameters createCameraConfigurationParameters(Camera mCamera, int previewFrameWidth, int previewFrameHeight) {
        // configure camera
        Camera.Parameters params = mCamera.getParameters();

        Log.debug("Hardware build: " + Build.PRODUCT + " / " + Build.DEVICE + " / " + Build.MODEL + " / " + Build.BRAND);

        Log.debug("Current camera params: " + params.flatten());

        String deviceManufacturerName = params.get("exif-make");
        if (deviceManufacturerName == null)
            deviceManufacturerName = "Unknown";

        Log.debug("  deviceManufacturerName = [" + deviceManufacturerName + "]");

        switch (deviceManufacturerName) {
            case "Vuzix":
                Log.debug("Optimizing for Vuzix");

                // glass tweaks
                params.setAutoExposureLock(true);
                params.setAutoWhiteBalanceLock(true);
                params.setExposureCompensation(0);
                params.setVideoStabilization(true);

                params.setPreviewFpsRange(27000, 27000);
                params.set("iso", "800");
                params.set("scene-mode", "barcode");

                // vuzix tweaks
                params.setPreviewSize(previewFrameWidth, previewFrameHeight);
                break;
            case "Epson":
                Log.debug("Optimizing for Epson Moverio");

                params.set("auto-exposure-lock", "false");
                params.set("manual-exposure", 0);
                params.set("contrast", 80);
                params.set("iso-mode-values", 100);
                params.set("zoom", 2);
                params.set("scene-mode-values", "barcode");
                break;
            case "Google":
                Log.debug("Optimizing for Google Glass");

                //params.set("manual-exposure", 2);
                //params.set("mode", "high-performance");
                //params.setExposureCompensation(50);
                //params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
                //params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                //params.setAutoWhiteBalanceLock(true);
                //params.setRecordingHint(true);
                //params.setVideoStabilization(true);

                params.set("iso", 800);
                params.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
                params.setPreviewFormat(ImageFormat.NV21);
                params.setPreviewFpsRange(30000, 30000);
                params.setPreviewSize(previewFrameWidth, previewFrameHeight);
                break;
            case "Emulator":
                Log.debug("Optimizing for known emulator");

                params.setAutoExposureLock(false);
                params.setAutoWhiteBalanceLock(false);

                //params.set("manual-exposure", 0);
                //params.set("contrast", 80);
                //params.set("zoom", 10);
                //params.set("video-stabilization", 80);
                //params.set("whitebalance", "warm-fluorescent");

                params.setPreviewFormat(ImageFormat.NV21);
                params.setPreviewFpsRange(30000, 30000);
                params.setPreviewSize(previewFrameWidth, previewFrameHeight);

                break;
            default:
                Log.debug("Optimizing for an unrecognized device (keeping it generic)");

                params.setAutoWhiteBalanceLock(false);
                params.setAutoExposureLock(false);
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                params.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
                params.setPreviewFormat(ImageFormat.NV21);
                params.setPreviewSize(previewFrameWidth, previewFrameHeight);
                break;
        }

        Log.debug("Proposing new camera preview size: " + params.getPreviewSize().width + " x " + params.getPreviewSize().height);
        return params;
    }
}
