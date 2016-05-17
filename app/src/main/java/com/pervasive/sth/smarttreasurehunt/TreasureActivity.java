package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.distances.HunterTask;
import com.pervasive.sth.distances.TreasureTask;

public class TreasureActivity extends AppCompatActivity {

    private GPSTracker _gps;
    private BluetoothAdapter _bluetooth;
    TreasureTask _task;
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
    }

    protected void onResume() {
        Log.d("TreasureTask", "onResume() invoked.");
        super.onResume();

        // Start treasure task
        if ( _task == null || _task.isCancelled() ) {
            // Initialize treasure task
            try {
                _task = new TreasureTask(this, _gps);
            } catch ( RuntimeException e ) {
                //Log.e("TreasureActivity", e.getMessage().toString());
                Toast.makeText(this, "A treasure already exists", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (Exception e) {
              //  Log.e("TreasureActivity", e.getMessage());
                finish();
                return;
            }
            _task.execute();
        }
    }

    protected void onPause() {
        super.onPause();
        Log.d("TreasureTask", "onPause() invoked.");

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
    }

    protected void onStop() {
        super.onStop();
        Log.d("TreasureTask", "onStop() invoked.");

        // Stop treasure task
        if ( _task != null &&  !_task.isCancelled() )
            _task.cancel(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("TreasureTask", "onDestroy() invoked.");

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
    }

    public void onClickCaught(View v) {
        Log.d("TreasureActivity", "Treasure caught!");
        Toast.makeText(this, "Congratulations! Treasure caught!!!", Toast.LENGTH_LONG).show();

        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
        finish();
    }
}
