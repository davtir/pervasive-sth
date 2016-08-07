package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
import com.pervasive.sth.entities.Media;
import com.pervasive.sth.tasks.TreasureMediaTask;
import com.pervasive.sth.tasks.TreasureTask;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.rest.WSInterface;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class TreasureActivity extends AppCompatActivity {

	static final int REQUEST_IMAGE_CAPTURE = 1;

	private GPSTracker _gps;
	private BluetoothAdapter _bluetooth;
	TreasureTask _task;
	TreasureMediaTask _media;
	Device treasure;
	WSInterface _webserver;

	CameraPreview _frontPreview;
	CameraPreview _backPreview;
	FrameLayout frontPrevLayout;
	//FrameLayout backPrevLayout;

	private void setupCamera() {
		Log.d(this.getClass().getName(), "Setting up camera.");
		try {
			Log.d(this.getClass().getName(), "Front camera opened;");


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_treasure);

		// Request permissions for device discoverability
		Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverable, 0);

		// Initialize GPS and Bluetooth trackers
		_gps = new GPSTracker(this);
		_gps.getLocation();
		_bluetooth = BluetoothAdapter.getDefaultAdapter();
		_webserver = new WSInterface();

		setupCamera();
		_frontPreview = new CameraPreview(this, Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT));
		frontPrevLayout = (FrameLayout) findViewById(R.id.front_camera_preview);
		frontPrevLayout.addView(_frontPreview);

		//_backPreview = new CameraPreview(this,  Camera.open(1));
		//backPrevLayout = (FrameLayout) findViewById(R.id.front_camera_preview);
		//backPrevLayout.addView(_backPreview);

	}

	protected void onResume() {
		Log.d("TreasureTask", "onResume() invoked.");
		super.onResume();

		// Start treasure task
		if (_task == null || _task.isCancelled()) {
			// Initialize treasure task
			try {
				_task = new TreasureTask(this, _gps, treasure);
			} catch (RuntimeException e) {
				Log.e("TreasureActivity", e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return;
			} catch (Exception e) {
				//  Log.e("TreasureActivity", e.getMessage());
				finish();
				return;
			}
			_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}


		if (_media == null || _media.isCancelled()) {
			// Initialize media task
			Log.d(this.getClass().getName(), "Entrato in media == null || media is cancelled");
			try {
				_media = new TreasureMediaTask(this, _frontPreview, _backPreview);
			} catch (RuntimeException e) {
				Log.d(this.getClass().getName(), "Primo catch");
				Log.e("TreasureActivity", e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return;
			} catch (Exception e) {
				Log.d(this.getClass().getName(), "Secondo catch");
				Log.e("TreasureActivity", e.getMessage());
				finish();
				return;
			}
			_media.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	protected void onPause() {
		super.onPause();
		Log.d("TreasureTask", "onPause() invoked.");

		// Stop treasure task
		/*if (_task != null && !_task.isCancelled())
            _task.cancel(true);
        if (_media != null && !_media.isCancelled())
            _media.cancel(true);*/
	}

	protected void onStop() {
		super.onStop();
		Log.d("TreasureTask", "onStop() invoked.");

		// Stop treasure task
        /*if (_task != null && !_task.isCancelled())
            _task.cancel(true);
        if (_media != null && !_media.isCancelled())
            _media.cancel(true);*/
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("TreasureTask", "onDestroy() invoked.");

		// Stop treasure task
		if (_task != null && !_task.isCancelled())
			_task.cancel(true);
		if (_media != null && !_media.isCancelled())
			_media.cancel(true);
	}

	public void onClickCaught(View v) {
		Log.d(this.getClass().getName(), "CATCH ME CLICKED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");

		//Stop the Treasure Task
		TreasureTask.setFound(true);
		//TreasureTask.setWinner(imageBitmap);
		if (_task != null && !_task.isCancelled())
			_task.cancel(true);
		if (_media != null && !_media.isCancelled())
			_media.cancel(true);

		_frontPreview.getCamera().release();

		//Launch the camera intent
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		//retrieve the winner photo
		Bitmap imageBitmap = null;
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			imageBitmap = (Bitmap) extras.get("data");
			//mImageView.setImageBitmap(imageBitmap);

			Toast.makeText(this, "Congratulations! Treasure caught!!!", Toast.LENGTH_LONG).show();


			finish();

			EndGameThread endGameThread = new EndGameThread(true, imageBitmap);
			endGameThread.start();
		}
	}
}

class EndGameThread extends Thread {

	private boolean _status;
	private Bitmap _bitmap;
	private WSInterface _webserver;

	public EndGameThread(boolean status, Bitmap bitmap) {
		_status = status;
		_bitmap = bitmap;
		_webserver = new WSInterface();
	}

	public void run() {
		try {
			Log.d("TreasureTask", "Updating...........");
			_webserver.updateTreasureStatus(_status, _bitmap);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}