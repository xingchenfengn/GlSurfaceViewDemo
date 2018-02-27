package com.xingzhiqiao.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xingzhiqiao on 2017/8/3.
 */

public class GlWallpaperRender implements GLSurfaceView.Renderer {

    private String TAG = GlWallpaperRender.class.getName();
    private Context context;
    private GLBitmap rotateBitmap;
    private GLBitmap bgBitmap;
    //背景图的bitmap
    private int width = 0;
    private int height = 0;
    private int halfHeight = 0;
    private int halfWidth = 0;
    public static final int ORIGIN_ROTATE_RADIUS = 4;
    public static final int SPEED_UP = 1001;
    //点击减速
    public static final int CLICK_TO_SLOW = 1002;
    public static final int VISIBILE_CHANGED = 1003;
    //减速延迟时间
    public static final int SPEED_DELAY_TIME = 2000;
    //每两次执行onDrawFrame操作的标准时间
    public static final long Standard_Time = 16;
    private long dt;
    private long endTime;
    private long startTime;
    private int rotateRadius;
    //逆时针加速
    private boolean haveReverseSpeed;


    private int phoneWidth, phoneHeight;//手机屏幕宽高
    private int roateImgWith;
    private int MAX_TEXTURE_SIZE = 0;
    //是否有虚拟键
    private boolean hasVirtualkey;
    private int statusBarHeigh;

