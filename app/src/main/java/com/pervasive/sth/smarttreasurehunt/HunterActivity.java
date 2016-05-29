package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.distances.HunterTask;
import com.pervasive.sth.distances.TreasureTask;
import com.pervasive.sth.entities.Device;

import org.w3c.dom.Text;

public class HunterActivity extends AppCompatActivity {

    private GPSTracker _gps;
    private BluetoothTracker _bluetooth;
    private HunterTask _task;
    private Device _hunter;

    private TextView _GPSView;
    private static TextView _GPSValue;
    private TextView _BLTView;
    private TextView _BLTValue;
    private TextView _LuxView;
    private static TextView _LuxVal;
    private TextView _AccView;
    private static TextView _AccVal;
    private TextView _RotView;
    private static TextView _RotVal;

    boolean _receiverRegistered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_hunter);

        _GPSView = (TextView)this.findViewById(R.id.GPSView);
        _GPSValue = (TextView)this.findViewById(R.id.GPSVal);
        _BLTView = (TextView)this.findViewById(R.id.BLTView);
        _BLTValue = (TextView)this.findViewById(R.id.BLTVal);
        _LuxView = (TextView)this.findViewById(R.id.LuxView);
        _LuxVal = (TextView)this.findViewById(R.id.LuxVal);
        _AccView = (TextView)this.findViewById(R.id.AccView);
        _AccVal = (TextView)this.findViewById(R.id.AccVal);
        _RotView = (TextView)this.findViewById(R.id.RotView);
        _RotVal = (TextView)this.findViewById(R.id.RotVal);


        _gps = new GPSTracker(this);
        _bluetooth = new BluetoothTracker(this, receiver);

        _gps.getLocation();
 //       _task = new HunterTask(this, _gps, _bluetooth);
  //      _task.execute();

        _receiverRegistered = false;

        _hunter = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "H");
    }

    protected void onResume() {
        Log.d("HunterTask", "onResume() invoked.");
        super.onResume();

        if ( !_receiverRegistered ) {
            // Register for broadcasts when a device is discovered
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

            // Register for broadcasts when discovery has finished
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

            // Register for broadcast when discovery has started
            registerReceiver( receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));

            _receiverRegistered = true;
        }

        Log.d("HunterTask", "Starting task");
        // Start treasure task
        if ( _task == null || _task.isCancelled() ) {
            _task = new HunterTask(this, _gps, _bluetooth, _hunter);
            _task.execute();
        }
    }

    protected void onPause() {
        super.onPause();
        Log.d("HunterTask", "onPause() invoked.");

        if ( _receiverRegistered ) {
            unregisterReceiver(receiver);
            _receiverRegistered = false;
        }

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
    }

    protected void onStop() {
        super.onStop();
        Log.d("HunterTask", "onStop() invoked.");


        if ( _receiverRegistered ) {
            unregisterReceiver(receiver);
            _receiverRegistered = false;
        }

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("HunterTask", "onDestroy() invoked.");

        if ( _receiverRegistered ) {
            unregisterReceiver(receiver);
            _receiverRegistered = false;
        }

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(true);
    }

    public static void setGPSDistance(double dist) {
       // _GPSValue.setText(Math.round(dist * 100)/100 + " m");
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String mIntentAction = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(mIntentAction)) {
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                Log.d("Receiver", "Received ID: " + deviceAddress + " TreasureID " + _task.getTreasureID());
                if( deviceAddress.equals(_task.getTreasureID()) ) {
                   // _BLTValue.setText(Math.round(BluetoothTracker.calculateDistance(RSSI)*100)/100 + " m");
                    Toast.makeText(context,
                            "BLUETOOTH: " + deviceName + " : " + RSSI + " (dBm) ->" + BluetoothTracker.calculateDistance(RSSI) + " m" +
                                    "\nGPS Distance: " + _task.getDistance(), Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
