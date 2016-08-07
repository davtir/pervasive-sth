package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @brief	This class implements the activity associated
 * 			to the main screen of the application, in which
 * 			players are allowed to choose the role of their devices.
 */
public class MainActivity extends AppCompatActivity {

	private static final int DISCOVERABLE_REQUEST_CODE = 0;
	private static final String LOG_TAG = MainActivity.class.getName();

	/**
	 * @brief	This function implements the creation procedure
	 *			of this activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		TextView roleTextView = (TextView) this.findViewById(R.id.select_your_role_textview);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/TravelingTypewriter.ttf");
		roleTextView.setTypeface(type);


		Log.d(LOG_TAG, "MainActivity started.");
	}

	/**
	 * @param	v: The view that has generated the event
	 * @brief	This function handles the click event associated
	 * 			to the treasure button.
	 */
	public void onClickTreasure(View v) {
		// Request permissions for device discoverability
		Log.d(LOG_TAG, "Requesting infinite bluetooth discoverability...");
		Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverable, DISCOVERABLE_REQUEST_CODE);
	}

	/**
	 * @param	v: The view that has generated the event
	 * @brief	This function handles the click event associated
	 * 			to the hunter button.
	 */
	public void onClickHunter(View v) {
		startActivity(new Intent(this, HunterActivity.class));
	}

	/**
	 * @param	requestCode: The request code of the associated activity
	 * @param	resultCode: The result code of the associated activity
	 * @param	data: The associated intent
	 * @brief	This functions handles the result of the activity associated
	 * 			to the bluetooth discoverability request.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch ( requestCode ) {
			case DISCOVERABLE_REQUEST_CODE:
				if ( resultCode == 1 ) {
					Log.d(LOG_TAG, "Infinite bluetooth discoverability allowed.");
					startActivity(new Intent(this, TreasureActivity.class));
				} else {
					Log.d(LOG_TAG, "Infinite bluetooth discoverability not allowed.");
				}
				break;
		}
	}
}
