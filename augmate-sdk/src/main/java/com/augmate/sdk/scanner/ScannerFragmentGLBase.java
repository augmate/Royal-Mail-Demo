package com.augmate.sdk.scanner;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.opengl.GLRenderer;

import javax.microedition.khronos.opengles.GL10;

/**
 * this is a very rough start. it definitely doesn't clean up after itself properly.
 */
public abstract class ScannerFragmentGLBase extends Fragment implements SurfaceTexture.OnFrameAvailableListener, GLRenderer.IGLReady {
    private CameraSettings frameBufferSettings = new CameraSettings(1280, 720);
    private CameraController cameraController = new CameraController();
    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void setupScannerActivity(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        glRenderer = new GLRenderer(this);
        glSurfaceView.setRenderer(glRenderer);
    }

    @Override
    public void onRendererReady() {
        Log.debug("OpenGL Renderer is ready; setting up camera feed..");
        int glTextureId = createOpenGLTextureId();
        SurfaceTexture glSurfaceTexture = new SurfaceTexture(glTextureId);
        glSurfaceTexture.setOnFrameAvailableListener(this);

        glRenderer.setSurfaceTexture(glSurfaceTexture);
        glRenderer.setCameraTextureId(glTextureId);

        cameraController.beginFrameCapture(null, null, frameBufferSettings.width, frameBufferSettings.height, glSurfaceTexture);
    }

    /**
     * new frame arrived from the camera, tell renderer to repaint
     * @param surfaceTexture camera texture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    // allocate an opengl texture and configure filtering
    private int createOpenGLTextureId()
    {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    @Override
    public void onDetach() {
        cameraController.endFrameCapture();
        super.onDetach();
    }
}