    public GlWallpaperRender(Context context) {
        this.context = context;
        rotateBitmap = new GLBitmap();
        bgBitmap = new GLBitmap();
        if (phoneWidth == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            phoneWidth = dm.widthPixels;
        }
        if (phoneHeight == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            phoneHeight = dm.heightPixels;
        }
        roateImgWith = Math.min(phoneHeight, phoneWidth) * 82 / 100;
        rotateRadius = ORIGIN_ROTATE_RADIUS;
        hasVirtualkey = ToolUtils.checkDeviceHasNavigationBar(context);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int delayTime;
            switch (msg.what) {
                case SPEED_UP://自动减速
                    delayTime = 500;
                    if (rotateRadius > ORIGIN_ROTATE_RADIUS) {
                        rotateRadius--;
                    } else if (rotateRadius < -ORIGIN_ROTATE_RADIUS) {
                        rotateRadius++;
                    }
                    if (Math.abs(rotateRadius) > ORIGIN_ROTATE_RADIUS) {
                        handler.sendEmptyMessageDelayed(SPEED_UP, delayTime);
                    }
                    resetState();
                    break;
                case CLICK_TO_SLOW://点击减速
                    delayTime = 10;
                    if (rotateRadius > ORIGIN_ROTATE_RADIUS) {
                        rotateRadius = rotateRadius - 5;
                    } else if (rotateRadius < -ORIGIN_ROTATE_RADIUS) {
                        rotateRadius = rotateRadius + 5;
                    }
                    if (Math.abs(rotateRadius) > ORIGIN_ROTATE_RADIUS) {
                        handler.sendEmptyMessageDelayed(CLICK_TO_SLOW, delayTime);
                    }
                    resetState();
                    break;
                case VISIBILE_CHANGED://视图状态改变
                    isVisible = false;
                    break;

            }
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        statusBarHeigh = ToolUtils.getStatusBarHeight(context);
        rotateBitmap.loadGLTexture(gl, context, R.mipmap.spinner);
        bgBitmap.loadGLTexture(gl, context, R.mipmap.bg);
        int[] maxTextureSize = new int[1];
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        MAX_TEXTURE_SIZE = maxTextureSize[0];
        if (MAX_TEXTURE_SIZE > 2048) {
            MAX_TEXTURE_SIZE = 4096;
        }
        // Enable Texture Mapping ( NEW )
        gl.glEnable(GL10.GL_TEXTURE_2D);
        // Enable Smooth Shading
        gl.glShadeModel(GL10.GL_SMOOTH);
        // Black Background
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        // Depth Buffer Setup
        gl.glClearDepthf(1.0f);
        // Enables Depth Testing
        gl.glEnable(GL10.GL_DEPTH_TEST);
        // The Type Of Depth Testing To Do
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        endTime = System.currentTimeMillis();
//        if (!isVisible) {
//            return;
//        }
        dt = endTime - startTime;
        if (dt < Standard_Time) {
            try {
                Thread.sleep(Standard_Time - dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startTime = System.currentTimeMillis();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(0x4100);
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        //虚拟键高度
        int virtualkeyHeight = 0;
        //判断是否有虚拟键
        if (hasVirtualkey) {
            virtualkeyHeight = statusBarHeigh * 2;
        }
        bgBitmap.draw(gl, false, 0, phoneWidth, phoneHeight + virtualkeyHeight);//绘制背景
        radius -= rotateRadius;
        rotateBitmap.draw(gl, true, radius, phoneWidth, phoneHeight + virtualkeyHeight);
    }

    private int radius = 0;


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        this.halfWidth = width / 2;
        this.halfHeight = height / 2;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
    }

    private float startX = 0;
    private float startY = 0;

    public void onCustomTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                startY = motionEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltax = motionEvent.getX() - startX;
                float y = motionEvent.getY();
                if (y <= halfHeight){//在屏幕上半部
                    if (deltax > 50) {//向左滑动
                        speedUp(true);
                    } else if (deltax < -50) {//向右滑动
                        speedUp(false);
                    }
                }else {//屏幕下半部，相反方向
                    if (deltax > 50) {//向左滑动
                        speedUp(false);
                    } else if (deltax < -50) {//向右滑动
                        speedUp(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                float stopX = motionEvent.getX();
                float stopY = motionEvent.getY();
                int xleft = phoneWidth / 2 - roateImgWith / 2;
                int xRigth = phoneWidth / 2 + roateImgWith / 2;
                int yTop = phoneHeight / 2 - roateImgWith / 2;
                int yBottom = phoneHeight / 2 + roateImgWith / 2;

                if (Math.abs(stopX - startX) <= 10 && Math.abs(stopY - startY) <= 10) {//点击事件
                    if (stopX >= xleft && stopX <= xRigth && stopY >= yTop && stopY <= yBottom) {
                        if (Math.abs(rotateRadius) != ORIGIN_ROTATE_RADIUS) {
                            handler.removeMessages(SPEED_UP);
                            handler.removeMessages(CLICK_TO_SLOW);
                            handler.sendEmptyMessage(CLICK_TO_SLOW);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 重置旋转状态
     */
    private void resetState() {
        if (Math.abs(rotateRadius) <= ORIGIN_ROTATE_RADIUS) {//减速动画完成后，重置状态
            if (haveReverseSpeed) {
                rotateRadius = -ORIGIN_ROTATE_RADIUS;
            } else {
                rotateRadius = ORIGIN_ROTATE_RADIUS;
            }
            haveReverseSpeed = false;
        }
    }

    /**
     * 加速
     *
     * @param clockwise 顺时针方向
     */
    public void speedUp(boolean clockwise) {
        handler.removeMessages(SPEED_UP);
        handler.removeMessages(CLICK_TO_SLOW);
        if (clockwise) {
            rotateRadius = ORIGIN_ROTATE_RADIUS * 3;
            haveReverseSpeed = false;
            handler.sendEmptyMessageDelayed(SPEED_UP, SPEED_DELAY_TIME);
        } else {
            handler.removeMessages(SPEED_UP);
            rotateRadius = -ORIGIN_ROTATE_RADIUS * 3;
            haveReverseSpeed = true;
            handler.sendEmptyMessageDelayed(SPEED_UP, SPEED_DELAY_TIME);
        }
    }

    private boolean isVisible;

//    @Override
//    public void onCustomVisibilityChanged(boolean visible) {
//        handler.removeMessages(VISIBILE_CHANGED);
//        if (visible) {
//            isVisible = visible;
//        } else {//2s后重置isVisible，解决退出界面时视觉卡动
//            handler.sendEmptyMessageDelayed(VISIBILE_CHANGED, 2000);
//        }
//    }
}
