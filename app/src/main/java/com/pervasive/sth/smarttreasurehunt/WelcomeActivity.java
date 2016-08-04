package com.pervasive.sth.smarttreasurehunt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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

/**
 * @brief	This class implements the activity associated
 * 			to the welcome screen of the application.
 */
public class WelcomeActivity extends AppCompatActivity {

	/*
	 * Tag string for logs
	 */
	private final String LOG_TAG = WelcomeActivity.class.getName();

	/*
	 * The Location manager class for GPS
	 */
	private LocationManager _gpsManager;

	/*
	 * The bluetooth manager class
	 */
	private BluetoothManager _bluetoothManager;

	/**
	 * @brief	This function implements the creation procedure
	 *			of this activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set content view AFTER ABOVE sequence (to avoid crash)
		setContentView(R.layout.activity_welcome);

		// Creating fake images
		try {
			createFakeImages(3);
		} catch ( IOException e ) {
			Log.e(LOG_TAG, e.toString());
			finish();
		}

		Typeface type = Typeface.createFromAsset(getAssets(), "fonts/TravelingTypewriter.ttf");

		// Creating blinking welcome message
		TextView blinkingText = (TextView) findViewById(R.id.blinking);
		blinkingText.setTypeface(type);
		createBlinkingText(blinkingText, 1000);

		// Creating info string
		TextView infoText = (TextView) findViewById(R.id.info_textview);
		infoText.setTypeface(type);

		// Creating gps service manager
		_gpsManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		// Creating bluetooth service manager
		_bluetoothManager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);

		// If the bluetooth have not been activated yet ...
		if ( !_bluetoothManager.getAdapter().isEnabled() ) {
			// ... request the activation to the user
			requestBluetoothPermissions(this);
		}

		// If the GPS have not been activated yet ...
		if ( !_gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
			// ... request the activation to the user
			requestGPSPermissions(this);
		}

		Log.d(LOG_TAG, "WelcomeActivity have been created.");
	}

	/**
	 * @param	imagesNumber: The number of images to be written
	 * @throws	IOException
	 * @brief	Generates 'imagesNumber' fake images on external storage
	 * 			starting from fake images in drawable
	 */
	private void createFakeImages(int imagesNumber) throws IOException {
		String imagesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";
		for ( int i = 0; i <= imagesNumber; ++i ) {
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

	/**
	 * @param	blinkingText: The text view that needs to be animated
	 * @param	blinkDelay: The blink delay in ms
	 * @brief	Enables blinking animation to the text view in input
	 */
	private void createBlinkingText(TextView blinkingText, int blinkDelay) {
		if ( blinkingText != null ) {
			Animation anim = new AlphaAnimation(0.0f, 1.0f);
			anim.setDuration(blinkDelay);
			anim.setStartOffset(20);
			anim.setRepeatMode(Animation.REVERSE);
			anim.setRepeatCount(Animation.INFINITE);
			blinkingText.startAnimation(anim);
		}
	}

	/**
	 * @param	context: The current context
	 * @brief	Requests GPS activation to the user
	 */
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

	/**
	 * @param	context: The current context
	 * @brief	Requests bluetooth activation to the user
	 */
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

	/**
	 * @param	event: The event to be handled
	 * @return	True if correctly handled, false otherwise
	 * @brief	Handles touch event
	 */
	public boolean onTouchEvent(MotionEvent event) {
		boolean bluetoothEnabled = _bluetoothManager.getAdapter().isEnabled();
		boolean gpsEnabled = _gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		int action = event.getAction();
		switch ( action ) {
			case MotionEvent.ACTION_DOWN:
				if ( bluetoothEnabled && gpsEnabled) {
					startActivity(new Intent(this, MainActivity.class));
				} else {
					Log.d(LOG_TAG, "Bluetooth or GPS not available yet.");
					if ( !bluetoothEnabled ) {
						Toast.makeText(this, "Bluetooth is not available yet.", Toast.LENGTH_SHORT).show();
					}
					if ( !gpsEnabled ) {
						Toast.makeText(this, "GPS is not available yet.", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			default:
				return false;
		}
		return true;
	}

}
