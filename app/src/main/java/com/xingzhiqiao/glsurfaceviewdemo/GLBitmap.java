package com.xingzhiqiao.glsurfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * 绘制GlBitmap
 * Created by xingzhiqiao on 2017/8/3.
 */

class GLBitmap {
    private FloatBuffer textureBuffer; // buffer holding the texture coordinates
    public int height;
    private int widget;
    private float texture[] = {
            // Mapping coordinates for the vertices
            0.0f, 1.0f, // top left (V2)
            0.0f, 0.0f, // bottom left (V1)
            1.0f, 1.0f, // top right (V4)
            1.0f, 0.0f // bottom right (V3)
    };

    private FloatBuffer vertexBuffer; // buffer holding the vertices

    private float vertices[] = {-1.0f, -1.0f, 0.0f, // V1 - bottom left
            -1.0f, 1.0f, 0.0f, // V2 - top left
            1.0f, -1.0f, 0.0f, // V3 - bottom right
            1.0f, 1.0f, 0.0f // V4 - top right
    };
    private int mTextureId;

    public GLBitmap() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    /**
     * The texture pointer
     */
    private int[] textures = new int[1];

    public void loadGLTexture(GL10 gl, Context context, int bitmapId) {
        Bitmap b = loadBitmap(context, bitmapId);
        textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureId = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, b, 0);
        int error = GLES20.glGetError();
        if (error != 0) {
            Log.e("GLBitmap", "Error loading GL texture. OpenGL code: " + error);
        }
        b.recycle();
    }

    /**
     * 加载Bitmap的方法，
     * 用来从res中加载Bitmap资源
     */
    private Bitmap loadBitmap(Context context, int resourceId) {
        InputStream is = context.getResources().openRawResource(resourceId);
        Bitmap bitmap = null;
        try {
            // 利用BitmapFactory生成Bitmap
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                // 关闭流
                is.close();
                is = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return bitmap;

    }


    public void draw(GL10 gl, boolean isRotate, int radius, int width, int height) {
        // bind the previously generated texture
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);
        if (isRotate) {
            int roateImgWith = Math.min(height, width) * 82 / 100;

            gl.glViewport(width / 2 - roateImgWith / 2, height / 2 - roateImgWith / 2, roateImgWith, roateImgWith);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glRotatef((radius) % 360, 0, 0, 1);  //绘制旋转图
        } else {
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
        }
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Set the face rotation
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public void unloadTexture(GL10 p1) {
        if (textures != null) {
            p1.glDeleteTextures(0x1, textures, 0x0);
        }
    }
}
