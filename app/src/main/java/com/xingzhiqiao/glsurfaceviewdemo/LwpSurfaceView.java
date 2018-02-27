package com.xingzhiqiao.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * 为Locker提供的Surface
 * Created by xingzhiqiao on 2017/8/4.
 */

public class LwpSurfaceView extends GLSurfaceView {

    private GlWallpaperRender renderer;
    private Context mContext;

    public LwpSurfaceView(Context context) {
        super(context);
        mContext = context;
        renderer = new GlWallpaperRender(mContext);
        setRenderer(renderer);
    }

    public LwpSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        renderer = new GlWallpaperRender(mContext);
        setRenderer(renderer);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
//        renderer.onCustomVisibilityChanged(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
//        renderer.onCustomVisibilityChanged(false);
    }
}
