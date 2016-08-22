package com.pervasive.sth.tasks;

import android.content.Context;


import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.Media;
import com.pervasive.sth.entities.CameraPreview;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;
import com.pervasive.sth.network.WSInterface;
import com.sun.jna.platform.win32.WinNT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @brief This class implements the asynchronous task for treasure medias
 */
public class TreasureMediaTask extends AsyncTask<Void, Void, Void> {

	private final String LOG_TAG = TreasureMediaTask.class.getName();
	/*
	 * Device storage path for media files
	 */
	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";

	/*
	 * Audio file path
	 */
	private final String audioFileName = pathName + "/treasure_audio.3gp";

	/*
	 * Picture file path
	 */
	public final static String pictureFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH" + "/front_treasure_pic.bmp";

	/*
	 * Microphone handler
	 */
	private MediaRecorder mRecorder = null;

	/*
	 * Web server interface
	 */
	WSInterface _webserver;


	Context _context;


	public static boolean pictureSaved = true;

	/*
	 * Front camera preview
	 */
	CameraPreview _frontPreview;

	/*
	 * Fronnt camera handler
	 */
	Camera _frontCamera;

	/**
	 * @brief Initialize the object
	 */
	public TreasureMediaTask(Context context, CameraPreview frontPreview) throws InvalidRESTClientParametersException {
		_context = context;
		_webserver = new WSInterface();
		File f = new File(pathName);
		if ( !f.exists() ) {
			f.mkdir();
		}

		_frontPreview = frontPreview;
		_frontCamera = _frontPreview.getCamera();
	}

	@Override
	protected Void doInBackground(Void... params) {

		Log.d(LOG_TAG, "TreasureMediaTask started");

		while (!isCancelled()) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setOutputFile(audioFileName);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			try {
				mRecorder.prepare();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				this.cancel(true);
			}

			mRecorder.start();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, e.getMessage());
			}

			mRecorder.stop();
			mRecorder.release();

			/*
			 * Upload audio file on web server
			 */
			uploadAudio();

			if (getPictureSaved()) {
				try {
					_frontCamera.reconnect();
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
					this.cancel(true);
				}

				_frontCamera.startPreview();
				_frontCamera.takePicture(null, null, new FrontCameraPicture());
				setPictureSaved(false);
			}

			/*
			 * Upload audio file on web server
			 */
			uploadPicture();

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		}

		return null;
	}

	/**
	 * @brief Set the pictureSaved to the given value
	 */
	public static synchronized void setPictureSaved(boolean bool) {
		pictureSaved = bool;
	}

	/**
	 * @brief Returns the picture saved boolean
	 */
	public static synchronized boolean getPictureSaved() {
		return pictureSaved;
	}

	/**
	 * @brief Uploads the recorded audio file onn the web server
	 */
	private void uploadAudio() {
		File f = new File(audioFileName);

		if ( f.exists() ) {
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] data = new byte[(int) f.length()];
				fis.read(data);
				_webserver.uploadAudio(new Media(audioFileName, data));
			} catch (FileNotFoundException e) {
				Log.w(LOG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.w(LOG_TAG, e.getMessage());
			} catch (Exception e) {
				Log.w(LOG_TAG, e.getMessage());
			}
		}
	}

	/**
	 * @brief Uploads the captured image file onn the web server
	 */
	private void uploadPicture() {
		File f = new File(pictureFileName);
		if (f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] data = new byte[(int) f.length()];
				fis.read(data);
				_webserver.uploadPicture(new Media(pictureFileName, data));
			} catch (FileNotFoundException e) {
				Log.w(LOG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.w(LOG_TAG, e.getMessage());
			} catch (Exception e) {
				Log.w(LOG_TAG, e.getMessage());
			}
		}
	}
}

class FrontCameraPicture implements Camera.PictureCallback {

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {


		try {
			FileOutputStream fo = new FileOutputStream(TreasureMediaTask.pictureFileName);
			fo.write(data);
		} catch (FileNotFoundException e) {
			Log.w(FrontCameraPicture.class.getName(), e.getMessage());
		} catch (IOException e) {
			Log.w(FrontCameraPicture.class.getName(), e.getMessage());
		}

		TreasureMediaTask.setPictureSaved(true);
	}
}