package com.pervasive.sth.entities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder _holder;
    private Camera _camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        _camera = camera;
        //_camera.setDisplayOrientation(90);
        _holder = getHolder();
        _holder.addCallback(this);

        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public Camera getCamera() {
        return _camera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            _camera.setPreviewDisplay(holder);
            _camera.startPreview();
        } catch ( IOException e ) {
            Log.w(this.getClass().getName(), "Error setting camera preview " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if ( _holder.getSurface() == null )
            return;
        try {
            _camera.stopPreview();
        } catch ( Exception e ) { }

        try {
            _camera.setPreviewDisplay(_holder);
            _camera.startPreview();
        } catch ( Exception e ) {
            Log.w(this.getClass().getName(), "Error starting camera preview " + e.getMessage());
        }
    }
}