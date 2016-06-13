package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.CameraPreview;
import com.pervasive.sth.tasks.TreasureMediaTask;
import com.pervasive.sth.tasks.TreasureTask;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.rest.WSInterface;

import java.io.File;
import java.io.IOException;

public class TreasureActivity extends AppCompatActivity {

	private GPSTracker _gps;
	TreasureTask _task;
	TreasureMediaTask _media;
	Device treasure;
	WSInterface _webserver;

	CameraPreview _frontPreview;
	CameraPreview _backPreview;
	FrameLayout _frontPrevLayout;
	//FrameLayout backPrevLayout;

	private static final String LOG_TAG = TreasureActivity.class.getName();

	private void releaseCameraPreview() {
		if ( _frontPreview != null ) {
			_frontPreview.releaseCamera();
			_frontPrevLayout.removeView(_frontPreview);
			_frontPreview = null;
		}
	}

	private void terminateTask(AsyncTask<Void, Void, Void> task) {
		if ( task != null && !task.isCancelled() )
			task.cancel(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_treasure);

		// Initialize GPS and Bluetooth trackers
		_gps = new GPSTracker(this);
		_gps.getLocation();
		_webserver = new WSInterface();
	}

	protected void onResume() {
		Log.d(LOG_TAG, "onResume() invoked.");
		super.onResume();

		_frontPreview = new CameraPreview(this, Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT));
		_frontPrevLayout = (FrameLayout) findViewById(R.id.front_camera_preview);
		_frontPrevLayout.addView(_frontPreview);

		// Start treasure task
		if ( _task == null || _task.isCancelled() ) {
			// Initialize treasure task
			try {
				_task = new TreasureTask(this, _gps, treasure);
			} catch ( RuntimeException e ) {
				Log.e(LOG_TAG, e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return;
			} catch ( Exception e ) {
				Log.e(LOG_TAG, e.getMessage());
				finish();
				return;
			}
			_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}


		if ( _media == null || _media.isCancelled() ) {
			// Initialize media task
			try {
				_media = new TreasureMediaTask(this, _frontPreview, _backPreview);
			} catch ( RuntimeException e ) {
				Log.e(LOG_TAG, e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return;
			} catch ( Exception e ) {
				Log.e(LOG_TAG, e.getMessage());
				finish();
				return;
			}
			_media.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	protected void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause() invoked.");

		// Stop treasure tasks
		terminateTask(_task);
		terminateTask(_media);

		releaseCameraPreview();
	}

	protected void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "onStop() invoked.");

		// Stop treasure task
		terminateTask(_task);
		terminateTask(_media);

		releaseCameraPreview();
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy() invoked.");

		// Stop treasure task
		terminateTask(_task);
		terminateTask(_media);

		releaseCameraPreview();
	}

	public void onClickCaught(View v) {
		Log.d(LOG_TAG, "Treasure caught!");

		Toast.makeText(this, "Congratulations! Treasure caught!!!", Toast.LENGTH_LONG).show();

		TreasureTask.setFound(true);
		finish();
	}
}
