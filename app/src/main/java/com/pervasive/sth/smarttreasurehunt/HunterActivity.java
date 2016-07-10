package com.pervasive.sth.smarttreasurehunt;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Suggestion;
import com.pervasive.sth.entities.SuggestionsGenerator;
import com.pervasive.sth.tasks.HunterDistanceTask;
import com.pervasive.sth.tasks.HunterTask;
import com.pervasive.sth.entities.Device;

import java.io.File;
import java.io.IOException;

public class HunterActivity extends AppCompatActivity {

    private static final int WINNER_REQUEST_CODE = 1;

    public static String EXIT_ACTION = "com.pervasive.sth.smarttreasurehunt.EXIT_GAME";
    public static String GPS_ACTION = "com.pervasive.sth.smarttreasurehunt.GPS_UPDATE";
    public static String AUDIO_ACTION = "com.pervasive.sth.smarttreasurehunt.AUDIO_UPDATE";
    public static String PICTURE_ACTION = "com.pervasive.sth.smarttreasurehunt.PICTURE_UPDATE";
    public static String SUGGESTION_ACTION = "com.pervasive.sth.smarttreasurehunt.SUGGESTION_UPDATE";
    public static String WINNER_ACTION = "com.pervasive.sth.smarttreasurehunt.WINNER_UPDATE";

    private GPSTracker _gps;
    private BluetoothTracker _bluetooth;
    private HunterTask _task;
    private HunterDistanceTask _distance;
    private Device _hunter;

    private RelativeLayout _rl;

    private ProgressBar gpsProgressBar;
    private ProgressBar BLProgressBar;

    private String _audioPath, _picturePath;

    private ImageView satellite;
    private ImageView radar;
    private Typewriter textualSuggestion;
    private ImageView photoSuggestion;
    private Handler textHandler = new Handler();
    private String messageReceived;

    private ImageView _photoButton;
    private ImageView _audioButton;
    private ImageView _luxButton;
    private ImageView _temperatureButton;
    private ImageView _accelerometerButton;

