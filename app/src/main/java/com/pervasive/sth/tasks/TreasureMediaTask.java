package com.pervasive.sth.tasks;

import android.content.Context;


import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.pervasive.sth.entities.Media;
import com.pervasive.sth.entities.CameraPreview;
import com.pervasive.sth.rest.WSInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alex on 29/05/2016.
 */
public class TreasureMediaTask extends AsyncTask<Void, Void, Void> {

	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";
	private final String audioFileName = pathName + "/treasure_audio.3gp";
	public final static String frontPictureFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH" + "/front_treasure_pic.bmp";
	public final static String backPictureFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH" + "/back_treasure_pic.bmp";
	public final static String pictureFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH" + "/front_treasure_pic.bmp";
	private MediaRecorder mRecorder = null;
	WSInterface _webserver;
	Context _context;
	public static boolean pictureSaved = true;

	CameraPreview _frontPreview;
	CameraPreview _backPreview;
	Camera _frontCamera;
	//Camera _backCamera;

	public TreasureMediaTask(Context context, CameraPreview frontPreview, CameraPreview backPreview) {
		Log.d(this.getClass().getName(), "Inizio Costruttore");

		_context = context;
		_webserver = new WSInterface();
		File f = new File(pathName);
		if ( !f.exists() )
			f.mkdir();

		_frontPreview = frontPreview;
		_backPreview = backPreview;
		_frontCamera = _frontPreview.getCamera();
		//_backCamera = _backPreview.getCamera();

		Log.d(this.getClass().getName(), "Fine Costruttore");
	}

	@Override
	protected Void doInBackground(Void... params) {

		Log.d(this.getClass().getName(), "Entrato in mediaExecute");

		while ( !isCancelled() ) {

			Log.d(this.getClass().getName(), "ENTRATO TREASURE MEDIA TASK");
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setOutputFile(audioFileName);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			try {
				mRecorder.prepare();
			} catch ( IOException e ) {
				Log.e("TreasureMediaTask", e.getMessage());
			}

			mRecorder.start();

			try {
				Thread.sleep(10000);
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}

			mRecorder.stop();

			mRecorder.release();

			uploadAudio();

			if ( getPictureSaved() ) {
				try {
					_frontCamera.reconnect();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				_frontCamera.startPreview();
				Log.d(this.getClass().getName(), "BEFORE TAKING PICTURE");
				_frontCamera.takePicture(null, null, new FrontCameraPicture());
				//_backCamera.takePicture(null, null, new BackCameraPicture());
				setPictureSaved(false);
				//_frontCamera.release();
			}


			uploadPicture();

			Log.d(this.getClass().getName(), "Picture taken");

			try {
				Thread.sleep(30000);
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}


		}

		//_frontCamera.release();
		//_backCamera.release();

		return null;
	}

	public static synchronized void setPictureSaved(boolean bool) {
		pictureSaved = bool;
	}

	public static synchronized boolean getPictureSaved() {
		return pictureSaved;
	}

	private void uploadAudio() {
		File f = new File(audioFileName);

		if ( f.exists() ) {

			try {

				FileInputStream fis = new FileInputStream(f);

				byte[] data = new byte[(int) f.length()];

				fis.read(data);


				_webserver.uploadAudio(new Media(audioFileName, data));


			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	private void uploadPicture() {
		File f = new File(pictureFileName);

		if ( f.exists() ) {
			try {

				FileInputStream fis = new FileInputStream(f);

				byte[] data = new byte[(int) f.length()];

				fis.read(data);


				_webserver.uploadPicture(new Media(pictureFileName, data));


			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

}

class FrontCameraPicture implements Camera.PictureCallback {

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {


		try {
			FileOutputStream fo = new FileOutputStream(TreasureMediaTask.frontPictureFileName);
			fo.write(data);
			Log.d(this.getClass().getName(), "Picture written on data --------------------");
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		//camera.stopPreview();
		//camera.setPreviewCallback(null);
		//camera.release();
		TreasureMediaTask.setPictureSaved(true);
	}
}

class BackCameraPicture implements Camera.PictureCallback {

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(this.getClass().getName(), "Entered");
		try {
			FileOutputStream fo = new FileOutputStream(TreasureMediaTask.backPictureFileName);
			fo.write(data);
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}