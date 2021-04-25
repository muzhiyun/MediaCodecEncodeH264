package com.example.mediacodecencode.util;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.mediacodecencode.YUVPlayerActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class YuvPlayer extends GLSurfaceView implements Runnable,  SurfaceHolder.Callback, GLSurfaceView.Renderer {

   // private final static String PATH = "/sdcard/sintel_640_360.yuv";
   // public final static String PATH = "/sdcard/test_NV21.yuv";

    public YuvPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e("YUvPlayer","context= "+context + " attrs= " + attrs);
        setRenderer(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    }

    @Override
    public void run() {
     startPlay();
    }
    public void startPlay(){

     loadYuv(YUVPlayerActivity.path,getHolder().getSurface());
    }

    public native void loadYuv(String url, Object surface);

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
