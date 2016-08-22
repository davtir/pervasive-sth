package com.pervasive.sth.smarttreasurehunt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.CameraPreview;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;
import com.pervasive.sth.tasks.TreasureMediaTask;
import com.pervasive.sth.tasks.TreasureTask;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.network.WSInterface;

/**
 * @brief The treasure device activity
 */
public class TreasureActivity extends AppCompatActivity {

	private static String LOG_TAG = TreasureActivity.class.getName();

	/*
	 * Starts the activity for capturing the winner picture
	 */
	static final int REQUEST_IMAGE_CAPTURE = 1;

	public static final String EXCEPTION_ACTION = "com.pervasive.sth.smarttreasurehunt.EXCEPTION_ACTION";

	/*
	 * Starts the GPS handler for getting lat and lon coordinates of the device
	 */
	private GPSTracker _gps;

	/*
	 * The asynchronous treasure task
	 */
	private TreasureTask _task;

	/*
	 * The asynchronous treasure media task
	 */
	private TreasureMediaTask _media;

	/*
	 * The treasure device
	 */
	private Device treasure;

	/*
	 * The front camera preview
	 */
	private CameraPreview _frontPreview;

	/*
	 * The camera invisible layout
	 */
	private FrameLayout frontPrevLayout;

	/*
	 * flag for intent already registered in the broadcast receiver
	 */
	boolean _receiverRegistered;

	/**
	 * @brief initialization of Treasure activity components
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(LOG_TAG, "onCreate() invoked.");

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_treasure);

		// Initialize GPS and Bluetooth trackers
		_gps = new GPSTracker(this);
		try {
			_gps.getLocation();
		} catch ( Exception e ) {
			Log.e(LOG_TAG, e.toString());
			showErrorDialog(e.getMessage());
		}

		/*
		 * Initialize camera preview and layouts
		 */
		_frontPreview = new CameraPreview(this, Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT));
		frontPrevLayout = (FrameLayout) findViewById(R.id.front_camera_preview);
		frontPrevLayout.addView(_frontPreview);

	}

	/**
	 * @brief initialization of Hunter activity when resumed
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume() invoked.");

		_receiverRegistered = false;
		if ( !_receiverRegistered ) {
			// Register for broadcasts when a device is discovered
			registerReceiver(receiver, new IntentFilter(EXCEPTION_ACTION));

			_receiverRegistered = true;
		}
		// Start treasure task
		if (_task == null || _task.isCancelled()) {
			// Initialize treasure task
			try {
				_task = new TreasureTask(this, _gps, treasure);
			} catch (RuntimeException e) {
				Log.e(LOG_TAG, e.toString());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				showErrorDialog(e.getMessage());
				return;
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
				showErrorDialog(e.getMessage());
			}
			try {
				_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} catch ( Exception e ) {
				Log.e(LOG_TAG, e.toString());
				showErrorDialog(e.getMessage());
			}
		}

		// Start treasure media task
		if (_media == null || _media.isCancelled()) {
			// Initialize media task
			try {
				_media = new TreasureMediaTask(this, _frontPreview);
			} catch (RuntimeException e) {
				Log.e(LOG_TAG, e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				showErrorDialog(e.getMessage());
				return;
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage());
				showErrorDialog(e.getMessage());
				return;
			}
			try {
				_media.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} catch ( Exception e ) {
				Log.e(LOG_TAG, e.toString());
				showErrorDialog(e.getMessage());
			}
		}
	}

	protected void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause() invoked.");
	}

	protected void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "onStop() invoked.");
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy() invoked.");

		// Stop treasure task
		if (_task != null && !_task.isCancelled())
			_task.cancel(true);
		if (_media != null && !_media.isCancelled())
			_media.cancel(true);
	}

	public void onClickCaught(View v) {
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

			Toast.makeText(this, "Congratulations! Treasure caught!!!", Toast.LENGTH_LONG).show();

			try {
				EndGameThread endGameThread = new EndGameThread(true, imageBitmap);
				endGameThread.start();
			} catch (InvalidRESTClientParametersException e) {
				Log.e(LOG_TAG, e.toString());
				showErrorDialog(e.getMessage());
			}
			finish();
		}
	}

	/**
	 * @brief Displays the detected error
	 */
	private void showErrorDialog(String message) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Internal Error");
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.show();
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String mIntentAction = intent.getAction();

			if ( mIntentAction.equals(TreasureActivity.EXCEPTION_ACTION) ) {
				Log.e(TreasureActivity.class.getName(), intent.getStringExtra("EXCEPTION_NAME"));
				showErrorDialog(intent.getStringExtra("EXCEPTION_NAME"));
			}
		}
	};
}

/**
 * @brief Uploads treasure status and winner picture to the web server
 */
class EndGameThread extends Thread {

	private boolean _status;
	private Bitmap _bitmap;
	private WSInterface _webserver;

	public EndGameThread(boolean status, Bitmap bitmap) throws InvalidRESTClientParametersException {
		_status = status;
		_bitmap = bitmap;
		_webserver = new WSInterface();
	}

	public void run() {
		try {
			Log.d(this.getClass().getName(), "Updating...........");
			_webserver.updateTreasureStatus(_status, _bitmap);
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}
	}
}