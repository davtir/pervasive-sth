package com.pervasive.sth.smarttreasurehunt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StartupActivity extends AppCompatActivity {

	private final String LOG_TAG = StartupActivity.class.getName();
	private LocationManager _gpsManager;
	private BluetoothManager _bluetoothManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set content view AFTER ABOVE sequence (to avoid crash)
		setContentView(R.layout.activity_startup);

		// Creating fake images
		try {
			createFakeImages(3);
		} catch ( IOException e ) {
			Log.e(LOG_TAG, e.toString());
			finish();
		}

		// Creating blinking welcome message
		TextView blinkingText = (TextView) findViewById(R.id.blinking);
		createBlinkingText(blinkingText, 1000);

		// Creating gps service manager
		_gpsManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		// Creating bluetooth service manager
		_bluetoothManager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);

		// If not activated yet, then request to user bluetooth activation
		if ( !_bluetoothManager.getAdapter().isEnabled() ) {
			requestBluetoothPermissions(this);
		}

		// If not activated, , then request to user gps activation
		if ( !_gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
			requestGPSPermissions(this);
		}

		Log.d(LOG_TAG, "StartupActivity have been created.");
	}

	private void createFakeImages(int images) throws IOException {
		String imagesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";
		for ( int i = 0; i <= images; ++i ) {
			int fakeImageID = R.drawable.fake_image + i;
			Log.d(this.getClass().getName(), "Creating fake image " + fakeImageID);

			Bitmap bm = BitmapFactory.decodeResource(getResources(), fakeImageID);
			File file = new File(imagesPath, "fake_image" + i + ".png");
			FileOutputStream outStream = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();
		}
	}

	private void createBlinkingText(TextView blinkingText, int blinkDelay) {
		if ( blinkingText != null ) {
			Animation anim = new AlphaAnimation(0.0f, 1.0f);
			anim.setDuration(blinkDelay); //You can manage the blinking time with this parameter
			anim.setStartOffset(20);
			anim.setRepeatMode(Animation.REVERSE);
			anim.setRepeatCount(Animation.INFINITE);
			blinkingText.startAnimation(anim);
		}
	}

	public void requestGPSPermissions(final Context context) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle("GPS Permission Request");
		alertDialog.setMessage("GPS is not enabled. Do you want to open settings?");
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
			}
		});

		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();
	}

	public void requestBluetoothPermissions(final Context context) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		alertDialog.setTitle("Bluetooth permission request");
		alertDialog.setMessage("Bluetooth is not enabled. Do you want to open settings?");
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				context.startActivity(intent);
			}
		});

		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();
	}

	public boolean onTouchEvent(MotionEvent event) {
		boolean bluetoothEnabled = _bluetoothManager.getAdapter().isEnabled();
		boolean gpsEnabled = _gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if ( bluetoothEnabled && gpsEnabled) {
			int action = event.getAction();
			switch ( action ) {
				case MotionEvent.ACTION_DOWN:
					startActivity(new Intent(this, MainActivity.class));
					break;
				default:
					return false;
			}
			return true;
		} else {
			Log.d(LOG_TAG, "Bluetooth or GPS not available yet.");
			if ( !bluetoothEnabled ) {
				Toast.makeText(this, "Bluetooth is not available yet.", Toast.LENGTH_SHORT).show();
			}
			if ( !gpsEnabled ) {
				Toast.makeText(this, "GPS is not available yet.", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
	}

}
