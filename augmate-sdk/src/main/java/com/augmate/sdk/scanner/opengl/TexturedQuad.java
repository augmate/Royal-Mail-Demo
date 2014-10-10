package com.augmate.sdk.scanner.opengl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import com.augmate.sdk.logger.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TexturedQuad {

    private final static String vertexShaderCode =
            "attribute vec2 inPosition;\n" +
            "attribute vec2 inTexCoord;\n" +
            "varying vec2 texCoord;\n" +
            "void main()\n" +
            "{" +
            "   texCoord = inTexCoord;\n" +
            "   gl_Position = vec4(inPosition.x, inPosition.y, 0, 1);\n" +
            "}";

    private final static String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 texCoord;\n" +
            "uniform samplerExternalOES texSampler;\n" +
            "void main() {\n" +
            "   gl_FragColor.rgb = texture2D(texSampler, texCoord).rgb;\n" +
            "   gl_FragColor.a = 1;\n" +
            "}";

    private final static float vertices[] = {
            -1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f,
             1.0f, -1.0f
    };

    private final static float textureCoords[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    private final static short indices[] = {0, 1, 3, 3, 2, 1};

    private final int shaderId;
    private int vertexPositionAttribute;
    private int texCoordAttribute;

    private FloatBuffer vertexBuffer, texCoordBuffer;
    private ShortBuffer indexBuffer;

    public TexturedQuad() {
        Log.debug("Setting up texture rendering square..");

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(textureCoords);
        texCoordBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        GLRenderer.checkGlError("loadShader vertex");
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        GLRenderer.checkGlError("loadShader fragment");

        shaderId = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderId, vertexShader);
        GLES20.glAttachShader(shaderId, fragmentShader);
        GLES20.glLinkProgram(shaderId);
        GLRenderer.checkGlError("glLinkProgram");
        vertexPositionAttribute = GLES20.glGetAttribLocation(shaderId, "inPosition");
        texCoordAttribute = GLES20.glGetAttribLocation(shaderId, "inTexCoord");
    }

    public void draw(int textureId) {
        GLES20.glUseProgram(shaderId);
        GLRenderer.checkGlError("glUseProgram");

        GLES20.glEnableVertexAttribArray(vertexPositionAttribute);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);
        GLRenderer.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(vertexPositionAttribute, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        GLES20.glVertexAttribPointer(texCoordAttribute, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer);
        GLRenderer.checkGlError("glVertexAttribPointer");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(shaderId, "texSample"), 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        GLRenderer.checkGlError("glDrawElements");

        GLES20.glDisableVertexAttribArray(vertexPositionAttribute);
        GLES20.glDisableVertexAttribArray(texCoordAttribute);
    }

}