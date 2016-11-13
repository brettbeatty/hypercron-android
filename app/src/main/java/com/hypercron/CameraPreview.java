package com.hypercron;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by brett on 11/11/16.
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private WindowManager windowManager;

    public CameraPreview(Context context, Camera camera, WindowManager windowManager) {
        super(context);
        this.camera = camera;
        this.windowManager = windowManager;

        if (camera != null) camera.setDisplayOrientation(90);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {

        if (camera == null) return;

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch(IOException e) {
            Log.e("camera", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (holder.getSurface() == null || camera == null) return;

        try {
            camera.stopPreview();
            switch (windowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    camera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    camera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    camera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    camera.setDisplayOrientation(180);
                    break;
            }
        } catch (Exception e) {
            //  ignore: tried to stop a non-existent preview
            Log.e("debug", e.getMessage());
        }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("camera", "Error setting camera preview: " + e.getMessage());
        }
    }
}
