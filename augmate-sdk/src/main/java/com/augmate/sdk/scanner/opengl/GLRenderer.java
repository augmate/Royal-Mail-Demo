package com.augmate.sdk.scanner.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.augmate.sdk.logger.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Simple OpenGL camera textured-quad renderer
 */
public class GLRenderer implements GLSurfaceView.Renderer {
    private TexturedQuad outputQuad;
    private SurfaceTexture surfaceTexture;
    private int cameraTextureId;
    private IGLReady onReady;

    public interface IGLReady {
        public void onRendererReady();
    }

    public GLRenderer(IGLReady onReady) {
        super();
        this.onReady = onReady;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    public void setCameraTextureId(int id) {
        this.cameraTextureId = id;
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        checkGlError("glCompileShader");

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if(compiled[0] == 0) {
            Log.error("Error compiling shader: %s", GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        } else {
            Log.debug("Shader compiled OK");
        }

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error = GLES20.glGetError();
        if (error == GLES20.GL_NO_ERROR)
            return;

        Log.error("Hit OpenGL error (code=%d) while: %s", error, glOperation);
        throw new RuntimeException(glOperation + ": glError " + error);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.9f, 0.7f, 0.2f, 1.0f);
        Log.debug("Loading resources..");
        outputQuad = new TexturedQuad();

        onReady.onRendererReady();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        outputQuad.draw(cameraTextureId);
        surfaceTexture.updateTexImage();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.debug("Surface changed to: %d x %d", width, height);
        GLES20.glViewport(0, 0, width, height);
    }
}