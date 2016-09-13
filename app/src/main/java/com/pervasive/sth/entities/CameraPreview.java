package com.pervasive.sth.entities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 *	@brief This class implements the camera functionalities
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private final String LOG_TAG = CameraPreview.class.getName();
	/*
	 * Allows you to control the surface size and format
	 */
	private SurfaceHolder _holder;

	/*
	 * The android camera handler
	 */
	private Camera _camera;

	public Camera.Size getMinSize() {
		return minSize;
	}

	private Camera.Size minSize;
	/**
	 *
	 * @param context
	 * @param camera
	 * @brief initialize class fields
	 */
	public CameraPreview(Context context, Camera camera) {
		super(context);
		_camera = camera;
		_holder = getHolder();
		_holder.addCallback(this);
		_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Camera.Parameters params = _camera.getParameters();
		List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();

		minSize = pictureSizes.get(0);
		for ( Camera.Size size : pictureSizes ) {
			Log.i(LOG_TAG, "Available resolution: " + size.width + " " + size.height);
			if ( size.width * size.height < minSize.width * minSize.height )
				minSize = size;
		}

		Log.i(LOG_TAG, "Chosen resolution " + minSize.width + "x" + minSize.height);
		params.setPictureSize(minSize.width, minSize.height);
		_camera.setParameters(params);
	}

	public Camera getCamera() {
		return _camera;
	}

	/**
	 * @brief release the camera handler
	 */
	public void releaseCamera() {
		if (_camera != null)
			_camera.release();
	}

	/**
	 *
	 * @param holder
	 * @brief creates the photo surface preview
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			_camera.setPreviewDisplay(holder);
			_camera.startPreview();
		} catch (IOException e) {
			Log.w(LOG_TAG, "Error setting camera preview " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	/**
	 *
	 * @param holder
	 * @param format
	 * @param width
	 * @param height
	 * @brief updates the photo surface preview
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if ( _holder.getSurface() == null )
			return;
		try {
			_camera.stopPreview();
			_camera.setPreviewDisplay(_holder);
			_camera.startPreview();
		} catch (Exception e) {
			Log.w(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
}