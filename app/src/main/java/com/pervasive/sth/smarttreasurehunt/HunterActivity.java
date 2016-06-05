package com.pervasive.sth.smarttreasurehunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.tasks.HunterMediaTask;
import com.pervasive.sth.tasks.HunterTask;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.tasks.TreasureMediaTask;

import java.io.File;
import java.io.IOException;

public class HunterActivity extends AppCompatActivity {

    public static String FOUND_ACTION = "com.pervasive.sth.smarttreasurehunt.TREASURE_FOUND";
    public static String GPS_ACTION = "com.pervasive.sth.smarttreasurehunt.GPS_UPDATE";
    public static String AUDIO_ACTION = "com.pervasive.sth.smarttreasurehunt.AUDIO_UPDATE";
    public static String PICTURE_ACTION = "com.pervasive.sth.smarttreasurehunt.PICTURE_UPDATE";

    private GPSTracker _gps;
    private BluetoothTracker _bluetooth;
    private HunterTask _task;
    private HunterMediaTask _media;
    private Device _hunter;

    private RelativeLayout _rl;

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

    private Button _audioButton, _pictureButton;
    private String _audioPath, _picturePath;

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
        _rl = new RelativeLayout(this);

        _audioButton = (Button)this.findViewById(R.id.audio_button);
        _audioButton.setEnabled(false);

        _pictureButton = (Button)this.findViewById(R.id.pic_button);
        _pictureButton.setEnabled(false);

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

            registerReceiver ( receiver, new IntentFilter(FOUND_ACTION));

            registerReceiver ( receiver, new IntentFilter(GPS_ACTION));

            registerReceiver( receiver, new IntentFilter(AUDIO_ACTION));

            registerReceiver( receiver, new IntentFilter(PICTURE_ACTION));

            _receiverRegistered = true;
        }

        Log.d("HunterTask", "Starting task");
        // Start treasure task
        if ( _task == null || _task.isCancelled() ) {
            _task = new HunterTask(this, _gps, _bluetooth, _hunter);
            _task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if ( _media == null || _media.isCancelled() ) {
            _media = new HunterMediaTask(this);
            _media.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if ( _media != null && !_media.isCancelled() )
            _media.cancel(true);
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
        if ( _media != null && !_media.isCancelled() )
            _media.cancel(true);
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
        if ( _media != null && !_media.isCancelled() )
            _media.cancel(true);
    }

    public void onAudioButtonClick(View v) {
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(_audioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "media player prepare() failed");
        }

        _audioButton.setEnabled(false);
    }

    public void onPicButtonClick(View v) {
        File picFile = new File(_picturePath);
        if  ( picFile.exists() ) {
            Bitmap bmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
            Matrix rotation = new Matrix();
            rotation.postRotate((float) -90.0);
            Bitmap bmapRotated = Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), rotation, true);
            ImageView image = (ImageView) findViewById(R.id.image_view);
            image.setImageBitmap(bmapRotated);
            image.setVisibility(View.VISIBLE);

        }

        _pictureButton.setEnabled(false);
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
                    Toast.makeText(context, "BLUETOOTH: " + deviceName + " : " + RSSI + " (dBm) ->" + BluetoothTracker.calculateDistance(RSSI) + " m", Toast.LENGTH_LONG).show();
                                    //"\nGPS Distance: " + _task.getDistance(), Toast.LENGTH_LONG).show();
                }
            } else if(FOUND_ACTION.equals(mIntentAction)) {
                    if ( intent.getBooleanExtra("TREASURE_FOUND", false) ) {
                        Toast.makeText(context, "The treasure has been found!", Toast.LENGTH_LONG).show();
                        finish();
                    }
            } else if(GPS_ACTION.equals(mIntentAction)) {
                Toast.makeText(context, "GPS Distance: " + intent.getDoubleExtra("GPS_DISTANCE", 0.0), Toast.LENGTH_LONG).show();
            } else if(AUDIO_ACTION.equals(mIntentAction)) {
                Log.d(this.getClass().getName(), "Media received");
                _audioPath = intent.getStringExtra("MEDIA_AUDIO");
                _audioButton.setEnabled(true);
                Toast.makeText(context, "Audio Received", Toast.LENGTH_LONG).show();
            } else if(PICTURE_ACTION.equals(mIntentAction)) {
                Log.d(this.getClass().getName(), "Media received");
                _picturePath = intent.getStringExtra("MEDIA_PICTURE");
                _pictureButton.setEnabled(true);
                Toast.makeText(context, "Picture Received", Toast.LENGTH_LONG).show();
            }
        }
    };
}
