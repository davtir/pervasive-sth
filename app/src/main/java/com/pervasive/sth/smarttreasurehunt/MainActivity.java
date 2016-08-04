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

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

	private static final int DISCOVERABLE_REQUEST_CODE = 0;

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


		Log.d("MainActivity", "MainActivity started.");
	}

	public void onClickTreasure(View v) {
		// Request permissions for device discoverability
		Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverable, DISCOVERABLE_REQUEST_CODE);
	}

	public void onClickHunter(View v) {
		startActivity(new Intent(this, HunterActivity.class));
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch ( requestCode ) {
			case DISCOVERABLE_REQUEST_CODE:
				if ( resultCode == 1 ) {
					startActivity(new Intent(this, TreasureActivity.class));
				}
				break;
		}
	}
}