    boolean _receiverRegistered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_hunter);

        satellite = (ImageView) this.findViewById(R.id.satellite_pic);
        satellite.setImageResource(R.drawable.red_square);

        radar = (ImageView) this.findViewById(R.id.radar_pic);
        radar.setImageResource(R.drawable.red_square);

        gpsProgressBar = (ProgressBar) this.findViewById(R.id.gps_progress_bar);
        BLProgressBar = (ProgressBar) this.findViewById(R.id.bl_progress_bar);

        gpsProgressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        BLProgressBar.getProgressDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

        gpsProgressBar.setScaleY(10f);
        BLProgressBar.setScaleY(10f);

        gpsProgressBar.setProgress(0);
        BLProgressBar.setProgress(0);

        textualSuggestion = (Typewriter) this.findViewById(R.id.textual_suggestion);
        photoSuggestion = (ImageView) this.findViewById(R.id.photo_suggestion);

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/TravelingTypewriter.ttf");
        textualSuggestion.setTypeface(type);
        textualSuggestion.setTextSize(20);

        textualSuggestion.setCharacterDelay(50);

        _photoButton = (ImageButton) findViewById(R.id.photo_button);
        _photoButton.setImageResource(R.drawable.blue_square);
        _photoButton.setEnabled(false);

        _audioButton = (ImageButton) findViewById(R.id.audio_button);
        _audioButton.setImageResource(R.drawable.blue_square);
        _audioButton.setEnabled(false);


        _luxButton = (ImageButton) findViewById(R.id.lux_button);
        _luxButton.setImageResource(R.drawable.blue_square);
        _luxButton.setEnabled(false);

        _temperatureButton = (ImageButton) findViewById(R.id.temperature_button);
        _temperatureButton.setImageResource(R.drawable.blue_square);
        _temperatureButton.setEnabled(false);

        _accelerometerButton = (ImageButton) findViewById(R.id.accelerometer_button);
        _accelerometerButton.setImageResource(R.drawable.blue_square);
        _accelerometerButton.setEnabled(false);
        _accelerometerButton.setClickable(false);

        _rl = new RelativeLayout(this);

       // _audioButton = (Button)this.findViewById(R.id.audio_button);
        //_audioButton.setEnabled(false);

        //_pictureButton = (Button)this.findViewById(R.id.pic_button);
        //_pictureButton.setEnabled(false);

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

            registerReceiver ( receiver, new IntentFilter(EXIT_ACTION));

            registerReceiver ( receiver, new IntentFilter(GPS_ACTION));

            registerReceiver( receiver, new IntentFilter(AUDIO_ACTION));

            registerReceiver( receiver, new IntentFilter(PICTURE_ACTION));

            registerReceiver( receiver, new IntentFilter(SUGGESTION_ACTION));

            registerReceiver(receiver, new IntentFilter(WINNER_ACTION));

            _receiverRegistered = true;
        }

        Log.d("HunterTask", "Starting task");
        // Start treasure task
        if ( _task == null || _task.isCancelled() ) {
            _task = new HunterTask(this, _hunter);
            _task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if ( _distance == null || _distance.isCancelled() ) {
            _distance = new HunterDistanceTask(this, _gps, _bluetooth, _hunter);
           _distance.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if ( _distance != null && !_distance.isCancelled() )
            _distance.cancel(true);
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
        if ( _distance != null && !_distance.isCancelled() )
            _distance.cancel(true);
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
        if ( _distance != null && !_distance.isCancelled() )
            _distance.cancel(true);
    }

    public void onLuxButtonClick(View v) {
        _luxButton.setEnabled(false);
        _luxButton.setClickable(false);
        _luxButton.setImageResource(R.drawable.blue_square);
        textualSuggestion.setText(messageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(messageReceived);
    }

    public void onTemperatureButtonClick(View v) {
        _temperatureButton.setEnabled(false);
        _temperatureButton.setClickable(false);
        _temperatureButton.setImageResource(R.drawable.blue_square);
        textualSuggestion.setText(messageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(messageReceived);
    }

    public void onAccelerometerButtonClick(View v) {
        _accelerometerButton.setEnabled(false);
        _accelerometerButton.setClickable(false);
        _accelerometerButton.setImageResource(R.drawable.blue_square);
        textualSuggestion.setText(messageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(messageReceived);


    }

    public void onAudioButtonClick(View v) {

        _audioButton.setEnabled(false);
        _audioButton.setClickable(false);
        _audioButton.setImageResource(R.drawable.blue_square);

        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(_audioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "media player prepare() failed");
        }
    }

    public void onPicButtonClick(View v) {

        _photoButton.setEnabled(false);
        _photoButton.setClickable(false);
        _photoButton.setImageResource(R.drawable.blue_square);

        textualSuggestion.setVisibility(View.INVISIBLE);
        Log.d(this.getClass().getName(), "*************"+_picturePath);
        File picFile = new File(_picturePath);
        if  ( picFile.exists() ) {
            Bitmap bmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
            Matrix rotation = new Matrix();
            rotation.postRotate((float) -90.0);
            Bitmap bmapRotated = Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), rotation, true);
            photoSuggestion.setImageBitmap(bmapRotated);
            photoSuggestion.setVisibility(View.VISIBLE);

        }
    }

    public void updateBLProximityBars(double dist) {
        int progress = 100 - ((int) Math.round((dist * 100.0 / 20.0)));
        if(android.os.Build.VERSION.SDK_INT >= 11){
            // will update the "progress" propriety of seekbar until it reaches progress
            ObjectAnimator animation = ObjectAnimator.ofInt(BLProgressBar, "progress", progress);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
            BLProgressBar.setProgress(progress);
    }

    public void updateGPSProximityBars(double dist) {
        int progress = 100 - ((int) Math.round((dist * 100.0 / 20.0)));
        if(android.os.Build.VERSION.SDK_INT >= 11){
            // will update the "progress" propriety of seekbar until it reaches progress
            ObjectAnimator animation = ObjectAnimator.ofInt(gpsProgressBar, "progress", progress);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
            gpsProgressBar.setProgress(progress);
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        if (reqCode == WINNER_REQUEST_CODE) {
            finish();
        }
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
                    updateBLProximityBars(BluetoothTracker.calculateDistance(RSSI));
                    Toast.makeText(context, "BLUETOOTH: " + deviceName + " : " + RSSI + " (dBm) ->" + BluetoothTracker.calculateDistance(RSSI) + " m", Toast.LENGTH_LONG).show();
                                    //"\nGPS Distance: " + _task.getDistance(), Toast.LENGTH_LONG).show();
                }
            } else if(EXIT_ACTION.equals(mIntentAction)) {
                    if ( intent.getBooleanExtra("EXIT_GAME", false) ) {
                        finish();
                    }
            } else if(GPS_ACTION.equals(mIntentAction)) {
                updateGPSProximityBars(intent.getDoubleExtra("GPS_DISTANCE", 0.0));
                Toast.makeText(context, "GPS Distance: " + intent.getDoubleExtra("GPS_DISTANCE", 0.0), Toast.LENGTH_LONG).show();
            } else if (WINNER_ACTION.equals(mIntentAction)) {
                String pathName = intent.getStringExtra("WINNER_UPDATE");
                Intent winnerIntent = new Intent(context, TreasureCaught.class);
                winnerIntent.putExtra("WINNER_PICTURE", pathName);
                startActivityForResult( winnerIntent, WINNER_REQUEST_CODE);

            } else if ( SUGGESTION_ACTION.equals(mIntentAction)) {
                Suggestion received = (Suggestion)(intent.getSerializableExtra("SUGGESTION"));
                Log.d(this.getClass().getName(), "++++++++++++++++++++++++++++++++++++++++++++++++++TYPE: " + received.getType());

                if( received.getType() == SuggestionsGenerator.LUX_SUGGESTION) {
                    messageReceived = received.getMessage();
                    _luxButton.setEnabled(true);
                    _luxButton.setClickable(true);
                    _luxButton.setImageResource(R.drawable.red_square);

                } else if (received.getType() == SuggestionsGenerator.TEMPERATURE_SUGGESTION ) {
                    messageReceived = received.getMessage();
                    _temperatureButton.setEnabled(true);
                    _temperatureButton.setClickable(true);
                    _temperatureButton.setImageResource(R.drawable.red_square);
                } else if ( received.getType() == SuggestionsGenerator.PICTURE_SUGGESTION) {
                    Log.d(this.getClass().getName(), "Media received");
                    Log.d(this.getClass().getName(), "****************************"+received.getMessage());
                    _picturePath = received.getMessage();
                    _photoButton.setEnabled(true);
                    _photoButton.setClickable(true);
                    _photoButton.setImageResource(R.drawable.red_square);

                    //Toast.makeText(context, "Picture Received", Toast.LENGTH_LONG).show();
                } else if ( received.getType() == SuggestionsGenerator.AUDIO_SUGGESTION){
                    Log.d(this.getClass().getName(), "Media received");
                    _audioPath = received.getMessage();
                    _audioButton.setEnabled(true);
                    _audioButton.setClickable(true);
                    _audioButton.setImageResource(R.drawable.red_square);

                    //Toast.makeText(context, "Audio Received", Toast.LENGTH_LONG).show();
                } else if ( received.getType() == SuggestionsGenerator.ACCELEROMETER_SUGGESTION) {
                    messageReceived = received.getMessage();
                    _accelerometerButton.setEnabled(true);
                    _accelerometerButton.setClickable(true);
                    _accelerometerButton.setImageResource(R.drawable.red_square);
                }
            }
        }
    };




}

class Typewriter extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay


    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}